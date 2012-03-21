
public class TrainingParser extends Parser{
	public void run(){
		lines = getLines("wsd-data/train.data");
		createHashMap();
		writeHashMap();
		writeCSV("trainData");
	}
	
	public static void main(String args[]) {
		TrainingParser parser = new TrainingParser();
		parser.run();
	}
}
