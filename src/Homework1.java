import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;


public class Homework1 {
    
    private static HashMap<String, Integer> ngrams = new HashMap<String, Integer>();
    private static HashMap<String, Integer> nMinusOneGrams = new HashMap<String, Integer>();
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        findNGrams("omg", 5);
        System.out.println(ngrams);
        System.out.println(nMinusOneGrams);
        System.out.println(probability("my cat is on fire"));
    }
    
    /**
     * Reads through the test set to find n-grams, and stores them in a HashMap
     */
    public static void findNGrams(String filename, int n) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new FileInputStream(filename));
            String plaintext = "";
            while (scanner.hasNextLine()) {
                plaintext += scanner.nextLine() + "\n";
            }
            ArrayList<String> words = new ArrayList<String>(Arrays.asList(plaintext.split(" |\n")));
            while(words.remove("")); //Eliminate multiple consecutive spaces
            for(int i = 0; i < words.size() - (n - 1); i++) {
                String ngram = getNGram(words, i, n);
                int ngramCount = ngrams.get(ngram) == null ? 0 : ngrams.get(ngram);
                ngrams.put(ngram, ngramCount + 1);
                
                String nMinusOneGram = getNGram(words, i, n - 1);
                //System.out.println(nMinusOneGram);
                int nMinusOneGramCount = nMinusOneGrams.get(nMinusOneGram) == null ? 0 : nMinusOneGrams.get(nMinusOneGram);
                nMinusOneGrams.put(nMinusOneGram, nMinusOneGramCount + 1);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found:" + filename);
        } finally {
            scanner.close();
        }
    }
    
    public static String getNGram(ArrayList<String> words, int pos, int n) {
        String ngram = "";
        for(int j = pos; j < pos + n; j++)
            if(j == pos + n - 1)
                ngram += words.get(j);
            else
                ngram += words.get(j) + " ";
        return ngram;
    }
    
    public static double probability(String ngram) {
        double numerator = ngrams.get(ngram) == null ? 0 : ngrams.get(ngram);
        System.out.println(numerator);
        String nMinusOneGram = ngram.substring(0, ngram.lastIndexOf(' '));
        System.out.println(ngram.lastIndexOf(' '));
        System.out.println(nMinusOneGram);
        double denominator = nMinusOneGrams.get(nMinusOneGram) == null ? 0 : nMinusOneGrams.get(nMinusOneGram);
        System.out.println(denominator);
        return numerator / denominator;
    }
}
