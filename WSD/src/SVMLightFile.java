import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;


public class SVMLightFile {
	public ArrayList<SVMLightLine> lines = new ArrayList<SVMLightLine>();
	
	public void addLine(SVMLightLine newLine) {
		lines.add(newLine);
	}

	public void write(String filename) {
	    try{
	        // Create file
	        FileWriter fstream = new FileWriter(filename);
	        BufferedWriter out = new BufferedWriter(fstream);
	        for(SVMLightLine line : lines) {
	            out.write(line.toString() + "\n");
	        }
	        out.close();
	    } catch (Exception e) {
	        System.err.println("Error writing " + filename + ": " + e.getMessage());
	    }
	}
}
