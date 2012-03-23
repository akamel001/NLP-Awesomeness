package parsing;

import java.util.ArrayList;

import com.google.common.collect.HashBiMap;

public class TrainingParser extends Parser{
	public void run() {
		lines = getLines("wsd-data/train.data");
		writeCSV("trainData");
	}

	public static void main(String args[]) {
		TrainingParser parser = new TrainingParser();
		parser.run();
	}

   public void writeCSV(String path) {
        String currentWord = "begin.v";
        SVMLightFile curFile = new SVMLightFile();
        System.out.println("Parsing word: " + currentWord);
        for(String line : lines) {
            ArrayList<String> words = getWords(line);
            if(!currentWord.equals(words.get(0))) {
                writeHashMap(path+"/"+currentWord+".map");
                curFile.write(path+"/"+currentWord+".dat");
                curFile = new SVMLightFile();
                wordMap = HashBiMap.create();
                positionCounter = 1;
                currentWord = words.get(0);
                System.out.println("Parsing word:" + currentWord);
            }

            SVMLightLine curLine = new SVMLightLine();
            String target = "";
            int i;
            for(i = 1; i < words.size() && !words.get(i).equals("@"); i++)
                target += words.get(i);
            //i now holds the position of the @
            for(int j = i + 1; j < words.size(); j++)
                if(words.get(j).startsWith("@") && words.get(j).endsWith("@")) {
                    //TODO: check for periods
                    int startPos = Math.max(i + 1, j - WINDOW_SIZE/2);
                    int endPos = Math.min(words.size() - 1, j + WINDOW_SIZE/2);
                    for(int k = startPos; k < endPos; k++) {
                        curLine.addWord(words.get(k));
                        if(!wordMap.containsKey(words.get(k)))
                            wordMap.put(words.get(k), positionCounter++);
                    }
                    break;
                }
            curLine.setTarget(target);
            curLine.setMap(wordMap);
            curFile.addLine(curLine);
        }
        writeHashMap(path+"/"+currentWord+".map");
        curFile.write(path+"/"+currentWord+".dat");
   }
}
