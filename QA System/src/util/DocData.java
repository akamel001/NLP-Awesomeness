package util;

import java.util.ArrayList;

// Represents a single document
public class DocData
{
	public String doc_num; // The document ID number (contains both letters and numbers)
	public double score; // The IR score of the document
	public ArrayList<String> full_doc_text; // The full text of the document, by word
	
	public DocData()
	{
		full_doc_text = new ArrayList<String>();
	}
}