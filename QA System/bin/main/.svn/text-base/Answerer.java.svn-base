package main;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import train.*;
import test.*;
import util.*;

// The main class that will be run
// Takes in question and answer files for training and test sets,
// then trains models, makes guesses, and checks accuracy of output
public class Answerer
{
	// Question and Answer files for Training
	private static final String TRAIN_QUESTION_FILE_NAME = "res/training_questions.txt";
	private static final String TRAIN_ANSWER_FILE_NAME = "res/training_answers.txt";

	// Question and Answer files for Testing
	private static final String TEST_QUESTION_FILE_NAME = "res/validation_questions.txt";
	private static final String TEST_ANSWER_FILE_NAME = "res/validation_answers.txt";
	
	// Files to output guesses and results to
	private static final String TEST_OUTPUT_FILE_NAME = "out/kaggle.csv";
	private static final String VALIDATION_ERROR_FILE_NAME = "out/errors.txt";
	
	// TODO: parse question numbers, and remove this constant
	// Question number for first question in the answer set
	private static final int TESTING_SET_START = 400;
	
	// The training and testing models currently being used
	private static TrainModel training_model;
	private static TestModel testing_model;
	
	// The models keeping track of POS sequences of the answers of each question type
	private static HashMap<QuestionType.Type, GuessModel> question_models; 
	
	// POS Tagging objects
	public static InputStream modelIn = null;
	public static POSModel model = null;
	public static POSTaggerME tagger = null;
	
	// Main function: Do initialization, then training, then validation
	public static void main(String[] args)
	{
		init();
		train();
		validate();
	}
	
	// Initialization function: Initializes important objects
	private static void init()
	{
		// Initialize Parser
		Parser.init();
		
		// Initialize Training Model
		//training_model = new DummyTrainer();
		training_model = new Trainer();
		
		// Initialize Testing Model
		//testing_model = new DummyTester();
		testing_model = new Tester();
		//testing_model = new BaselineTester();
		
		// Initialize POS Tagging Objects
		try
    	{
    		modelIn = new FileInputStream("lib/OpenNLP/bin/en-pos-maxent.bin");
        	model = new POSModel(modelIn);
        	tagger = new POSTaggerME(model);
    	}
    	catch(IOException e)
		{
			System.out.println("IOException in Tagger Initialization");
			System.out.println(e.getMessage());
		}
	}
	
	// Training function: Builds and stores GuessModels
	private static void train()
	{
		// Get questions and answers for training set
		List<Answer> questions_with_answers = Parser.parse_answers(TRAIN_ANSWER_FILE_NAME);
		
		// Store questions and associated answers in a HashMap
		HashMap<String, String> questions_to_answers = new HashMap<String, String>();
		String current_question;
		for(Answer a : questions_with_answers)
		{
			current_question = a.getQuestion();
			for(String ans : a.getAnsList())
			{
				while(questions_to_answers.containsKey(current_question))
				{
					current_question += " ";
				}
				questions_to_answers.put(current_question, ans);
			}
		}
		
		// Train and store question models
		question_models = training_model.train_models(questions_to_answers);
	}
	
	// Validation Function: Makes guesses using the given TestModel and then scores them and outputs a guess and error file
	private static void validate()
	{
		// Get all questions
		ArrayList<String> questions = Parser.parse_questions(TEST_QUESTION_FILE_NAME);
		// Make guesses using testing model
		ArrayList<ArrayList<Guess>> guesses = testing_model.get_guesses(questions, question_models, TESTING_SET_START);
		// Write guesses to file
		write_guesses(guesses);
		
		// Get all answers
		List<Answer> answers = Parser.parse_answers(TEST_ANSWER_FILE_NAME);
		
		int correct_guesses = 0; // Number of questions which have a non-zero MRR Score
		double total_score = 0; // Sum of all MRR scores
		double current_score; // MRR Score for current question
		
		ArrayList<String> output_file_lines = new ArrayList<String>(); // Lines to output to the file
		
		BufferedWriter out;
		
		for(int i = 0; i < guesses.size(); i++)
		{
			current_score = Scorer.score(answers.get(i), guesses.get(i)); // Look up score for current question
			total_score += current_score; // Add to total score
			if(current_score > 0) // If MRR is greater than zero, add one to questions guessed correctly 
			{
				correct_guesses += 1;
			}
			
			// Add Question, Score, Answer(s) and Guesses to output list
			output_file_lines.add("----------------------------------------------------------------------------");
			output_file_lines.add(String.format("Question %d:",TESTING_SET_START + i));
			output_file_lines.add(String.format("\t%s", answers.get(i).getQuestion()));
			output_file_lines.add("Score: ");
			output_file_lines.add(String.format("\t%.3f", current_score));
			output_file_lines.add("Answer(s):");
			for(int j = 0; j < answers.get(i).getAnsList().size(); j++)
			{
				output_file_lines.add(String.format("\t%s", answers.get(i).getAnsList().get(j)));
			}
			output_file_lines.add("Guesses:");
			for(int j = 0; j < guesses.get(i).size(); j++)
			{
				output_file_lines.add(String.format("\t%s", guesses.get(i).get(j).guess));
			}			
		}
		
		// TODO: Get overall MRR Score
		//total_score = total_score / ((double)guesses.size());
		total_score = total_score / ((double)correct_guesses);
		
		try
		{
			out = new BufferedWriter(new FileWriter(VALIDATION_ERROR_FILE_NAME));
			
			// Write header of file containing overall statistics
			out.write(String.format("Questions Answered: %d", guesses.size()));
			out.newLine();
			out.write(String.format("Questions (Partially) Correct: %d", correct_guesses));
			out.newLine();
			out.write(String.format("(Partially) Correct Percentage: %.3f", (((double)correct_guesses)/((double)guesses.size()))*100.0));
			out.newLine();
			out.write(String.format("Total Score: %.3f", total_score));
			out.newLine();
			
			// Write results for each question
			for(int i = 0; i < output_file_lines.size(); i++)
			{
				out.write(output_file_lines.get(i));
				out.newLine();
			}
			
			out.close();
		}
		catch(IOException e)
		{
			System.out.println("IOException in Validation File Writing");
			System.out.println(e.getMessage());
		}
	}
	
	// Writes guesses to a file
	private static void write_guesses(ArrayList<ArrayList<Guess>> guesses)
	{
		ArrayList<Guess> current_ques_guesses;
		Guess current_guess;
		
		BufferedWriter out;
		
		try
		{
			out = new BufferedWriter(new FileWriter(TEST_OUTPUT_FILE_NAME));
			for(int i = 0; i < guesses.size(); i++)
			{
				current_ques_guesses = guesses.get(i);
				for(int j = 0; j < current_ques_guesses.size(); j++)
				{
					current_guess = current_ques_guesses.get(j); 
					out.write(String.format("%d %s %s\n", TESTING_SET_START + i, current_guess.doc, current_guess.guess));
				}
			}
			out.close();
		}
		catch(IOException e)
		{
			System.out.println("IOException in Answer Writing");
			System.out.println(e.getMessage());
		}
		
	}
}
