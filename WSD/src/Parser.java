package src;

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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

public class Parser {

	public static ArrayList<String> trainDoc = null;
	public static ArrayList<String> testDoc = null;
	public static ArrayList<String> models = null;
//	public static HashSet<String> commonWords = null;
	public static HashMap<String, Integer> commonWords = null;
	public static InputStream modelIn = null;
	public static SentenceModel sm = null;
	public static SentenceDetectorME sentenceDetector = null;
	public static HashMap<String, LinkedHashMap<String, Double>> precisionTable;
	public static HashMap<String, Integer> cumulativeFrequency = new HashMap<String, Integer>();

	public static int windowSize = 14;
	public static int NUM_FEATURES_PER_SENSE = 1;

	public static void main(String[] args) {
		trainDoc = loadDoc("wsd-data/train.data");
		Collections.sort(trainDoc, new LineComparator());
		testDoc = loadDoc("wsd-data/test.data");
		initSentenceDetector();
		loadModels();
		loadCommonWords();
		buildPrecisionTable("precision.map");
		for(String model : models)
			handleModel(model);
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

	private static void buildPrecisionTable(String filename) {
	    ObjectSerializer<HashMap<String, LinkedHashMap<String, Double>>> serializer = new ObjectSerializer<HashMap<String, LinkedHashMap<String, Double>>>();
	    precisionTable = serializer.readObject(filename);
	    if(precisionTable != null)
	        return;
	    precisionTable = new LinkedHashMap<String, LinkedHashMap<String, Double>>();

	    System.out.println("Building Precision Table");
	    HashMap<String, Integer> frequencyTable = new HashMap<String, Integer>();
	    LinkedHashMap<String, Double> curMap;
	    String label = "", word = "", key = "";
        for(String line : trainDoc) {
            label = getSense(line);
            word = getWord(line);

            if(key.equals(""))
                key = word + label;
            else if(!key.equals(word + label)) {
                key = word + label;

                if(!precisionTable.containsKey(key))
                    curMap = new LinkedHashMap<String, Double>();
                else {
                    curMap = precisionTable.get(key);
                    System.err.println("WTF");
                    System.exit(0);
                }

                for(Entry<String, Integer> entry : frequencyTable.entrySet())
                    curMap.put(entry.getKey(), frequencyTable.get(entry.getKey()) / (double)commonWords.get(entry.getKey()));

                System.out.println("Sorting Map:" + key);
                curMap = sortDoubleHashMap(curMap);

                precisionTable.put(key, curMap);
                frequencyTable.clear();
            }


            ArrayList<String> words = getWords(line);
            for(String curWord : words) {
                if(!isWord(curWord))
                    continue;

                if(!frequencyTable.containsKey(curWord))
                    frequencyTable.put(curWord, 1);
                else
                    frequencyTable.put(curWord, frequencyTable.get(curWord)+1);
            }
        }
        serializer.writeObject(precisionTable, filename);
	}

	private static void loadCommonWords() {
	    System.out.println("Loading Common Words");
		commonWords = new HashMap<String, Integer>();
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
				if(!commonWords.containsKey(words.get(i)))
					commonWords.put(words.get(i), 1);
				else
					commonWords.put(words.get(i), commonWords.get(words.get(i))+1);
			}
		}
	}

	private static void filterCommonWords(double percent) {
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
	}

	public static void handleModel(String model){
		ArrayList<String> featVector = getFeatures(model);

		System.out.println(model);
		createARFF(featVector, model, true);
		createARFF(featVector, model, false);
	}

	public static ArrayList<String> getFeatures(String model) {
	    HashSet<String> features = new HashSet<String>();
	    for(Entry<String, LinkedHashMap<String, Double>> entry1 : precisionTable.entrySet()) {
	        if(!entry1.getKey().startsWith(model))
	            continue;
	        int count = 0;
	        for(Entry<String, Double> entry2 : entry1.getValue().entrySet()) {
	            if(++count > NUM_FEATURES_PER_SENSE)
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
				    int count = 0;
				    for(int i = 0; i < sense.length(); i++) {
				        if(sense.charAt(i) != '0')
				            count++;
				    }
				    if(count > 1)
				        continue;
				    valueVector.add(sense);

					//String sentence = getSentence(sentenceDetector.sentDetect(line));
					ArrayList<String> words = getWords(line);//sentence);

					boolean commit = false;
					for(int i = 0; i < featVector.size(); i++) {
					    if(i < featVector.size()){
	                        if(words.contains(featVector.get(i))) {
	                            valueVector.add("1");
	                            commit = true;
	                        } else
	                            valueVector.add("0");
	                    }
					}

					if(!commit)
					    valueVector.set(0, allZero);

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

	/*public static HashMap<String, Integer> getHashMap(String curModel){
		System.out.println(curModel);
		try {
			HashMap<String, Integer> occ = new HashMap<String, Integer>();
			modelIn = new FileInputStream("models/en-sent.bin");
			sm = new SentenceModel(modelIn);
			sentenceDetector = new SentenceDetectorME(sm);
			for(String line : trainDoc){
				String target = getWord(line);
				if(target.equals(curModel)){
					String sentence = getSentence(sentenceDetector.sentDetect(line));
					ArrayList<String> words = getWords(sentence);
					int i =0;
					for(;i < words.size();i++){
						if(words.get(i).startsWith("@") && words.get(i).endsWith("@"))
							break;
					}

					//right window
					for(int j = i; j < words.size() -1 && j <= i+(windowSize/2); ++j){
						if(!isWord(words.get(j)) || commonWords.containsKey(words.get(j)))
							continue;
						if(!occ.containsKey(words.get(j)))
							occ.put(words.get(j), 1);
						else
							occ.put(words.get(j), occ.get(words.get(j))+1);
					}

					//left window
					for(int j = i; j >= 0 && j >= i-(windowSize/2); --j){
						if(!isWord(words.get(j)) || commonWords.containsKey(words.get(j)))
							continue;
						if(!occ.containsKey(words.get(j)))
							occ.put(words.get(j), 1);
						else
							occ.put(words.get(j), occ.get(words.get(j))+1);
					}
				}
			}
			modelIn.close();
			return sortIntHashMap(occ);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}*/

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

//	public static void loadCommonWords(){
//		commonWords = 					new HashSet<String>();
//		commonWords.add("the");			commonWords.add("be");
//		commonWords.add("to");			commonWords.add("of");
//		commonWords.add("and");			commonWords.add("a");
//		commonWords.add("in");			commonWords.add("that");
//		commonWords.add("have");		commonWords.add("I");
//		commonWords.add("for");			commonWords.add("not");
//		commonWords.add("on");			commonWords.add("with");
//		commonWords.add("he");			commonWords.add("as");
//		commonWords.add("you");			commonWords.add("do");
//		commonWords.add("this");		commonWords.add("but");
//		commonWords.add("his");			commonWords.add("by");
//		commonWords.add("from");		commonWords.add("they");
//		commonWords.add("we");			commonWords.add("say");
//		commonWords.add("her");			commonWords.add("she");
//		commonWords.add("or");			commonWords.add("an");
//		commonWords.add("will");		commonWords.add("my");
//		commonWords.add("all");			commonWords.add("would");
//		commonWords.add("there");		commonWords.add("their");
//		commonWords.add("what");		commonWords.add("so");
//		commonWords.add("up");			commonWords.add("out");
//		commonWords.add("if");			commonWords.add("about");
//		commonWords.add("who");			commonWords.add("get");
//		commonWords.add("which");		commonWords.add("go");
//		commonWords.add("me");			commonWords.add("The");
//		commonWords.add("is");			commonWords.add("are");
//		commonWords.add("it");			commonWords.add("when");
//		commonWords.add("can");			commonWords.add("was");
//		commonWords.add("only");		commonWords.add("its");
//		commonWords.add("at");			commonWords.add("has");
//		commonWords.add("also");		commonWords.add("them");
//		commonWords.add("same");		commonWords.add("our");
//		commonWords.add("had");			commonWords.add("then");
//		commonWords.add("than");		commonWords.add("any");
//		commonWords.add("When");		commonWords.add("were");
//		commonWords.add("way");			commonWords.add("yet");
//		commonWords.add("He");			commonWords.add("This");
//		commonWords.add("It");			commonWords.add("does");
//		commonWords.add("could");		commonWords.add("must");
//		commonWords.add("no");			commonWords.add("against");
//		commonWords.add("more");		commonWords.add("does");
//		commonWords.add("In");			commonWords.add("His");
//		commonWords.add("upon");		commonWords.add("very");
//		commonWords.add("been");		commonWords.add("some");
//		commonWords.add("too");			commonWords.add("into");
//		commonWords.add("And");			commonWords.add("us");
//		commonWords.add("him");			commonWords.add("may");
//		commonWords.add("over");		commonWords.add("why");
//		commonWords.add("too");			commonWords.add("own");
//		commonWords.add("over");		commonWords.add("many");
//		commonWords.add("because");		commonWords.add("before");
//		commonWords.add("like");		commonWords.add("into");
//		commonWords.add("said");		commonWords.add("these");
//		commonWords.add("less");		commonWords.add("much");
//		commonWords.add("new");			commonWords.add("each");
//		commonWords.add("your");		commonWords.add("just");
//		commonWords.add("But");			commonWords.add("There");
//		commonWords.add("At");			commonWords.add("You");
//		commonWords.add("see");			commonWords.add("should");
//		commonWords.add("how");			commonWords.add("Bye");
//		commonWords.add("your");		commonWords.add("soon");
//		commonWords.add("A");			commonWords.add("did");
//		commonWords.add("such");		commonWords.add("those");
//		commonWords.add("What");		commonWords.add("As");
//		commonWords.add("She");			commonWords.add("By");
//	}
}
