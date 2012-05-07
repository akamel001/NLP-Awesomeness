package train;

import java.util.ArrayList;
import java.util.HashMap;

import util.GuessModel;
import util.QuestionType;
import util.QuestionType.Type;

public class DummyTrainer extends TrainModel
{

	@Override
	public HashMap<Type, GuessModel> train_models(final HashMap<String, String> questions_answers)
	{
		HashMap<Type, GuessModel> models = new HashMap<Type, GuessModel>();
		
		models.put(QuestionType.Type.WHERE, make_dummy_model());
		models.put(QuestionType.Type.WHEN, make_dummy_model());
		models.put(QuestionType.Type.HOW, make_dummy_model());
		models.put(QuestionType.Type.WHO_IS, make_dummy_model());
		models.put(QuestionType.Type.WHO_OTHER, make_dummy_model());
		models.put(QuestionType.Type.OTHER, make_dummy_model());
		
		return models;
	}

	private GuessModel make_dummy_model()
	{
		GuessModel dummy_model = new GuessModel();
		
		ArrayList<String> temp_list = new ArrayList<String>();
		temp_list.add("NN");
		temp_list.add("NN");
		temp_list.add("NN");
		dummy_model.pos_sequences.put(new PosSequence(temp_list), 1);
		
		temp_list = new ArrayList<String>();
		temp_list.add("NNP");
		temp_list.add("NNP");
		temp_list.add("NNP");
		
		return dummy_model;
	}
	
}
