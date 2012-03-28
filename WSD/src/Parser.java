package src;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

public class Parser {

	public static ArrayList<String> trainDoc = null;
	public static ArrayList<String> testDoc = null;
	public static ArrayList<String> models = null;
	public static HashSet<String> commonWords = null;
	public static HashMap<String, Integer> countWords = null;
	public static InputStream modelIn = null;
	public static SentenceModel sm = null;
	public static SentenceDetectorME sentenceDetector = null;
	public static HashMap<String, Integer> dfTable = null;
	public static HashMap<String, LinkedHashMap<String, Integer>> tfTable = null;
	public static HashMap<String, LinkedHashMap<String, Double>> tfidfTable = null;
	public static HashMap<String, String> mostFrequentLabels = null;

	public static int windowSize = 25;
	public static int NUM_FEATURES_PER_SENSE = 5;

	public static void main(String[] args) {
		trainDoc = loadDoc("wsd-data/train.data");
		Collections.sort(trainDoc, new LineComparator());
		testDoc = loadDoc("wsd-data/test.data");
		initSentenceDetector();
		loadCommonWords();
		buildMostFrequentLabelsTable();
		//genBest();
		loadModels();
		countWords();
		buildPrecisionTable("precision.map");
		for(String model : models)
			handleModel(model);
	}

	private static void genBest() {
	    try {
	        // Create file
        FileWriter fstream = new FileWriter("kaggle-perf.csv");
        BufferedWriter out = new BufferedWriter(fstream);
        for(String line : testDoc) {
            String word = getWord(line);
            String bestLabel = mostFrequentLabels.get(word);
            for(int i = 0; i < bestLabel.length(); i++)
                out.write(bestLabel.charAt(i) + "\n");
        }
        //Close the output stream
        out.close();
        } catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
	}


	private static void initSentenceDetector() {
	    try {
    	    modelIn = new FileInputStream("models/en-sent.bin");
            sm = new SentenceModel(modelIn);
            sentenceDetector = new SentenceDetectorME(sm);
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	}

	private static void incrementMap(HashMap<String, Integer> map, String key) {
	    map.put(key, map.containsKey(key) ? map.get(key) + 1 : 1);
	}

	private static void buildMostFrequentLabelsTable() {
	    mostFrequentLabels = new HashMap<String, String>();

        HashMap<String, Integer> labelCount = new HashMap<String, Integer>();
        String word = null;
        for(String line : trainDoc) {
            String newWord = getWord(line);
            String label = getSense(line);
            if(word != null && !newWord.equals(word)) {
                labelCount = sortIntHashMap(labelCount);
                Iterator<Entry<String, Integer>> iter = labelCount.entrySet().iterator();
                mostFrequentLabels.put(word,iter.next().getKey());
                labelCount.clear();
            }
            word = newWord;
            incrementMap(labelCount, label);
        }
        labelCount = sortIntHashMap(labelCount);
        Iterator<Entry<String, Integer>> iter = labelCount.entrySet().iterator();
        mostFrequentLabels.put(word,iter.next().getKey());
        labelCount.clear();

        //There are some ties. The following values have been experimentally determined to improve performance.

        //mostFrequentLabels.put("write.v", "001000000");
        mostFrequentLabels.put("write.v", "000000001");

        //mostFrequentLabels.put("simple.a", "00100000");
        mostFrequentLabels.put("simple.a", "00010000");

        //mostFrequentLabels.put("difficulty.n", "01000");
        mostFrequentLabels.put("difficulty.n", "00010");

	}

	private static void buildPrecisionTable(String filename) {
	    ObjectSerializer<HashMap<String, LinkedHashMap<String, Double>>> serializer = new ObjectSerializer<HashMap<String, LinkedHashMap<String, Double>>>();
	    /*precisionTable = serializer.readObject(filename);
	    if(precisionTable != null)
	        return;*/
	    tfTable = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();
	    dfTable = new HashMap<String, Integer>();


	    System.out.println("Building Precision Table");
	    HashMap<String, Integer> frequencyTable = new HashMap<String, Integer>();
	    LinkedHashMap<String, Integer> curMap;
	    String label = "", key = "";
	    String word = "";
        for(String line : trainDoc) {
            label = getSense(line);
            word = getWord(line);

            if(key.equals(""))
                key = word + label;
            else if(!key.equals(word + label)) {
                if(!tfTable.containsKey(key))
                    curMap = new LinkedHashMap<String, Integer>();
                else {
                    curMap = tfTable.get(key);
                    System.err.println("WTF");
                    System.exit(0);
                }

                //for(Entry<String, Integer> entry : frequencyTable.entrySet())
                //    curMap.put(entry.getKey(), frequencyTable.get(entry.getKey()) / (double)countWords.get(entry.getKey()));

                System.out.println("Sorting Map:" + key);
                curMap = sortIntHashMap(frequencyTable);

                tfTable.put(key, curMap);

                key = word + label;
                frequencyTable.clear();
            }


            if(windowSize > 0) {
                String sentence = getSentence(sentenceDetector.sentDetect(line));
                ArrayList<String> words = getWords(sentence);
                int i = 0;
                for(;i < words.size();i++){
                    if(words.get(i).startsWith("@") && words.get(i).endsWith("@"))
                        break;
                }

                //right window
                for(int j = i; j < words.size() -1 && j <= i+(windowSize/2); ++j){
                    if(!isWord(words.get(j)) || commonWords.contains(words.get(j).toLowerCase()))
                        continue;
                    if(!frequencyTable.containsKey(words.get(j).toLowerCase())) {
                        frequencyTable.put(words.get(j).toLowerCase(), 1);
                        if(!dfTable.containsKey(words.get(j)))
                            dfTable.put(words.get(j), 1);
                        else
                            dfTable.put(words.get(j), dfTable.get(words.get(j)) + 1);
                    } else
                        frequencyTable.put(words.get(j).toLowerCase(), frequencyTable.get(words.get(j).toLowerCase())+1);
                }

                //left window
                for(int j = i; j >= 0 && j >= i-(windowSize/2); --j){
                    if(!isWord(words.get(j)) || commonWords.contains(words.get(j).toLowerCase()))
                        continue;
                    if(!frequencyTable.containsKey(words.get(j).toLowerCase())) {
                        frequencyTable.put(words.get(j).toLowerCase(), 1);
                        if(!dfTable.containsKey(words.get(j)))
                            dfTable.put(words.get(j), 1);
                        else
                            dfTable.put(words.get(j), dfTable.get(words.get(j)) + 1);
                    } else
                        frequencyTable.put(words.get(j).toLowerCase(), frequencyTable.get(words.get(j).toLowerCase())+1);
                }
            } else {
                ArrayList<String> words = getWords(line);
                for(String curWord : words) {
                    if(!isWord(curWord))
                        continue;
                    if(!frequencyTable.containsKey(curWord.toLowerCase())) {
                        frequencyTable.put(curWord.toLowerCase(), 1);
                        if(!dfTable.containsKey(curWord))
                            dfTable.put(curWord, 1);
                        else
                            dfTable.put(curWord, dfTable.get(curWord) + 1);
                    } else
                        frequencyTable.put(curWord.toLowerCase(), frequencyTable.get(curWord.toLowerCase())+1);
                }
            }
        }
        //serializer.writeObject(precisionTable, filename);

        System.out.println("Calculating tf.idfs");
        tfidfTable = new LinkedHashMap<String, LinkedHashMap<String, Double>>();
        LinkedHashMap<String, Double> map;
        for(Entry<String, LinkedHashMap<String, Integer>> entry1 : tfTable.entrySet()) {
            map = new LinkedHashMap<String, Double>();
            for(Entry<String, Integer> entry2 : entry1.getValue().entrySet()) {
                map.put(entry2.getKey(), (1+Math.log10(entry2.getValue())) * Math.log10((double)tfTable.size() / dfTable.get(entry2.getKey())));
            }
            map = sortDoubleHashMap(map);
            tfidfTable.put(entry1.getKey(),map);
        }
	}

	private static void countWords() {
	    System.out.println("Counting Words");
		countWords = new HashMap<String, Integer>();
		for(String line : trainDoc){
			ArrayList<String> words = getWords(line);
			int i = 0;
			for(;i < words.size(); i++){
				if(words.get(i).equals("@")){
					i++;
					break;
				}
			}
			for(; i < words.size(); i++){

				if(!isWord(words.get(i)))
					continue;

				if(words.get(i).startsWith("@") && words.get(i).endsWith("@"))
					continue;
				if(!countWords.containsKey(words.get(i).toLowerCase()))
				    countWords.put(words.get(i).toLowerCase(), 1);
				else
				    countWords.put(words.get(i).toLowerCase(), countWords.get(words.get(i).toLowerCase())+1);
			}
		}
	}

	/*private static void filterCommonWords(double percent) {
	       commonWords = sortIntHashMap(commonWords);
	        HashMap<String, Integer> tmp = new HashMap<String, Integer>();
	        Iterator<Entry<String, Integer>> it = commonWords.entrySet().iterator();
	        int topPerc = (int) (commonWords.size() * percent);

	        for(int i = 0; i < topPerc && it.hasNext(); i++){
	            Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>)it.next();
	            tmp.put(pairs.getKey(), pairs.getValue());
	        }

	        commonWords = tmp;
	        System.out.println(commonWords.size() + " common words found: \n ===> " + commonWords);
	}*/

	public static void handleModel(String model){
		ArrayList<String> featVector = getFeatures(model);

		System.out.println(model);
		createARFF(featVector, model, true);
		createARFF(featVector, model, false);
	}

	public static ArrayList<String> getFeatures(String model) {
	    HashSet<String> features = new HashSet<String>();
	    for(Entry<String, LinkedHashMap<String, Double>> entry1 : tfidfTable.entrySet()) {
	        if(!entry1.getKey().startsWith(model))
	            continue;
	        int count = 0;
	        for(Entry<String, Double> entry2 : entry1.getValue().entrySet()) {
	            if(commonWords.contains(entry2.getKey()))
	                continue;
	            //if(++count > NUM_FEATURES_PER_SENSE)
	            //    break;
	            if(entry2.getValue() < 2.2)
	                break;
	            features.add(entry2.getKey());
	        }
	    }
	    ArrayList<String> returnList = new ArrayList<String>();
	    for(String feature : features)
	        returnList.add(feature);
	    return returnList;
	}

	public static void createARFF(ArrayList<String> featVector, String model, boolean training){
		try {
			HashSet<String> senses = new HashSet<String>();
			String path = (training)? "trainData/" : "testData/";
			FileWriter outFile = new FileWriter(path + model + ".arff");
			PrintWriter out = new PrintWriter(outFile);

			out.println("@RELATION " + model + "\n");

			for(String line : trainDoc){
				String curWord = getWord(line);
				if(curWord.equals(model)){
					senses.add(getSense(line));
				}
			}

			Iterator<String> sense_it = senses.iterator();
			int senseSize = sense_it.next().length();
			String allZero = "";
			for(int i = 0; i < senseSize; i++)
				allZero += "0";
			senses.add(allZero);

			out.println("\n@ATTRIBUTE sense " + senses.toString().replace("[", "{").replace("]", "}"));

			for(String feature: featVector)
				out.println("@ATTRIBUTE #" + feature + "{0,1}");



			out.println("\n@DATA");
			for(String line : ((training)? trainDoc : testDoc)){
				String curWord = getWord(line);
				if(curWord.equals(model)){
				    ArrayList<String> valueVector = new ArrayList<String>();
				    String sense = getSense(line);
				    /*int count = 0;
				    for(int i = 0; i < sense.length(); i++) {
				        if(sense.charAt(i) != '0')
				            count++;
				    }
				    if(count > 1)
				        continue;*/
				    valueVector.add(sense);

					//String sentence = getSentence(sentenceDetector.sentDetect(line));
					ArrayList<String> words = getWords(line);//sentence);

					boolean commit = false;
					for(int i = 0; i < featVector.size(); i++) {
					    if(i < featVector.size()){
	                        if(words.contains(featVector.get(i).toLowerCase())) {
	                            valueVector.add("1");
	                            commit = true;
	                        } else
	                            valueVector.add("0");
	                    }
					}

					if(!commit)
					    valueVector.set(0, mostFrequentLabels.get(curWord));

					//if(commit || !training)
					    out.print(valueVector.toString().substring(1, valueVector.toString().length()-1) + "\n");
				}
			}
			if(training) {
			    ArrayList<String> valueVector = new ArrayList<String>();
			    valueVector.add(allZero);
			    for(String feature : featVector)
			        valueVector.add("0");
			    out.print(valueVector.toString().substring(1, valueVector.toString().length()-1) + "\n");
			}

			outFile.close();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getSense(String line){
		String sense = "";
		ArrayList<String> words = getWords(line);

		for(int i = 1; i < words.size(); i++){
			if(!words.get(i).equals("@"))
				sense += words.get(i);
			else
				break;
		}
		return sense;
	}

	public static void loadModels(){
	    System.out.println("Loading Models");
		models = new ArrayList<String>();
		for(String line : trainDoc){
			ArrayList<String> words = getWords(line);
			if(!models.contains(words.get(0)))
				models.add(words.get(0));
		}
		Collections.sort(models);
	}

	public static boolean isWord(String word){
		for(int i = 0; i < word.length(); i++){
			if(!Character.isLetter(word.charAt(i)))
				return false;
		}
		return true;
	}

	public static String getWord(String line){
		String word = "";

		for(int i=0; i < line.length(); i++){
			if(line.charAt(i) != ' ')
				word += line.charAt(i);
			else
				break;
		}
		return word;
	}
	public static String getSentence(String sentences[]){
		for(String sentence : sentences){
			ArrayList<String> words = getWords(sentence);
			for(int i = 0; i < words.size(); i++){
				if(words.get(i).startsWith("@") && words.get(i).endsWith("@") && words.get(i).length() > 1)
					return sentence;
			}
		}
		return null;
	}

	public static LinkedHashMap<String, Integer> sortIntHashMap(HashMap<String, Integer> passedMap) {
		List<String> mapKeys = new ArrayList<String>(passedMap.keySet());
		List<Integer> mapValues = new ArrayList<Integer>(passedMap.values());
		Collections.sort(mapValues);
		Collections.reverse(mapValues);

		Collections.sort(mapKeys);
		Collections.reverse(mapKeys);

		LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();

		Iterator<Integer> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			Object val = valueIt.next();
			Iterator<String> keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				Object key = keyIt.next();
				String comp1 = passedMap.get(key).toString();
				String comp2 = val.toString();

				if (comp1.equals(comp2)){
					passedMap.remove(key);
					mapKeys.remove(key);
					sortedMap.put((String)key, (Integer)val);
					break;
				}

			}

		}
		return sortedMap;
	}

	public static LinkedHashMap<String, Double> sortDoubleHashMap(HashMap<String, Double> passedMap) {
        List<String> mapKeys = new ArrayList<String>(passedMap.keySet());
        List<Double> mapValues = new ArrayList<Double>(passedMap.values());
        Collections.sort(mapValues);
        Collections.reverse(mapValues);

        Collections.sort(mapKeys);
        Collections.reverse(mapKeys);

        LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<String, Double>();

        Iterator<Double> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                String comp1 = passedMap.get(key).toString();
                String comp2 = val.toString();

                if (comp1.equals(comp2)){
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put((String)key, (Double)val);
                    break;
                }

            }

        }
        return sortedMap;
    }

	public static ArrayList<String> loadDoc(String filename) {
		System.out.println("Reading File:" + filename);
		Scanner scanner = null;
		ArrayList<String> sentences = new ArrayList<String>();
		try {
			scanner = new Scanner(new FileInputStream(filename));

			while (scanner.hasNextLine())
				sentences.add(scanner.nextLine() + " ");

		} catch (FileNotFoundException e) {
			System.out.println("File not found:" + filename);
			System.exit(1);
		}

		System.out.println("Done reading file: " + filename);
		//trainDoc = sentences;
		return sentences;
	}

	public static ArrayList<String> getWords(String line) {
		if(line == null) return null;
		ArrayList<String> words = new ArrayList<String>(Arrays.asList(line.split(" |\n")));
		while(words.remove("")); //Eliminate multiple consecutive spaces
		for(int i = 0; i < words.size(); i++)
		    words.set(i, words.get(i).toLowerCase());
		return words;
	}

	public static class LineComparator implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            String s1word = getWord(s1);
            String s2word = getWord(s2);
            int compare = s1word.compareTo(s2word);
            if(compare == 0) {
                int s1Sense = Integer.parseInt(getSense(s1), 2);
                int s2Sense = Integer.parseInt(getSense(s2), 2);
                return s1Sense - s2Sense;
            } else
                return compare;
        }

	}

	public static void loadCommonWords(){
		commonWords = 					new HashSet<String>();
		commonWords.add("the");			commonWords.add("be");
		commonWords.add("to");			commonWords.add("of");
		commonWords.add("and");			commonWords.add("a");
		commonWords.add("in");			commonWords.add("that");
		commonWords.add("have");		commonWords.add("i");
		commonWords.add("for");			commonWords.add("not");
		commonWords.add("on");			commonWords.add("with");
		commonWords.add("he");			commonWords.add("as");
		commonWords.add("you");			commonWords.add("do");
		commonWords.add("this");		commonWords.add("but");
		commonWords.add("his");			commonWords.add("by");
		commonWords.add("from");		commonWords.add("they");
		commonWords.add("we");			commonWords.add("say");
		commonWords.add("her");			commonWords.add("she");
		commonWords.add("or");			commonWords.add("an");
		commonWords.add("will");		commonWords.add("my");
		commonWords.add("all");			commonWords.add("would");
		commonWords.add("there");		commonWords.add("their");
		commonWords.add("what");		commonWords.add("so");
		commonWords.add("up");			commonWords.add("out");
		commonWords.add("if");			commonWords.add("about");
		commonWords.add("who");			commonWords.add("get");
		commonWords.add("which");		commonWords.add("go");
		commonWords.add("me");			commonWords.add("the");
		commonWords.add("is");			commonWords.add("are");
		commonWords.add("it");			commonWords.add("when");
		commonWords.add("can");			commonWords.add("was");
		commonWords.add("only");		commonWords.add("its");
		commonWords.add("at");			commonWords.add("has");
		commonWords.add("also");		commonWords.add("them");
		commonWords.add("same");		commonWords.add("our");
		commonWords.add("had");			commonWords.add("then");
		commonWords.add("than");		commonWords.add("any");
		commonWords.add("when");		commonWords.add("were");
		commonWords.add("way");			commonWords.add("yet");
		commonWords.add("he");			commonWords.add("this");
		commonWords.add("it");			commonWords.add("does");
		commonWords.add("could");		commonWords.add("must");
		commonWords.add("no");			commonWords.add("against");
		commonWords.add("more");		commonWords.add("does");
		commonWords.add("in");			commonWords.add("his");
		commonWords.add("upon");		commonWords.add("very");
		commonWords.add("been");		commonWords.add("some");
		commonWords.add("too");			commonWords.add("into");
		commonWords.add("and");			commonWords.add("us");
		commonWords.add("him");			commonWords.add("may");
		commonWords.add("over");		commonWords.add("why");
		commonWords.add("too");			commonWords.add("own");
		commonWords.add("over");		commonWords.add("many");
		commonWords.add("because");		commonWords.add("before");
		commonWords.add("like");		commonWords.add("into");
		commonWords.add("said");		commonWords.add("these");
		commonWords.add("less");		commonWords.add("much");
		commonWords.add("new");			commonWords.add("each");
		commonWords.add("your");		commonWords.add("just");
		commonWords.add("but");			commonWords.add("there");
		commonWords.add("at");			commonWords.add("you");
		commonWords.add("see");			commonWords.add("should");
		commonWords.add("how");			commonWords.add("bye");
		commonWords.add("your");		commonWords.add("soon");
		commonWords.add("a");			commonWords.add("did");
		commonWords.add("such");		commonWords.add("those");
		commonWords.add("what");		commonWords.add("as");
		commonWords.add("she");			commonWords.add("by");
	}
}
