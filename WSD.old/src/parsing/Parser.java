package parsing;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import com.google.common.collect.HashBiMap;


//TODO: Handle different spellings of the same word

public class Parser{

	public HashBiMap<String, Integer> wordMap = HashBiMap.create();
	public ArrayList<String> lines;
	public int positionCounter = 1;
	public int WINDOW_SIZE = 10;

	public void createHashMap() {
		for(String line : lines) {
			boolean skip = true;
			ArrayList<String> words = getWords(line);

			//TODO: handle word with @s
			for(String word : words) {
				if(skip && !word.equals("@"))
					continue;
				else if(skip && word.equals("@"))
					skip = false;
				else if(wordMap.get(word) == null) {
					wordMap.put(word, positionCounter++);
				}
			}
		}
	}

	public void writeHashMap(String filename) {
		try {
			//use buffering
			OutputStream file = new FileOutputStream(filename);
			OutputStream buffer = new BufferedOutputStream( file );
			ObjectOutput output = new ObjectOutputStream( buffer );
			try{
				output.writeObject(wordMap);
			}
			finally {
				output.close();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
     * Returns the lines of a file in an ArrayList
     * @param filename
     * @return
     */
    public ArrayList<String> getLines(String filename) {
        System.out.println("Reading File:" + filename);
        Scanner scanner = null;
        ArrayList<String> sentences = new ArrayList<String>();
        try {
            scanner = new Scanner(new FileInputStream(filename));

            while (scanner.hasNextLine())
                sentences.add(scanner.nextLine() + " ");

        } catch (FileNotFoundException e) {
            System.out.println("File not found:" + filename);
            System.exit(1);
        }

        System.out.println("Done reading file: " + filename);
        return sentences;
    }

    /**
     * Given a line, break it into a list of words.
     * @param string
     * @param order of the model
     * @return
     */
    public ArrayList<String> getWords(String line) {
        ArrayList<String> words = new ArrayList<String>(Arrays.asList(line.split(" |\n")));
        while(words.remove("")); //Eliminate multiple consecutive spaces
        return words;
    }
}
