package HMM;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import util.Util;

public class HMM implements Serializable {

    private int n;
    private List<Pair> pairs;
    private HashSet<String> words = new HashSet<String>();
    private HashSet<String> tags = new HashSet<String>();
    private HashMap<String, Integer> nGramCount = new HashMap<String, Integer>();
    private HashMap<String, Integer> nMinusOneGramCount = new HashMap<String, Integer>();
    private HashMap<String, HashMap<String, Integer>> tagsMap = new HashMap<String, HashMap<String, Integer>>();
    private HashMap<String, HashMap<String, Integer>> wordsMap = new HashMap<String, HashMap<String, Integer>>();
    private HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
    private HashMap<String, Integer> tagCount = new HashMap<String, Integer>();
    private HashMap<String, String> mostCommonTagMap = new HashMap<String, String>();
    private String mostCommonTag = null;

    public HMM(int n, List<Pair> pairs) {
        long time = System.currentTimeMillis();
        this.n = n;
        this.pairs = pairs;

        System.out.println("Counting states");
        //Find States and Labels
        for(Pair pair : pairs) {
            words.add(pair.word);
            tags.add(pair.tag);
        }

        System.out.println("Finding transition probabilities");
        System.out.println(pairs.size());
        //Find tag transition probabilities
        System.out.println("Counting ngrams");
        countGrams(n, nGramCount, false);
        if(n > 1) {
            System.out.println("Counting n-1 grams");
            countGrams(n-1, nMinusOneGramCount, false);
        }

        //Find emission probabilities
        System.out.println("Finding emission probabilities");
        for(Pair pair : pairs) {
            Util.tagWordCount(tagsMap, pair.word, pair.tag);
            Util.wordTagCount(wordsMap, pair.word, pair.tag);
            Util.incrementMap(wordCount, pair.word);
            Util.incrementMap(tagCount, pair.tag);
        }
        Util.mostCommonTagMap(wordsMap, mostCommonTagMap);
        mostCommonTag = Util.mostCommonTag(tagCount);
        System.out.println((System.currentTimeMillis() - time)/1000.0);
    }

    /**
     * Gets the probability of a tag, given the n-1 previous tags
     * @param nGram
     * @return
     */
    public double getTransitionProb(List<String> nGram) {
        if(!nGramCount.containsKey(nGram.toString()))
            return 0;
        if(n > 1) {
            ArrayList<String> nGramMini = new ArrayList<String>();
            nGramMini.addAll(nGram);
            nGramMini.remove(0);
            return (double)nGramCount.get(nGram.toString()) / nMinusOneGramCount.get(nGramMini.toString());
        } else
            return nGramCount.get(nGram) / (double)pairs.size();
    }

    /**
     * Returns the probability of a word given its tag
     * @param word
     * @param tag
     * @return
     */
    public double getEmissionProb(String word, String tag) {
        if(!tagsMap.containsKey(tag))
            return 0;
        Integer numerator = tagsMap.get(tag).get(word);
        Double denominator = (double)tagCount.get(tag);
        if(numerator == null || denominator == null)
            return 0;
        return numerator/denominator;
        //return tagsMap.get(tag).get(word) / (double)tagCount.get(tag);
    }

    public double getTagProb(String tag) {
        return tagCount.get(tag) / (double)pairs.size();
    }

    public void countGrams(int n, HashMap<String, Integer> count, boolean word) {
        List<String> nGram = new ArrayList<String>();
        int i;
        for(i = 0; i < n; i++)
            nGram.add(pairs.get(i).getContent(word));
        for(;i < pairs.size(); i++) {
            Util.incrementMap(count, nGram.toString());
            nGram.add(pairs.get(i).getContent(word));
            nGram.remove(0);
        }
        Util.incrementMap(count, nGram.toString());
    }

    public ArrayList<String> predict(List<String> nGram) {
        //Initialization
    	int c = tags.size();
    	double[][] score = new double[c][n];
    	int [][] bptr = new int[c][n];

    	ArrayList<String> myTags= new ArrayList<String>();
    	myTags.addAll(tags);

    	for(int i = 0; i < c; i++){
    	    double tagProb = getTagProb(myTags.get(i));
    	    double emProb = getEmissionProb(nGram.get(0), myTags.get(i));
    		score[i][0] = tagProb * emProb;
    		/*if(score[i][0] == 0 && tagProb > 0 && emProb > 0) {
                System.err.println("UNDERFLOW");
                System.exit(1);
            }*/
    		bptr[i][0] = 0;
    	}

    	//Iteration
    	for(int t = 1; t < n; t++){
    		for(int i = 0; i < c; i++){

    			double bestProb = Double.NEGATIVE_INFINITY;
    			int bestIndex = 0;
    			for(int j = 0; j < c; j++){
    			    ArrayList<String> tagGram = new ArrayList<String>();
    			    tagGram.add(myTags.get(j));
    			    tagGram.add(myTags.get(i));
    			    double transProb = getTransitionProb(tagGram);
    				double curProb = score[j][t-1] * transProb;
    				/*if(curProb == 0 && score[j][t-1] > 0 && transProb > 0) {
    				    System.err.println("UNDERFLOW");
    				    System.exit(1);
    				}*/
    				if(curProb > bestProb){
    					bestProb = curProb;
    					bestIndex = j;
    				}
    			}
    			double emProb = getEmissionProb(nGram.get(t), myTags.get(i));
    			score[i][t] = bestProb * emProb;
    			/*if(score[i][t] == 0 && bestProb > 0 && emProb > 0) {
                    System.err.println("UNDERFLOW");
                    System.exit(1);
                }*/
    			bptr[i][t] = bestIndex;
    		}
    	}
    	//Identify
    	int[] t = new int[n];

    	for(int j = 0; j < n; j++) {
	        int maxIndx= Integer.MIN_VALUE;
	        double max = Double.NEGATIVE_INFINITY;
    		for(int i = 0; i < c; i++){
        		if(score[i][j] > max) {
        			maxIndx = i;
        			max = score[i][j];
        		}
        	}
    		t[j] = maxIndx;
    	}

    	//CHECKME
    	/*for(int i = n-2; i >= 0; i--)
    		t[i] = bptr[t[i+1]] [i+1];*/

    	ArrayList<String> results = new ArrayList<String>();

    	for(int i = 0; i < n; i++)
    		results.add(myTags.get(t[i]));
    	if(results.get(results.size()-1).equals("<s>")) {
    	    results.remove(results.size()-1);
    	    if(mostCommonTagMap.containsKey(nGram.get(n-1))) {
    	        results.add(mostCommonTagMap.get(nGram.get(n-1)));
    	    } else {
    	        results.add(mostCommonTag);
    	    }
    	}

        return results;
    }

    public static class Pair implements Serializable {
        private String word;
        private String tag;

        public Pair(String word, String tag) {
            this.word = word;
            this.tag = tag;
        }

        public String getContent(boolean word) {
            if(word)
                return this.word;
            else
                return this.tag;
        }
    }
}
