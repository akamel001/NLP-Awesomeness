package parsing;
import java.util.HashMap;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class SVMLightLine {
	public HashBiMap<String, Integer> wordMap = HashBiMap.create();
	public HashMap<String, Integer> wordCounts = new HashMap<String, Integer>();
	public String target;


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

	public void incrementWord(String word) {
	    if(wordMap.containsKey(word)) {
	        if(wordCounts.containsKey(word)) {
	            wordCounts.put(word, wordCounts.get(word) + 1);
	        } else {
	            wordCounts.put(word, 1);
	        }
	    }
	}

	public void setMap(HashBiMap<String, Integer> wordMap) {
	    this.wordMap = wordMap;
	}

	@Override
    public String toString() {
		BiMap<Integer, String> inverseMap = wordMap.inverse();
		StringBuilder myString = new StringBuilder(target + " ");
		for(int i = 0; i < inverseMap.size(); i++) {
			Integer value = wordCounts.get(inverseMap.get(i));

			/*if(value == null)
				continue;
			else*/
				myString.append((i+1) + ":" + (value == null ? 0 : value) + " ");
		}
		return myString.toString();
	}
}
