package parsing;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import com.google.common.collect.HashBiMap;


public class TestParser extends Parser {
	public void run() {
		lines = getLines("wsd-data/test.data");
		writeCSV("trainingData", "testData");
	}

	public static void main(String args[]) {
		TestParser parser = new TestParser();
		parser.run();
	}

    public void writeCSV(String trainingPath, String testPath) {
	        String currentWord = "begin.v";
	        System.out.println("Parsing word:" + currentWord);
	        loadHashMap("trainData/begin.v.map");
	        SVMLightFile curFile = new SVMLightFile();
	        for(String line : lines) {
	            ArrayList<String> words = getWords(line);
	            if(!currentWord.equals(words.get(0))) {
	                curFile.write(testPath+"/"+currentWord+".dat");
	                curFile = new SVMLightFile();
	                wordMap = HashBiMap.create();
                    loadHashMap("trainData/" + words.get(0) +".map");
	                currentWord = words.get(0);
	                System.out.println("Parsing word:" + currentWord);
	            }

	            SVMLightLine curLine = new SVMLightLine();
	            curLine.setMap(wordMap);
	            String target = "";
	            int i;
	            for(i = 1; i < words.size() && !words.get(i).equals("@"); i++)
	                target += words.get(i);
	            for(int j = i + 1; j < words.size(); j++)
	                if(words.get(j).startsWith("@") && words.get(j).endsWith("@")) {
	                    //TODO: check for periods
	                    int startPos = Math.max(i + 1, j - WINDOW_SIZE/2);
	                    int endPos = Math.min(words.size() - 1,j + WINDOW_SIZE/2);
	                    for(int k = startPos; k < endPos; k++) {
	                        curLine.incrementWord(words.get(k));
	                    }
	                }
	            curLine.setTarget(target);
	            curFile.addLine(curLine);
	        }
	        curFile.write(testPath+"/"+currentWord+".dat");
	    }

	public void loadHashMap(String filename) {
		try {
			//use buffering
			InputStream file = new FileInputStream(filename);
			InputStream buffer = new BufferedInputStream( file );
			ObjectInput output = new ObjectInputStream( buffer );
			try{
				wordMap = (HashBiMap<String,Integer>)output.readObject();
			}
			finally {
				output.close();
			}
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
}
