package Strategy;

import java.io.IOException;
import java.util.ArrayList;

import HMM.HMM;
import HMM.HMM.Pair;

public class HMMStrategy extends ParseStrategy {

    private ArrayList<Pair> pairs;
    private int n;
    private HMM hmm;

    public HMMStrategy(String trainPath, String testPath, String kaggleOutput, int n) {
        super(trainPath, testPath, kaggleOutput);
        this.n = n;
    }

    @Override
    public void train(String line) {
        String[] split = line.split("[ ]+");
        String tag = split[0];
        String word = split[1];
        pairs.add(new Pair(word, tag));
        System.out.println(word + " " + tag);
    }

    @Override
    public String test(String line) {
        return null;
        // TODO Auto-generated method stub

    }

    @Override
    public void execute() throws IOException {
        //pairs = ObjectSerializer.readObject("pos-train.dat");
        if(pairs == null) {
            pairs = new ArrayList<Pair>();
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
        hmm = new HMM(n, pairs);
        System.out.println("Model built");
    }

}
