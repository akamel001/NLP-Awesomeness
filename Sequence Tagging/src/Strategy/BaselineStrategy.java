package Strategy;

import java.util.HashMap;

import util.Util;

public class BaselineStrategy extends ParseStrategy {

    private HashMap<String, HashMap<String, Integer>> wordsMap = new HashMap<String, HashMap<String, Integer>>();
    private HashMap<String, String> finalMap = new HashMap<String, String>();
    private HashMap<String, Integer> tagCount = new HashMap<String, Integer>();
    private String mostCommonTag = null;

    public BaselineStrategy(String trainPath, String testPath,
            String kaggleOutput) {
        super(trainPath, testPath, kaggleOutput);
    }

    @Override
    public void train(String line) {
        String[] tokens = line.split("[ ]+");
        String tag = tokens[0];
        String word = tokens[1];

        if (word.matches("<s>")) {
            return;
        }

        Util.wordTagCount(wordsMap, word, tag);
        Util.incrementMap(tagCount,tag);
    }

    @Override
    public void postProcess() {
        Util.mostCommonTagMap(wordsMap, finalMap);
        mostCommonTag = Util.mostCommonTag(tagCount);
    }

    @Override
    public String test(String word) {
        String tag = null;
        if (finalMap.containsKey(word))
            tag = finalMap.get(word);
        else
            tag = mostCommonTag;

        return tag + " " + word;
    }
}
