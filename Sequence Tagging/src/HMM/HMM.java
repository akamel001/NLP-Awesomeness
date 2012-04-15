package HMM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import util.Util;

public class HMM {

    private int n;
    private List<Pair> pairs;
    private HashSet<String> words = new HashSet<String>();
    private HashSet<String> tags = new HashSet<String>();
    private HashMap<String, Integer> nGramCount = new HashMap<String, Integer>();
    private HashMap<String, Integer> nMinusOneGramCount = new HashMap<String, Integer>();
    private HashMap<String, HashMap<String, Integer>> tagsMap = new HashMap<String, HashMap<String, Integer>>();
    private HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
    private HashMap<String, Integer> tagCount = new HashMap<String, Integer>();

    public HMM(int n, List<Pair> pairs) {
        this.n = n;
        this.pairs = pairs;

        //Find States and Labels
        for(Pair pair : pairs) {
            words.add(pair.word);
            tags.add(pair.tag);
        }

        //Find tag transition probabilities
        countGrams(n, nGramCount, false);
        if(n > 1)
            countGrams(n-1, nMinusOneGramCount, false);

        //Find emission probabilities
        for(Pair pair : pairs) {
            Util.tagWordCount(tagsMap, pair.word, pair.tag);
            Util.incrementMap(wordCount, pair.word);
            Util.incrementMap(tagCount, pair.tag);
        }
    }

    /**
     * Gets the probability of a tag, given the n-1 previous tags
     * @param nGram
     * @return
     */
    public double getTransitionProb(List<String> nGram) {
        if(!nGramCount.containsKey(nGram.toString()))
            return 0;
        if(n > 1)
            return (double)nGramCount.get(nGram.toString()) / nMinusOneGramCount.get(nGram.remove(nGram.size()-1).toString());
        else
            return nGramCount.get(nGram) / tags.size();
    }

    /**
     * Returns the probability of a word given its tag
     * @param word
     * @param tag
     * @return
     */
    public double getEmissionProb(String word, String tag) {
        if(wordCount.get(word) == 0)
            return 0;
        return tagsMap.get(tag).get(word) / tagCount.get(tag);
    }

    public void countGrams(int n, HashMap<String, Integer> count, boolean word) {
        List<String> nGram = new ArrayList<String>();
        int i;
        for(i = 0; i < n; i++)
            nGram.add(pairs.get(i).getContent(word));
        for(i++;i < pairs.size(); i++) {
            nGram.remove(0);
            nGram.add(pairs.get(i).getContent(word));
            Util.incrementMap(count, nGram.toString());
        }
    }

    public String predict(String word) {
        //TODO: Viterbi
        return null;
    }

    public static class Pair {
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
