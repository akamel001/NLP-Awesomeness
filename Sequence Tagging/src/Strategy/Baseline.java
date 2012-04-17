package Strategy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import util.Util;

public class Baseline extends Parser {

    private HashMap<String, HashMap<String, Integer>> wordsMap = new HashMap<String, HashMap<String, Integer>>();
    private HashMap<String, String> finalMap = new HashMap<String, String>();
    private HashMap<String, Integer> tagCount = new HashMap<String, Integer>();
    private String mostCommonTag = null;

    public Baseline(String trainPath, String testPath,
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
        String tag = "";
        String word;
        Iterator<Entry<String, HashMap<String, Integer>>> e = wordsMap.entrySet().iterator();
        while (e.hasNext()) {
            Map.Entry<String, HashMap<String, Integer>> entry = (Entry<String, HashMap<String, Integer>>) e.next();

            word = entry.getKey();
            System.out.print("word = " + word);

            HashMap<String, Integer> temp = entry.getValue(); //file tree map
            Iterator<Entry<String, Integer>> ent = temp.entrySet().iterator();
            int count = 0;
            while (ent.hasNext()) {
                Map.Entry<String, Integer> entr = (Map.Entry<String, Integer>) ent.next();

                if (entr.getValue() >= count) {
                    tag = entr.getKey();
                    count = entr.getValue();
                }
            }
            System.out.print("  tag = " + tag);
            System.out.println("  count = " + count);
            finalMap.put(word, tag);
        }
        String maxKey = null;
        for (Entry<String, Integer> entry : tagCount.entrySet()) {
            int maxValue = -1;
            if(entry.getValue() > maxValue) {
                maxValue = entry.getValue();
                maxKey = entry.getKey();
            }
        }
        mostCommonTag = maxKey;
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

    public static void main(String[] args) throws IOException {
        Baseline strategy = new Baseline("pos_files/train.pos", "pos_files/test-obs.pos", "kaggle.csv");
        strategy.execute();
    }
}
