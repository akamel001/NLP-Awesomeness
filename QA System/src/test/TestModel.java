package test;

import java.util.ArrayList;
import java.util.HashMap;

import util.*;

public abstract class TestModel
{
	public abstract ArrayList<ArrayList<Guess>> get_guesses(ArrayList<String> test_file_questions, HashMap<QuestionType.Type, GuessModel> models, int start_index);
}
