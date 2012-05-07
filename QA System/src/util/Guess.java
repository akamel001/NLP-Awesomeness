package util;

// Represents a single guess for a question
public class Guess
{
	public String doc; // The document number the guess was draqn from
	public String guess; // The guess itself
	
	public Guess()
	{}
	
	public Guess(String doc, String guess)
	{
		this.doc = doc;
		this.guess = guess;
	}
	
}
