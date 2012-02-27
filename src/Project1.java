import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class Project1 {

    //End line with a period NOT FOLLOWED by a number (i.e. don't match 12.5)
    private static final String DELIMETER_PATTERN = "[.][^12345890]";

    private static final int LANGUAGE_MODEL_ORDER = 2;
    private static final int K_SAMPLE_REJECT = 1;
    private static final int SENTENCE_WORD_LIMIT = -1;

    public static void main(String[] args) {

    	String file = (args.length > 0)? args[0] : "data/test";

    	randomSentenceGeneration(file, K_SAMPLE_REJECT, SENTENCE_WORD_LIMIT, LANGUAGE_MODEL_ORDER);

        //authorPrediction(file, "data/EnronDataset/validation.txt", "data/EnronDataset/test.txt", LANGUAGE_MODEL_ORDER);

        //System.out.println(filePerplexity("data/Dataset3/Train.txt", "data/Dataset3/Test.txt", LANGUAGE_MODEL_ORDER));
    }

    /**
    *
    * @param filename
    * @return
    */
   public static double filePerplexity(String training, String testing, int n) {
       LanguageModel m = new LanguageModel();;
       ArrayList<String> trainingWords = Project1.getWords(Project1.getSentences(Project1.getLines(training)), n);
       Project1.findNGrams(trainingWords, n, m);

       ArrayList<String> testingWords = Project1.getWords(Project1.getSentences(Project1.getLines(testing)), n);
       return m.probabilityOfDocument(testingWords, n);
   }


    /**
     * Performs the experiments for Homework1, part 5: Email Author Prediction
     * @param filename
     */
    public static void authorPrediction(String trainingSetFilename, String validationSetFilename, String testSetFilename, int n) {
        HashMap<String, LanguageModel> authors = new HashMap<String, LanguageModel>();

        //Train
        ArrayList<String> lines = getLines(trainingSetFilename);
        //For author prediction, each line corresponds to a training example
        for(String line : lines) {
            String[] split = line.split("\t");
            String authorName = split[0];
            String email = split[1];

            ArrayList<String> text = new ArrayList<String>();
            text.add(email);
            ArrayList<String> words = getWords(getSentences(text), n);
            //Find the right author for this example
            LanguageModel m = authors.get(authorName);
            if(m == null) {
                m = new LanguageModel();
                findNGrams(words, n, m);
                authors.put(authorName, m);
            } else {
                findNGrams(words, n, m);
            }
        }


        ArrayList<String> predictions = new ArrayList<String>();
        double accuracy = testAuthorPrediction(validationSetFilename, authors, predictions, n);
        testAuthorPrediction(testSetFilename, authors, predictions, n);
        System.out.println("Accuracy:" + accuracy);

        writeFile(predictions);
    }

    /**
     * Tests a file against a collection of authors and attempts to label each one
     * @param testSetFilename - File to test against
     * @param authors - Mapping of authors to language models
     * @param predictions - Prediction results to be written here
     * @param n - Order of the language models
     * @return
     */
    public static double testAuthorPrediction(String testSetFilename, HashMap<String, LanguageModel> authors, ArrayList<String> predictions, int n) {
        //Test
        ArrayList<String> lines = getLines(testSetFilename);
        int numCorrect = 0;
        int count = 0;
        for(String line : lines) {
            System.out.println(count++);
            String[] split = line.split("\t");
            String authorName = split[0];
            String email = split[1];

            ArrayList<String> text = new ArrayList<String>();
            text.add(email);
            ArrayList<String> words = getWords(getSentences(text), n);

            double bestProb = Double.NEGATIVE_INFINITY;
            String bestAuthor = "";
            //find the most probable author
            for(Map.Entry<String, LanguageModel> entry : authors.entrySet()) {
                double probability = entry.getValue().probabilityOfDocument(words, n);
                if(probability > bestProb) {
                    bestProb = probability;
                    bestAuthor = entry.getKey();
                }
            }
            predictions.add(bestAuthor);
            if(bestAuthor.equals(authorName)) {
                numCorrect++;
            }
        }
        return (double)numCorrect / lines.size();
    }

    /**
     * Writes a list of predictions to a file, for submission to kaggle
     * @param predictions
     */
    public static void writeFile(ArrayList<String> predictions) {
        try{
            // Create file
            FileWriter fstream = new FileWriter("kaggle.csv");
            BufferedWriter out = new BufferedWriter(fstream);
            for(String prediction : predictions) {
                out.write(prediction + "\n");
            }
            out.close();
        } catch (Exception e) {
            System.err.println("Error writing kaggle.csv: " + e.getMessage());
        }
    }

    /**
     * Performs the experiments for Homework1, part 2: Random Sentence Generation
     * @param filename
     */
    public static void randomSentenceGeneration(String trainingSetFilename, int k, int wLimit, int n) {
        LanguageModel m = new LanguageModel();
        //Example of reading files:
        ArrayList<String> lines = getLines(trainingSetFilename);
        ArrayList<String> sentences = getSentences(lines);
        ArrayList<String> words = getWords(sentences, n);
        findNGrams(words, n, m);

        //TODO add loop to generate multiple sentences depending on variable
        randomSentenceGenerator(k, n, wLimit, m);
    }

    /**
     * Returns the last n words from the string
     */
    public static String getLastWords(String string, int n) {
        String[] split = string.split(" ");
        String words = "";
        for(int i = split.length - n; i < split.length; i++) {
            words += split[i] + " ";
        }
        return words;
    }


    /**
     * Used to construct a randomSentence. The sentence gets generated by using
     * two random variables; one for position and the other for probability of word being chosen.
     * The function keeps generating words until a period has been chosen.
     * @param k - value of how many times period gets rejected if chosen
     * @return
     * @
     */
    public static void randomSentenceGenerator(int k, int n, int wLimit, LanguageModel m) {
        System.out.println("Generating Random Sentence");
        Random generator = new Random();
        int pos = 0, kValue = 0, wordsAdded = 0;
        double prob = 0;

        //Use prefix to make sure sentence starts with the right words
        String sentence = getPrefix(n);
        Object[] words = m.words.toArray();

        while(kValue < k) {
            pos = generator.nextInt(m.words.size());
            prob = generator.nextDouble();
            String word = words[pos].toString();
            String lastWords = getLastWords(sentence, n-1);

            if(prob <= m.probability(lastWords + word, n, false)) {
                if(word.endsWith(".")) {
                    kValue++;
                    if(kValue == k)
                        sentence += word;
                    continue;
                }

                sentence += word + " ";
                wordsAdded++;
                if(wLimit != -1 && wordsAdded >= wLimit) {
                    sentence += ".";
                    break;
                }
            }
        }
        sentence = sentence.replaceAll("<S>", "");
        System.out.println(sentence);
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

            while (scanner.hasNextLine())
                sentences.add(scanner.nextLine() + " ");

        } catch (FileNotFoundException e) {
            System.out.println("File not found:" + filename);
            System.exit(1);
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
            fulltext += line + " ";
        }

        ArrayList<String> noPeriods =  new ArrayList<String>(Arrays.asList(fulltext.split("[.][^12345890]")));

        //Put periods on the end
        for(int i = 0; i < noPeriods.size(); i++)
            noPeriods.set(i, noPeriods.get(i) + " . ");

        return noPeriods;
    }

    /**
     * Reads through the test set to find n-grams, and stores them in a HashMap
     * @param filename - Name of file to load words from
     * @param n - Order of the language model
     */
    public static void findNGrams(ArrayList<String> words, int n, LanguageModel m) {
        System.out.println("Building word frequency tables");

        for(int i = 0; i < words.size() - (n - 1); i++) {
            String ngram = getNGram(words, i, n);
            int ngramCount = m.ngrams.get(ngram) == null ? 0 : m.ngrams.get(ngram);
            m.ngrams.put(ngram, ngramCount + 1);
        }

        for(int i = 0; (n != 1) && i <= words.size() - (n - 1); i++) {
            String nMinusOneGram = getNGram(words, i, n - 1);
            int nMinusOneGramCount = m.nMinusOneGrams.get(nMinusOneGram) == null ? 0 : m.nMinusOneGrams.get(nMinusOneGram);
            m.nMinusOneGrams.put(nMinusOneGram, nMinusOneGramCount + 1);
        }

        for(int i = 0; i < words.size(); i++) {
            String unigram = words.get(i);
            int unigramCount = m.unigrams.get(unigram) == null ? 0 : m.nMinusOneGrams.get(unigram);
            m.unigrams.put(words.get(i), unigramCount + 1);
        }

        for(int i = 0; i < words.size(); i++) {
            m.words.add(words.get(i));
        }
        m.words.remove("<S>");
    }

    /**
     * Given a list of sentences, break it into a list of words.
     * @param string
     * @param order of the model
     * @return
     */
    public static ArrayList<String> getWords(ArrayList<String> sentences, int n) {
        System.out.println("Breaking sentences into words");

        String fulltext = "";

        for(String sentence : sentences) {
            fulltext += cleanSentence(sentence, n) + " ";
        }

        ArrayList<String> words = new ArrayList<String>(Arrays.asList(fulltext.split(" |\n")));
        System.out.println("Removing Spaces");
        while(words.remove("")); //Eliminate multiple consecutive spaces

        return words;
    }

    /**
     * Generates a string with n - 1 consecutive "<s> "s
     * @param n
     * @return
     */
    public static String getPrefix(int n) {
        String prefix = "";
        for(int i = 0; i < n - 1; i++) {
            prefix += "<S> ";
        }
        return prefix;
    }

    /**
     * Seperates punctuation marks so they will be counted as words;
     *
     * @param sentence
     * @param n
     * @return
     */
    public static String cleanSentence(String sentence, int n) {
        //System.out.println("cleanSentence called");

        //TOOD: Any other cases that make sense?
        sentence = getPrefix(n) + sentence;
        sentence = sentence.replace("." , " . ");
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
}
