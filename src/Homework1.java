import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;


public class Homework1 {
    
    private static HashMap<String, Integer> ngrams = new HashMap<String, Integer>();
    //private static HashMap<String, Integer> nminusonegrams = new HashMap<String, Integer>();
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

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
            ArrayList<String> words = new ArrayList<String>(Arrays.asList(plaintext.split(" \t")));
            while(words.remove("")); //Eliminate multiple consecutive spaces
            for(int i = 0; i < words.size() - (n - 1); i++) {
                String ngram = "";
                for(int j = i; j < i + n; j++)
                    ngram += words.get(j);
                int ngramCount = ngrams.get(i) == null ? 0 : ngrams.get(i);
                ngrams.put(ngram, ngramCount + 1);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found:" + filename);
        } finally {
            scanner.close();
        }
    }
    
    public static void probabilityTable() {}

}
