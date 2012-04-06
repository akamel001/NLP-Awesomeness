package Strategy;

public class HMMStrategy extends ParseStrategy {

    public HMMStrategy(String trainPath, String testPath, String kaggleOutput) {
        super(trainPath, testPath, kaggleOutput);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void train(String line) {
        String[] split = line.split("[ ]+");
        String word = split[0];
        String tag = split[1];

    }

    @Override
    public String test(String line) {
        return null;
        // TODO Auto-generated method stub

    }

    @Override
    public void postProcess() {
        // TODO Auto-generated method stub

    }

}
