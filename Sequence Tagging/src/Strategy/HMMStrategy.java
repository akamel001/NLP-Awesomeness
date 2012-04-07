package Strategy;

import java.util.LinkedList;

import HMM.HMM;
import HMM.HMM.Pair;

public class HMMStrategy extends ParseStrategy {

    private LinkedList<Pair> pairs = new LinkedList<Pair>();
    private int n;

    public HMMStrategy(String trainPath, String testPath, String kaggleOutput, int n) {
        super(trainPath, testPath, kaggleOutput);
        this.n = n;
    }

    @Override
    public void train(String line) {
        String[] split = line.split("[ ]+");
        String word = split[0];
        String tag = split[1];
        pairs.add(new Pair(word, tag));
    }

    @Override
    public String test(String line) {
        return null;
        // TODO Auto-generated method stub

    }

    @Override
    public void postProcess() {
        HMM hmm = new HMM(n, pairs);
    }

}
