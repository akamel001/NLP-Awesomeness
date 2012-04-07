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
    private HashMap<String, HashMap<String, Integer>> wordsMap = new HashMap<String, HashMap<String, Integer>>();
    private HashMap<String, Integer> wordCount = new HashMap<String, Integer>();

    public HMM(int n, List<Pair> pairs) {
        this.n = n;
        this.pairs = pairs;

        //Find States and Labels
        for(Pair pair : pairs) {
            words.add(pair.word);
            tags.add(pair.tag);
        }

        //Find transition probabilities
        countGrams(n, nGramCount);
        if(n > 1)
            countGrams(n-1, nMinusOneGramCount);

        //Find emission probabilities
        for(Pair pair : pairs) {
            Util.wordTagCount(wordsMap, pair.word, pair.tag);
            Util.incrementMap(wordCount, pair.word);
        }
    }

    public double getTransitionProb(List<Pair> nGram) {
        if(!nGramCount.containsKey(nGram.toString()))
            return 0;
        if(n > 1)
            return (double)nGramCount.get(nGram) / nMinusOneGramCount.get(nGram.remove(nGram.size()-1));
        else
            return nGramCount.get(nGram);
    }

    public double getEmissionProb(String word, String tag) {
        if(wordCount.get(word) == 0)
            return 0;
        return wordsMap.get(word).get(tag) / wordCount.get(word);
    }

    public void countGrams(int n, HashMap<String, Integer> count) {
        List<Pair> nGram = new ArrayList<Pair>();
        int i;
        for(i = 0; i < n; i++)
            nGram.add(pairs.get(i));
        for(i++;i < pairs.size(); i++) {
            nGram.remove(0);
            nGram.add(pairs.get(i));
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
    }
}
