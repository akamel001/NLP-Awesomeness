import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Homework1 {

    private static HashMap<String, Integer> ngrams = new HashMap<String, Integer>();
    private static HashMap<String, Integer> nMinusOneGrams = new HashMap<String, Integer>();

    //End line with a period NOT FOLLOWED by a number (i.e. don't match 12.5)
    private static final String DELIMETER_PATTERN = "[.][^12345890]";

    public static void main(String[] args) {
        authorPrediction("data/EnronDataset/debug.txt", "data/EnronDataset/debug.txt", 2);
    }

    /**
     * Performs the experiments for Homework1, part 5: Email Authot Prediction
     * @param filename
     */
    public static void authorPrediction(String trainingSetFilename, String testSetFilename, int n) {
        //TODO: Finish me

        //Maps from Author to language model
        HashMap<String, HashMap<String, Integer>> authors = new HashMap<String, HashMap<String, Integer>>();

        ArrayList<String> lines = getLines(trainingSetFilename);
        //For author prediction, each line corresponds to a training example
        for(String line : lines) {
            String[] split = line.split("\t");
            //Find the right author for this example
            if(authors.get(split[0]) != null) {

            } else {

            }
        }
        ArrayList<String> sentences = getSentences(lines);
        ArrayList<String> words = getWords(sentences, n);
        findNGrams(words, n);
    }

    /**
     * Performs the experiments for Homework1, part 2: Random Sentence Generation
     * @param filename
     */
    public static void randomSentenceGeneration(String trainingSetFilename, int n) {
        //TODO: Finish Me

        //Example of reading files:
        ArrayList<String> lines = getLines(trainingSetFilename);
        ArrayList<String> sentences = getSentences(lines);
        ArrayList<String> words = getWords(sentences, n);
        findNGrams(words, n);

    }

    /**
     * Returns the lines of a file in an ArrayList
     * @param filename
     * @return
     */
    public static ArrayList<String> getLines(String filename) {
        System.out.println("Reading File:" + filename);
        Scanner scanner = null;
        ArrayList<String> sentences = new ArrayList<String>();
        try {
            scanner = new Scanner(new FileInputStream(filename));

            scanner.useDelimiter(Pattern.compile(DELIMETER_PATTERN));
            while (scanner.hasNextLine()) {
                System.out.println(scanner.nextLine() + ".");
                sentences.add(scanner.nextLine() + ".");
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found:" + filename);
        }
        return sentences;
    }

    /**
     * Goes through a list of lines and returns sentences in an ArrayList.
     * @param filename
     * @param n
     */
    public static ArrayList<String> getSentences(ArrayList<String> lines) {
        String fulltext = "";
        for(String line : lines) {
            fulltext += line;
        }
        return new ArrayList<String>(Arrays.asList(fulltext.split("[.][^12345890]")));
    }

    /**
     * Reads through the test set to find n-grams, and stores them in a HashMap
     * @param filename - Name of file to load words from
     * @param n - Order of the language model
     */
    public static void findNGrams(ArrayList<String> words, int n) {
        System.out.println("Building word frequency tables");

        for(int i = 0; i < words.size() - (n - 1); i++) {
            String ngram = getNGram(words, i, n);
            int ngramCount = ngrams.get(ngram) == null ? 0 : ngrams.get(ngram);
            ngrams.put(ngram, ngramCount + 1);
        }

        for(int i = 0; (n != 1) && i <= words.size() - (n - 1); i++){
            String nMinusOneGram = getNGram(words, i, n - 1);
            int nMinusOneGramCount = nMinusOneGrams.get(nMinusOneGram) == null ? 0 : nMinusOneGrams.get(nMinusOneGram);
            nMinusOneGrams.put(nMinusOneGram, nMinusOneGramCount + 1);
        }
    }

    /**
     * Given a list of sentences, break it into a list of words.
     * @param string
     * @param order of the model
     * @return
     */
    public static ArrayList<String> getWords(ArrayList<String> sentences, int n) {
        String fulltext = "";
        System.out.println("Breaking sentences into words");
        for(String sentence : sentences) {
            fulltext += cleanSentence(sentence,n);
        }
        ArrayList<String> words = new ArrayList<String>(Arrays.asList(fulltext.split(" |\n")));
        System.out.println("Removing Spaces");
        while(words.remove("")); //Eliminate multiple consecutive spaces

        return words;
    }

    /**
     * Seperates punctuation marks so they will be counted as words;
     *
     * @param sentence
     * @param n
     * @return
     */
    public static String cleanSentence(String sentence, int n) {
        System.out.println("cleanSentence called");
        String prefix = "";
        for(int i = 0; i < n - 1; i++) {
            prefix += "<S> ";
        }
        //TODO: string replaces are a bit slow
        sentence = prefix + sentence;
        //sentence = sentence.replace("." , " .");
        sentence = sentence.replace("," , " , ");
        sentence = sentence.replace("\"", " \" ");
        sentence = sentence.replace("\"", " \" ");
        sentence = sentence.replace("(" , " ( ");
        sentence = sentence.replace(")", " ) ");
        sentence = sentence.replace(":", " : ");
        return sentence;
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
     * Computes the cumulative word frequency out of a frequency table
     * @param frequencyTable
     * @return
     */
    public static int wordCount(HashMap<String, Integer> frequencyTable) {
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
    public static double probability(String ngram, int n) {
        double numerator = ngrams.get(ngram) == null ? 0 : ngrams.get(ngram);
        int index = (ngram.lastIndexOf(' ') == -1)? 0 : ngram.lastIndexOf(' ');
        double denominator = 1;
        if (n == 1) {
            denominator = wordCount(ngrams);
        } else {
            String nMinusOneGram = ngram.substring(0, index);
            denominator = nMinusOneGrams.get(nMinusOneGram) == null ? 0 : nMinusOneGrams.get(nMinusOneGram);
        }
        return (numerator + 1) / (denominator + ngrams.size());
    }

    /**
     * Computes the total probability of a string, given a language model
     * Used for perplexity and author prediction
     * @param string - The string to find the probability of
     * @param probabilityTable - A particular language model to compute strings from
     * @param n - order of the language model (i.e. unigrams, bigrams, etc)
     */
    public static double probabilityOfDocument(ArrayList<String> sentences, HashMap<String, Double> probabilityTable, int n) {
        //TODO: Look up probability of first n words
        //TODO: Consider log probabilities to avoid underflow
        ArrayList<String> words = getWords(sentences, n);
        double probability = 1.0;
        for(int i = 0; i < words.size() - n; i++) {
            probability *= probabilityTable.get(getNGram(words, i, n));
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
    public static double perplexity(ArrayList<String> sentences, HashMap<String, Double> probabilityTable, int n) {
        return Math.pow(1 / probabilityOfDocument(sentences, probabilityTable, n), 1.0 / n);
    }
}