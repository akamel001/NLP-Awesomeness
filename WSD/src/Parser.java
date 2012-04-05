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
import java.util.Map.Entry;
import java.util.Scanner;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

public class Parser {

	public static ArrayList<String> trainDoc = null;
	public static ArrayList<String> testDoc = null;
	public static ArrayList<String> models = null;
	public static HashSet<String> stopWords = null;
	public static HashMap<String, Integer> countWords = null;
	public static InputStream modelIn = null;
	public static SentenceModel sm = null;
	public static SentenceDetectorME sentenceDetector = null;
	public static HashMap<String, Integer> dfTable = null;
	public static HashMap<String, LinkedHashMap<String, Integer>> tfTable = null;
	public static HashMap<String, LinkedHashMap<String, Double>> tfidfTable = null;
	public static HashMap<String, String> mostFrequentLabels = null;

	public static int WINDOW_SIZE = 7; //How many words around the @TARGET@ word to index
	public static double MIN_RELEVANCY = 2.2; //The minimum TF.IDF score for a word to be included as a feature

	public static String getWord2(String word) {
	    String result = "";
	    for(int i = 0; i < word.length(); i++) {
	        if(Character.isDigit(word.charAt(i)))
	            break;
	        result += word.charAt(i);
	    }
	    System.out.println(result);
	    return result;
	}

	public static void main(String[] args) {
		trainDoc = loadDoc("wsd-data/train.data");
		Collections.sort(trainDoc, new LineComparator());
		testDoc = loadDoc("wsd-data/test.data");
		initSentenceDetector();
		loadStopWords();
		buildMostFrequentLabelsTable();
		loadModels();
		buildTFIDFTable();

		System.out.println("Generating ARFF Files:");
		for(String model : models)
			handleModel(model);
	}

	/**
	 * Prints most relevant features for use in report
	 */
	private static void printReleventFeatures() {
        String word = null;
        HashMap<String, LinkedHashMap<String, Double>> newMap = new HashMap<String, LinkedHashMap<String, Double>>();
        LinkedHashMap<String, Double> curMap = new LinkedHashMap<String, Double>();
        for(Entry<String, LinkedHashMap<String, Double>> entry1 : tfidfTable.entrySet()) {
            String newWord = getWord2(entry1.getKey());
            if(!newWord.equals(word) && word != null) {
                newMap.put(word, curMap);
                curMap = new LinkedHashMap<String, Double>();
            }
            word = newWord;
            for(Entry<String, Double> entry2 : entry1.getValue().entrySet()) {
                if(curMap.containsKey(entry2.getKey()))
                    curMap.put(entry2.getKey(), Math.max(entry2.getValue(), curMap.get(entry2.getKey())));
                else
                    curMap.put(entry2.getKey(),entry2.getValue());
            }
        }
        newMap.put(word, curMap);

        for(Entry<String, LinkedHashMap<String, Double>> entry : newMap.entrySet()) {
            int count = 0;
            Iterator<Entry<String, Double>> iter = entry.getValue().entrySet().iterator();
            while(iter.hasNext() && count < 3) {
                Entry<String, Double> next = iter.next();
                System.out.println(entry.getKey() + "," + next.getKey() + "," + next.getValue() );
                count++;
            }
        }
	}

	/**
	 * Initialize the OpenNLP sentence detector
	 */
	private static void initSentenceDetector() {
	    try {
    	    modelIn = new FileInputStream("models/en-sent.bin");
            sm = new SentenceModel(modelIn);
            sentenceDetector = new SentenceDetectorME(sm);
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * Increment a hashmap of integers
	 */
	private static void incrementMap(HashMap<String, Integer> map, String key) {
	    map.put(key, map.containsKey(key) ? map.get(key) + 1 : 1);
	}

	/**
	 * Build a hashmap that contains the labels most frequently assigned to a word
	 */
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

        //Some labels tie for the highest label count.
        //The following values have been experimentally determined to improve performance.

        //mostFrequentLabels.put("write.v", "001000000");
        mostFrequentLabels.put("write.v", "000000001");

        //mostFrequentLabels.put("simple.a", "00100000");
        mostFrequentLabels.put("simple.a", "00010000");

        //mostFrequentLabels.put("difficulty.n", "01000");
        mostFrequentLabels.put("difficulty.n", "00010");

	}

	/**
	 * Builds a table of tf.idf values for each term-document pair
	 * @param filename Filename to store the serialized table in
	 */
	private static void buildTFIDFTable() {
	    tfTable = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();
	    dfTable = new HashMap<String, Integer>();


	    System.out.println("Building term frequency table");
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

                System.out.println("Indexing Document:" + key);
                curMap = sortIntHashMap(frequencyTable);

                tfTable.put(key, curMap);

                key = word + label;
                frequencyTable.clear();
            }

            //If the window-size is above zero, only check words within range
            if(WINDOW_SIZE > 0) {
                String sentence = getSentence(sentenceDetector.sentDetect(line));
                ArrayList<String> words = getWords(sentence);
                int i = 0;
                for(;i < words.size();i++){
                    if(words.get(i).startsWith("@") && words.get(i).endsWith("@"))
                        break;
                }

                //right window
                for(int j = i; j < words.size() -1 && j <= i+(WINDOW_SIZE/2); ++j){
                    if(!isWord(words.get(j)) || stopWords.contains(words.get(j).toLowerCase()))
                        continue;
                    if(!frequencyTable.containsKey(words.get(j).toLowerCase())) {
                        frequencyTable.put(words.get(j).toLowerCase(), 1);
                        incrementMap(dfTable, words.get(j));
                    } else
                        frequencyTable.put(words.get(j).toLowerCase(), frequencyTable.get(words.get(j).toLowerCase())+1);
                }

                //left window
                for(int j = i; j >= 0 && j >= i-(WINDOW_SIZE/2); --j){
                    if(!isWord(words.get(j)) || stopWords.contains(words.get(j).toLowerCase()))
                        continue;
                    if(!frequencyTable.containsKey(words.get(j).toLowerCase())) {
                        frequencyTable.put(words.get(j).toLowerCase(), 1);
                        incrementMap(dfTable, words.get(j));
                    } else
                        frequencyTable.put(words.get(j).toLowerCase(), frequencyTable.get(words.get(j).toLowerCase())+1);
                }
            } else {
                // If windowsize is 0 or less, use all words.
                ArrayList<String> words = getWords(line);
                for(String curWord : words) {
                    if(!isWord(curWord))
                        continue;
                    if(!frequencyTable.containsKey(curWord.toLowerCase())) {
                        frequencyTable.put(curWord.toLowerCase(), 1);
                        incrementMap(dfTable, curWord);
                    } else
                        frequencyTable.put(curWord.toLowerCase(), frequencyTable.get(curWord.toLowerCase())+1);
                }
            }
        }

        System.out.println("Calculating tf.idfs");
        tfidfTable = new LinkedHashMap<String, LinkedHashMap<String, Double>>();
        LinkedHashMap<String, Double> map;
        for(Entry<String, LinkedHashMap<String, Integer>> entry1 : tfTable.entrySet()) {
            map = new LinkedHashMap<String, Double>();
            for(Entry<String, Integer> entry2 : entry1.getValue().entrySet()) {
                //Calculate tf.idf weight
                map.put(entry2.getKey(), (1+Math.log10(entry2.getValue())) * Math.log10((double)tfTable.size() / dfTable.get(entry2.getKey())));
            }
            map = sortDoubleHashMap(map);
            tfidfTable.put(entry1.getKey(),map);
        }
	}

	/**
	 * Creates ARFF file for a given word
	 * @param model
	 */
	public static void handleModel(String model){
		ArrayList<String> featVector = getFeatures(model);

		System.out.println(model);
		createARFF(featVector, model, true);
		createARFF(featVector, model, false);
	}

	/**
	 *
	 * @param model
	 * @return
	 */
	public static ArrayList<String> getFeatures(String model) {
	    HashSet<String> features = new HashSet<String>();
	    for(Entry<String, LinkedHashMap<String, Double>> entry1 : tfidfTable.entrySet()) {
	        if(!entry1.getKey().startsWith(model))
	            continue;
	        int count = 0;
	        for(Entry<String, Double> entry2 : entry1.getValue().entrySet()) {
	            if(stopWords.contains(entry2.getKey()))
	                continue;
	            if(entry2.getValue() < MIN_RELEVANCY)
	                break;
	            features.add(entry2.getKey());
	        }
	    }
	    ArrayList<String> returnList = new ArrayList<String>();
	    for(String feature : features)
	        returnList.add(feature);
	    return returnList;
	}

	/**
	 * Generates an ARFF file for word
	 * @param featVector The features to be used
	 * @param model - The word
	 * @param training - Whether we are building a training or test file
	 */
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
				    valueVector.add(sense);

					ArrayList<String> words = getWords(line);

					boolean hasFeature = false;
					for(int i = 0; i < featVector.size(); i++) {
					    if(i < featVector.size()){
	                        if(words.contains(featVector.get(i).toLowerCase())) {
	                            valueVector.add("1");
	                            hasFeature = true;
	                        } else
	                            valueVector.add("0");
	                    }
					}

					if(!hasFeature)
					    valueVector.set(0, mostFrequentLabels.get(curWord));

					out.print(valueVector.toString().substring(1, valueVector.toString().length()-1) + "\n");
				}
			}

			outFile.close();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return the word-sense label of this line
	 */
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

	/**
	 * Returns a list of all words to train on
	 */
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

	/**
	 * Determines if something is a word
	 */
	public static boolean isWord(String word){
		for(int i = 0; i < word.length(); i++){
			if(!Character.isLetter(word.charAt(i)))
				return false;
		}
		return true;
	}

	/**
	 * Returns the word from a line
	 */
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

	/**
     * Returns the sentence that contains the @TARGET@ word from a list of sentences
	 */
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

	/**
	 * Sorts a hashmap of integers
	 * @return
	 */
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

	/**
	 * Sorts a hashmap of doubles.
	 * NB: I would have liked to make this code more DRY, but this function has the same
	 * erasure as the one above it when I give them the same name. This is a quick hack to make it work.
	 */
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

	/**
	 * Loads a wsd-data file into an array list
	 */
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

	/**
	 * Returns a list of the words in a line, in lowercase
	 * @return
	 */
	public static ArrayList<String> getWords(String line) {
		if(line == null) return null;
		ArrayList<String> words = new ArrayList<String>(Arrays.asList(line.split(" |\n")));
		while(words.remove("")); //Eliminate multiple consecutive spaces
		for(int i = 0; i < words.size(); i++)
		    words.set(i, words.get(i).toLowerCase());
		return words;
	}

	/**
	 * Used to sort the wsd-data
	 */
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

	/**
	 * Loads stop words, for exclusion
	 */
	public static void loadStopWords(){
		stopWords = 				new HashSet<String>();
		stopWords.add("the");		stopWords.add("be");
		stopWords.add("to");		stopWords.add("of");
		stopWords.add("and");		stopWords.add("a");
		stopWords.add("in");		stopWords.add("that");
		stopWords.add("have");		stopWords.add("i");
		stopWords.add("for");		stopWords.add("not");
		stopWords.add("on");		stopWords.add("with");
		stopWords.add("he");		stopWords.add("as");
		stopWords.add("you");		stopWords.add("do");
		stopWords.add("this");		stopWords.add("but");
		stopWords.add("his");		stopWords.add("by");
		stopWords.add("from");		stopWords.add("they");
		stopWords.add("we");		stopWords.add("say");
		stopWords.add("her");		stopWords.add("she");
		stopWords.add("or");		stopWords.add("an");
		stopWords.add("will");		stopWords.add("my");
		stopWords.add("all");		stopWords.add("would");
		stopWords.add("there");		stopWords.add("their");
		stopWords.add("what");		stopWords.add("so");
		stopWords.add("up");		stopWords.add("out");
		stopWords.add("if");		stopWords.add("about");
		stopWords.add("who");		stopWords.add("get");
		stopWords.add("which");		stopWords.add("go");
		stopWords.add("me");		stopWords.add("the");
		stopWords.add("is");		stopWords.add("are");
		stopWords.add("it");		stopWords.add("when");
		stopWords.add("can");		stopWords.add("was");
		stopWords.add("only");		stopWords.add("its");
		stopWords.add("at");		stopWords.add("has");
		stopWords.add("also");		stopWords.add("them");
		stopWords.add("same");		stopWords.add("our");
		stopWords.add("had");		stopWords.add("then");
		stopWords.add("than");		stopWords.add("any");
		stopWords.add("when");		stopWords.add("were");
		stopWords.add("way");		stopWords.add("yet");
		stopWords.add("he");		stopWords.add("this");
		stopWords.add("it");		stopWords.add("does");
		stopWords.add("could");		stopWords.add("must");
		stopWords.add("no");		stopWords.add("against");
		stopWords.add("more");		stopWords.add("does");
		stopWords.add("in");		stopWords.add("his");
		stopWords.add("upon");		stopWords.add("very");
		stopWords.add("been");		stopWords.add("some");
		stopWords.add("too");		stopWords.add("into");
		stopWords.add("and");		stopWords.add("us");
		stopWords.add("him");		stopWords.add("may");
		stopWords.add("over");		stopWords.add("why");
		stopWords.add("too");		stopWords.add("own");
		stopWords.add("over");		stopWords.add("many");
		stopWords.add("because");	stopWords.add("before");
		stopWords.add("like");		stopWords.add("into");
		stopWords.add("said");		stopWords.add("these");
		stopWords.add("less");		stopWords.add("much");
		stopWords.add("new");		stopWords.add("each");
		stopWords.add("your");		stopWords.add("just");
		stopWords.add("but");		stopWords.add("there");
		stopWords.add("at");		stopWords.add("you");
		stopWords.add("see");		stopWords.add("should");
		stopWords.add("how");		stopWords.add("bye");
		stopWords.add("your");		stopWords.add("soon");
		stopWords.add("a");			stopWords.add("did");
		stopWords.add("such");		stopWords.add("those");
		stopWords.add("what");		stopWords.add("as");
		stopWords.add("she");		stopWords.add("by");
	}
}
