import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;


public class Homework1 {

    private static HashMap<String, Integer> ngrams = new HashMap<String, Integer>();
    private static HashMap<String, Integer> nMinusOneGrams = new HashMap<String, Integer>();

    //private static HashMap<String, Double> unigram = new HashMap<String, Double>();
    private static HashMap<String, Double> bigram = new HashMap<String, Double>();


    public static void main(String[] args) {
        //bigram
        //TODO Does not work when n = 1 need
        Homework1.findNGrams("data/test", 2);
        //TODO Only workds for bigram not N-gram (fix later)

        Iterator<Entry<String, Integer>> itr1 = nMinusOneGrams.entrySet().iterator();
        Iterator<Entry<String, Integer>> itr2 = nMinusOneGrams.entrySet().iterator();
        while(itr1.hasNext()){
            Entry<String, Integer> key1 = itr1.next();
            while(itr2.hasNext()){
                Entry<String, Integer> key2 = itr2.next();
                String grams = key1.getKey() + " "+ key2.getKey();
                double prob= probability(grams);
                bigram.put(grams, prob);
            }
            itr2 = nMinusOneGrams.entrySet().iterator();
        }

        System.out.println(bigram);
    }

    /**
     * Reads through the test set to find n-grams, and stores them in a HashMap
     * @param filename - Name of file to load words from
     * @param n - Order of the language model
     */
    public static void findNGrams(String filename, int n) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new FileInputStream(filename));
            String plaintext = "";
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine() + "\n";
                plaintext += line;
            }
            ArrayList<String> words = getWords(plaintext);

            while(words.remove("")); //Eliminate multiple consecutive spaces

            for(int i = 0; (n != 1) && i < words.size() - (n - 1); i++) {
                String ngram = getNGram(words, i, n);
                int ngramCount = ngrams.get(ngram) == null ? 0 : ngrams.get(ngram);
                ngrams.put(ngram, ngramCount + 1);
            }

            for(int i = 0; i <= words.size() - (n -1); i++){
                String nMinusOneGram = getNGram(words, i, n -1);
                int nMinusOneGramCount = nMinusOneGrams.get(nMinusOneGram) == null ? 0 : nMinusOneGrams.get(nMinusOneGram);
                nMinusOneGrams.put(nMinusOneGram, nMinusOneGramCount + 1);
            }

        } catch (FileNotFoundException e) {
            System.out.println("File not found:" + filename);
        } finally {
            System.out.println(ngrams + "\n");
            System.out.println(nMinusOneGrams + "\n");
            scanner.close();
        }
    }

    /**
     * Given a string, returns a list of words contained in it
     * @param string
     * @return
     */
    public static ArrayList<String> getWords(String line) {
        line = line.replace("." , " . ");
        line = line.replace("," , " , ");
        line = line.replace("\"", " \" ");
        line = line.replace("\"", " \" ");
        line = line.replace("(" , " ( ");
        line = line.replace(")", " ) ");
        return new ArrayList<String>(Arrays.asList(line.split(" |\n")));
    }

    /**
     * Returns a string containing n words beginning from pos, space seperated
     * @param words The text to extract the n-gram from
     * @param pos The starting position
     * @param n - Length of the n-gram
     * @return
     */
    public static String getNGram(ArrayList<String> words, int pos, int n) {
        String ngram = "";
        //System.out.println(pos + " " + n);
        for(int j = pos; j < pos + n; j++)
            if(j == pos + n - 1)
                ngram += words.get(j);
            else
                ngram += words.get(j) + " ";
        return ngram;
    }

    /**
     * Finds the conditional probability of the last word in the string, given the previous words
     * @param ngram
     * @return double - conditional probability
     */
    public static double probability(String ngram) {
        double numerator = ngrams.get(ngram) == null ? 0 : ngrams.get(ngram);
        int index = (ngram.lastIndexOf(' ') == -1)? 0 : ngram.lastIndexOf(' ');
        String nMinusOneGram = ngram.substring(0, index);
        double denominator = nMinusOneGrams.get(nMinusOneGram) == null ? 0 : nMinusOneGrams.get(nMinusOneGram);
        return numerator / denominator;
    }

    /**
     * Computes the total probability of a string, given a language model
     * Used for perplexity and author prediction
     * @param string - The string to find the probability of
     * @param probabilityTable - A particular language model to compute strings from
     * @param n - order of the language model (i.e. unigrams, bigrams, etc)
     */
    public static double probabilityOfString(String string, HashMap<String, Double> probabilityTable, int n) {
        //TODO: Look up probability of first n words
        //TODO: Consider log probabilities to avoid underflow
        ArrayList<String> words = getWords(string);
        double probability = 1.0;
        for(int i = 0; i < words.size() - n; i++) {
            probability *= probabilityTable.get(getNGram(words, i, n));
        }
        return probability;
    }

    /**
     * Finds the perplexity of a string given a language model
     * @param string
     * @param probabilityTable
     * @param n
     * @return
     */
    public static double perplexity(String string, HashMap<String, Double> probabilityTable, int n) {
        return Math.pow(1 / probabilityOfString(string, probabilityTable, n), 1.0 / n);
    }
}
