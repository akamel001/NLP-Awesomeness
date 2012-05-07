package test;

import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import util.Guess;
import util.GuessModel;
import util.Parser;
import util.QuestionType;

public class BaselineTester extends TestModel
{
	private final static String DOC_PREFIX = "docs/top_docs.";
	
	public ArrayList<ArrayList<Guess>> get_guesses(ArrayList<String> test_file_questions, HashMap<QuestionType.Type, GuessModel> models, int start_index)
	{
		String fileText;
		String[] headTags = new String[] {"HEADLINE", "H3", "HL", "FIRST", "HEAD"};
		Document aDoc;
		Elements headlines;
		int tagNum;
		
		ArrayList<ArrayList<Guess>> all_guesses = new ArrayList<ArrayList<Guess>>();
		ArrayList<Guess> current_guesses;
		
		for(int i = 0; i < test_file_questions.size(); i++)
		{
			current_guesses = new ArrayList<Guess>();
			fileText = Parser.get_file(DOC_PREFIX + (start_index + i));
			aDoc = Jsoup.parse(fileText);
			headlines = null;
			tagNum = 0;
			while(headlines == null || headlines.isEmpty() && tagNum < headTags.length)
            {
                headlines = aDoc.getElementsByTag(headTags[tagNum++]);
            }
			if(!headlines.isEmpty())
            {
                int j;
                for(j = 0; j < Math.min(5, headlines.size()); j++)
                {
                	current_guesses.add(new Guess(DOC_PREFIX + (start_index + i),headlines.get(j).text()));
                }
                for(; j < 5; j++)
                {
                	current_guesses.add(new Guess(DOC_PREFIX + (start_index + i),"nil"));
                }
            }
			else
			{
				for(int j = 0; j < 5; j++)
				{
					current_guesses.add(new Guess(DOC_PREFIX + (start_index + i),"nil"));
				}
			}
			all_guesses.add(current_guesses);
		}
		return all_guesses;
	}
}
