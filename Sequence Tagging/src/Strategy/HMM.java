package Strategy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

import util.Util;

public class HMM {
	private String trainingFile = null;
	private String testFile = null;
	private String outputFile = null;
	private HashMap<String, Integer> tagFreq = new HashMap<String, Integer>();
    private HashMap<String, HashMap<String, Integer>> tagWordFreq = new HashMap<String, HashMap<String,Integer>>();
    private HashMap<String, HashMap<String, Integer>> bigramFreq= new HashMap<String, HashMap<String,Integer>>();
    private HashMap<String, HashMap<String, Integer>> wordTagFreq= new HashMap<String, HashMap<String, Integer>>();

    private Writer out = null;

	public HMM(String trainFile, String testFile, String output) throws FileNotFoundException{
		trainingFile = trainFile;
		this.testFile = testFile;
		outputFile = output;
		out = new OutputStreamWriter(new FileOutputStream(outputFile));
	}

	/**
	 * Runs the strategy
	 * @throws IOException
	 */
    public void execute() throws IOException{
        parseFile();
        System.out.println("Done parsing...");
        viterbi(getWords(testFile));
    }

    /**
     * Gets all the words in a file
     */
    public ArrayList<String> getWords(String file) throws FileNotFoundException{
        ArrayList<String> list = new ArrayList<String>();
        File testFile = new File(file);
        Scanner scanner = new Scanner(testFile);

		while (scanner .hasNext()){
            list.add(scanner.next());
        }
        return list;
    }

    /**
     * Reads the file
     * @throws FileNotFoundException
     */
    private void parseFile() throws FileNotFoundException {
        File file = new File(trainingFile);
        Scanner scanner = new Scanner(file);
        String line = scanner.nextLine();
        String[] tokens = line.split(" ");

        String prevTag = tokens[0];

        while(scanner.hasNext()){
            String tag = scanner.next();
            String word = scanner.next();

            //update tag frequency
            Util.incrementMap(tagFreq, tag);

            updateTagWordFreq(word, tag);
            updateBigramFreq(prevTag, tag);
            updateWordTagFreq(word, tag);
            prevTag = tag;
        }
    }

    /**
     * Updates the word frequency table
     * (Maps tags to word counts)
     */
    public void updateTagWordFreq(String word, String tag) {
        if(tagWordFreq.containsKey(tag)){
            HashMap<String, Integer> newMap = new HashMap<String, Integer>();
            newMap = tagWordFreq.get(tag);
            Util.incrementMap(newMap, word);
        } else {
            HashMap<String, Integer> newMap = new HashMap<String, Integer>();
            newMap.put(word, 1);
            tagWordFreq.put(tag, newMap);
        }
    }

    /**
     * Updates the bigram frequency map
     */
    public void updateBigramFreq(String prevTag, String tag) {
        //update bigram frequency
        if(bigramFreq.containsKey(prevTag)){
            HashMap<String, Integer> newMap = new HashMap<String, Integer>();
            newMap = bigramFreq.get(prevTag);

            Util.incrementMap(newMap, tag);

        } else {
            HashMap<String, Integer> newMap = new HashMap<String, Integer>();
            newMap.put(tag, 1);
            bigramFreq.put(prevTag, newMap);
        }
    }

    /**
     * Updates a map of words to tag counts
     */
    public void updateWordTagFreq(String word, String tag) {
        if(wordTagFreq.containsKey(word)){
            HashMap<String, Integer> newMap = new HashMap<String, Integer>();
            newMap = wordTagFreq.get(word);

            Util.incrementMap(newMap, tag);

        } else {
            HashMap<String, Integer> newMap = new HashMap<String, Integer>();
            newMap.put(tag, 1);
            wordTagFreq.put(word, newMap);
        }
    }

    /**
     * Runs the viterbi algorithm
     */
    public void viterbi(ArrayList<String> words) throws IOException{
        //two-dimensional Viterbi Matrix
        boolean startOfSent = true;
        HashMap<String, Pair> prevMap = null;
        for(int i=0; i<words.size(); i++){
            String word = words.get(i);
            HashMap<String, Pair> newMap = new HashMap<String,Pair>();

            if(startOfSent){
                Pair n = new Pair(word, "<s>", null, 1.0);
                newMap.put(word, n);
                startOfSent = false;
            } else {
                //add all tags
                if(wordTagFreq.containsKey(word)){
                    // Only Training Set tags
                    HashMap<String, Integer> tagcounts = wordTagFreq.get(word);
                    for(String tag : tagcounts.keySet()){
                        newMap.put(tag, computeNodeProb(word, tag, prevMap));
                    }
                } else {
                    for (String tag : tagFreq.keySet())
                        newMap.put(tag, computeNodeProb(word, tag, prevMap));
                }

                if((i == words.size()-1) || words.get(i+1).equals("<s>")){
                	vitrbiBptr(newMap);
                    startOfSent = true;
                }
            }
            prevMap = newMap;
        }
        out.close();
    }

    /**
     * Executes the viterbi backpointer
     */
    private void vitrbiBptr(HashMap<String, Pair> map) throws IOException {
        Pair n = new Pair("", "");
        for(String key : map.keySet()){
            Pair currentNode = map.get(key);
            if(currentNode.prob >= n.prob){
                n = currentNode;
            }
        }

        Stack<Pair> stack = new Stack<Pair>();
        while(n != null){
            stack.push(n);
            n = n.priorPair;
        }

        while(!stack.isEmpty()){
            n = stack.pop();
            out.write(n.tag + " " + n.word + "\n");
        }
    }

    /**
     * Gives the probability of a tag, given the previous tag in sequence
     */
    public double probTagGivenTag(String tag1, String tag2){
    	int vocabSize = tagFreq.keySet().size();
        return (double) (Util.counts(bigramFreq,tag1,tag2)+1) / (double) (Util.counts(tagFreq,tag1)+vocabSize);
    }

    /**
     * Computes the probability of a node in the viterbi algorithm
     */
    private Pair computeNodeProb(String word, String tag, HashMap<String, Pair> prevMap){
        Pair n = new Pair(word,tag);
        double maxProb = 0.0;
        for(String prevTag : prevMap.keySet()){
            Pair prevNode = prevMap.get(prevTag);
            double prevProb = prevNode.prob;
            prevProb = prevProb * probTagGivenTag(prevTag, tag);
            if(prevProb >= maxProb){
                maxProb = prevProb;
                n.priorPair = prevNode;
            }
        }

        int tagSize = wordTagFreq.keySet().size();
        double prob =  (double) (Util.counts(tagWordFreq,tag,word)+1) / (double) (Util.counts(tagFreq,tag)+tagSize);

        n.prob = maxProb * prob;
        return n;
    }

    public static void main(String[] args) throws IOException {
        HMM strategy = new HMM("pos_files/train.pos", "pos_files/test-obs.pos", "kaggle.csv");
        strategy.execute();
    }
}