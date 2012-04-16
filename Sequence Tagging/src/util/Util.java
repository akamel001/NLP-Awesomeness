package util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Util {

    public static void incrementMap(HashMap<String, Integer> map, String key) {
        map.put(key, map.containsKey(key) ? map.get(key) + 1 : 1);
    }

    /**
     * Creates a map from word to count of tags with that word
     * @param wordsMap
     * @param word
     * @param tag
     */
    public static void wordTagCount(HashMap<String, HashMap<String, Integer>> wordsMap, String word, String tag) {
        HashMap<String, Integer> wordTagCount;
        if (wordsMap.containsKey(word)) {
            wordTagCount = wordsMap.get(word);
            Util.incrementMap(wordTagCount, tag);
        } else {
            HashMap<String, Integer> tagsMap = new HashMap<String, Integer>();
            tagsMap.put(tag, 1);
            wordsMap.put(word, tagsMap);
        }
    }

    /**
     * Creates a map from tag, to count of words with that tag
     * @param tagsMap
     * @param word
     * @param tag
     */
    public static void tagWordCount(HashMap<String, HashMap<String, Integer>> tagsMap, String word, String tag) {
        wordTagCount(tagsMap, tag, word);
    }

    public static String mostCommonTag(HashMap<String, Integer> tagCount) {
        String maxKey = null;
        for (Entry<String, Integer> entry : tagCount.entrySet()) {
            int maxValue = -1;
            if(entry.getValue() > maxValue) {
                maxValue = entry.getValue();
                maxKey = entry.getKey();
            }
        }
        return maxKey;
    }

    public static void mostCommonTagMap(HashMap<String, HashMap<String, Integer>> wordsMap, HashMap<String, String> finalMap) {
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
    }
}
