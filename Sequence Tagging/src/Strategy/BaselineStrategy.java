package Strategy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.tartarus.snowball.TestApp;

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
        //stemm the word
        try {
            TestApp.main(word);
        } catch (Exception e) {
            System.err.println("Stemmer threw exception :(");
            e.printStackTrace();
            System.exit(1);
        }

        if (word.matches("<s>")) {
            return;
        }

        HashMap<String, Integer> T;
        if (wordsMap.containsKey(word)) {
            T = wordsMap.get(word);
            incrementMap(T, tag);
        } else {
            HashMap<String, Integer> tagsMap = new HashMap<String, Integer>();
            tagsMap.put(tag, 1);
            wordsMap.put(word, tagsMap);
        }
        incrementMap(tagCount,tag);
    }

    /**
     * Increment a hashmap of integers
     */
    private void incrementMap(HashMap<String, Integer> map, String key) {
        map.put(key, map.containsKey(key) ? map.get(key) + 1 : 1);
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

        return tag;
    }
}
