import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


class LanguageModel {

    public HashSet<String> words = new HashSet<String>();
    public HashMap<String, Integer> ngrams = new HashMap<String, Integer>();
    public HashMap<String, Integer> nMinusOneGrams = new HashMap<String, Integer>();

    /**
     * Computes the cumulative word frequency out of a frequency table
     * @param frequencyTable
     * @return
     */
    public int wordCount(HashMap<String, Integer> frequencyTable) {
        int count = 0;
        for(Integer freq : frequencyTable.values())
            count += freq;
        return count;
    }

    /**
     * Finds the conditional probability of the last word in the string, given the previous words
     * @param ngram
     * @return double - conditional probability
     */
    public double probability(String ngram, int n, boolean useSmoothing) {
        double numerator = ngrams.get(ngram) == null ? 0 : ngrams.get(ngram);
        int index = (ngram.lastIndexOf(' ') == -1)? 0 : ngram.lastIndexOf(' ');
        double denominator = 1;
        if (n == 1) {
            denominator = wordCount(ngrams);
        } else {
            String nMinusOneGram = ngram.substring(0, index);
            //System.out.println(ngram);
            //System.out.println(nMinusOneGram);
            denominator = nMinusOneGrams.get(nMinusOneGram) == null ? 0 : nMinusOneGrams.get(nMinusOneGram);
        }
        //System.out.println("numerator: " + numerator);
        //System.out.println("denominator:" + denominator);
        if(useSmoothing) {
            return (numerator + 1) / (denominator + ngrams.size());
        } else {
            return numerator / denominator;
        }
    }

    /**
     * Computes the total probability of a string, given a language model
     *
     * ----------> NOTE: USES LOG PROBABILITIES! <------------------
     * May be inaccurate in other contexts
     *
     * Used for perplexity and author prediction
     * @param string - The string to find the probability of
     * @param probabilityTable - A particular language model to compute strings from
     * @param n - order of the language model (i.e. unigrams, bigrams, etc)
     */
    public double probabilityOfDocument(ArrayList<String> words, int n) {
        double probability = 1.0;
        for(int i = 0; i < words.size() - n; i++) {
            double logProb = Math.log(probability(Homework1.getNGram(words, i, n), n, true));
            probability += logProb;
        }
        return probability;
    }

    /**
     * Finds the perplexity of a document given a language model
     * @param string
     * @param probabilityTable
     * @param n
     * @return
     */
    public double perplexity(ArrayList<String> sentences,  int n) {
        return probabilityOfDocument(sentences, n) * -1.0 / n;
    }
};