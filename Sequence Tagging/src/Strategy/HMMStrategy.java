package Strategy;

import java.io.IOException;
import java.util.ArrayList;

import HMM.HMM;
import HMM.HMM.Pair;

public class HMMStrategy extends ParseStrategy {

    private ArrayList<Pair> trainPairs;
    private int n;
    private HMM hmm;
    private ArrayList<String> prevWords = new ArrayList<String>();

    public HMMStrategy(String trainPath, String testPath, String kaggleOutput, int n) {
        super(trainPath, testPath, kaggleOutput);
        this.n = n;
    }

    @Override
    public void train(String line) {
        String[] split = line.split("[ ]+");
        String tag = split[0];
        String word = split[1];
        trainPairs.add(new Pair(word, tag));
        System.out.println(word + " " + tag);
    }

    @Override
    public String test(String word) {
        System.out.println(word);
        prevWords.add(word);
        if(prevWords.size() > n) {
            prevWords.remove(0);
        }
        if(word.equals("<s>"))
            return "<s> <s>";
        return hmm.predict(prevWords).get(n-1) + " " + word;
    }

    @Override
    public void execute() throws IOException {
        //pairs = ObjectSerializer.readObject("pos-train.dat");
        if(trainPairs == null) {
            trainPairs = new ArrayList<Pair>();
            parseFile(getTrainPath(), true);
        //    ObjectSerializer.writeObject(pairs, "pos-train.dat");
        }

        postProcess();
        parseFile(getTestPath(), false);
        printKaggle();
    }

    @Override
    public void postProcess() {
        System.out.println("Building Model");
        hmm = new HMM(n, trainPairs);
        System.out.println("Model built");
    }
}
