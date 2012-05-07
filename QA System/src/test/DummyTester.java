package test;

import java.util.ArrayList;
import java.util.HashMap;

import util.Guess;
import util.GuessModel;
import util.QuestionType.Type;

public class DummyTester extends TestModel
{

	public ArrayList<ArrayList<Guess>> get_guesses(ArrayList<String> test_file_questions,HashMap<Type, GuessModel> models, int start_index)
	{
		ArrayList<ArrayList<Guess>> all_guesses = new ArrayList<ArrayList<Guess>>();
		
		for (int i = 0; i < test_file_questions.size(); i++)
		{
			ArrayList<Guess> current_question_guesses = new ArrayList<Guess>();
			for (int j = 0; j < 5; j++)
			{
				current_question_guesses.add(new Guess("DOC_NAME_HERE", "nil"));
			}
			all_guesses.add(current_question_guesses);
		}
		
		return all_guesses;
	}
	


}
