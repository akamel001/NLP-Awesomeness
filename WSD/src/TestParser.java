import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

import com.google.common.collect.HashBiMap;


public class TestParser extends Parser{
	public void run(){
		lines = getLines("wsd-data/test.data");
		//createHashMap();
		//writeHashMap();
		loadHashMap();
		writeCSV("testData");
	}
	
	public static void main(String args[]) {
		TestParser parser = new TestParser();
		parser.run();
	}
	
	public void loadHashMap() {
		try {
			//use buffering
			InputStream file = new FileInputStream("wordMap.dat");
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
		}
	}
}
