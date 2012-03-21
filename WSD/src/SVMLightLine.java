import java.util.HashMap;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class SVMLightLine {
	public HashBiMap<String, Integer> wordMap = HashBiMap.create();
	public HashMap<String, Integer> wordCounts = new HashMap<String, Integer>();
	public String target;
	
	public SVMLightLine(HashBiMap<String, Integer> wordMap) {
		this.wordMap = wordMap;
	}
	
	public void setTarget(String target) {
		this.target = target;
	}
	
	public void addWord(String word) {
		if(wordCounts.containsKey(word)) {
			wordCounts.put(word, wordCounts.get(word) + 1);
		} else {
			wordCounts.put(word, 1);
		}
	}
	
	public String toString() {
		BiMap<Integer, String> inverseMap = wordMap.inverse();
		StringBuilder myString = new StringBuilder(target + " ");
		for(int i = 0; i < inverseMap.size() - 1; i++) {
			Integer value = wordCounts.get(inverseMap.get(i));
			myString.append(i + ":" + ((value == null) ? 0 : value) + " ");
		}
		Integer value = wordCounts.get(inverseMap.get(inverseMap.size() - 1));
		myString.append(inverseMap.size() - 1 + ":" + (value == null ? 0 : value));
		return myString.toString();
	}
}
