package util;

import java.util.HashMap;

public class Util {

    public static void incrementMap(HashMap<String, Integer> map, String key) {
        map.put(key, map.containsKey(key) ? map.get(key) + 1 : 1);
    }

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
}
