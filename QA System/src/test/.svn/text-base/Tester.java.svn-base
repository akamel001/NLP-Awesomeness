package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import main.Answerer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import train.PosSequence;
import util.DocData;
import util.Guess;
import util.GuessModel;
import util.Parser;
import util.QuestionType;
import util.QuestionType.Type;

public class Tester extends TestModel
{
	private static final int WINDOW_SIZE_MULTIPLIER = 4;
	private static final double DISTANCE_MULTIPLIER = 15;
	private static final double DOC_MULTIPLIER = 0;
	
	private static final String METADATA_REGEX = "Qid:[ \t]*[0-9]*[ \t]*Rank:[ \t]*[0-9]*[ \t]*Score:[ \t]*[0-9]*.[0-9]*";
	
	private static Pattern metadata_pattern;
    
    public class Triple implements Comparable<Triple>{
    	public int document;
    	public ArrayList<String> answer;
    	public double score;
    	
    	Triple(int d, ArrayList<String> ans, double scr) {
    		this.document = d;
    		this.answer = ans;
    		this.score = scr;
    	}
    	
		@Override
		public int compareTo(Triple t) {
			if (this.score < t.score)
				return 1;
			else if (this.score > t.score)
				return -1;
			else return 0;
		}
    	
    }
	
	public Tester()
	{		
		metadata_pattern = Pattern.compile(METADATA_REGEX);
	}
	
	public ArrayList<ArrayList<Guess>> get_guesses(ArrayList<String> test_file_questions, HashMap<Type, GuessModel> models, int start_index)
	{
		ArrayList<ArrayList<Guess>> guesses = new ArrayList<ArrayList<Guess>>();
		for(int i = 0; i < test_file_questions.size(); i++)
		{
			System.out.printf("Question %d\n", start_index + i + 1);
			
			String current_question = test_file_questions.get(i);
			GuessModel current_model = models.get(QuestionType.classify(current_question));
			// Get content words from question
			HashSet<String> content_words = Parser.get_content_words(current_question);
			
			// Parse document file to get full text and metadata of all documents
			ArrayList<DocData> docs = get_docs(start_index + i);

			// calculate window and subwindow size
			int max = 0;
			for(PosSequence ps : current_model.pos_sequences.keySet()) {
				if(ps.sequence.size() > max)
					max = ps.sequence.size();
			}
			int window_size = max*WINDOW_SIZE_MULTIPLIER;
			
			//  all occurrences of content words in documents
			// Get windows around occurrences of content words
			//int threshold = (int)Math.ceil((double)content_words.size()/2.0);
			int threshold = content_words.size();
			ArrayList<ArrayList<ArrayList<String>>> windows_by_doc = new ArrayList<ArrayList<ArrayList<String>>>();
			for(int j = 0; j < docs.size(); j++)
			{
				windows_by_doc.add(findWindows(content_words, docs.get(j).full_doc_text, window_size, threshold));
			}
			
			
			ArrayList<Guess> out = new ArrayList<Guess>();
			
			ArrayList<Triple> results = new ArrayList<Triple>();
			for(int j = 0; j < windows_by_doc.size(); j++) {
				HashMap<ArrayList<String>, Double> tempResults = findSequencesInWindows(windows_by_doc.get(j),current_model,content_words);

				for (Entry<ArrayList<String>, Double> e : tempResults.entrySet()) {
					//System.out.printf("%.4f ---- %.4f\n", e.getValue()*DISTANCE_MULTIPLIER, docs.get(j).score*DOC_MULTIPLIER);
					results.add(new Triple(j, e.getKey(), e.getValue()*DISTANCE_MULTIPLIER + docs.get(j).score*DOC_MULTIPLIER));
				}
			}
			
			if(results.size() == 0)
			{
				for(int j = 0; j < 5; j++)
				{
					DocData current_doc = docs.get(j);
					String guess = current_doc.full_doc_text.get(0);
					boolean correct_size = false;
					int k = 1;
					
					while(!correct_size)
					{
						if(guess.length() + current_doc.full_doc_text.get(k).length() + 1 <= 50)
						{
							guess += String.format(" %s", current_doc.full_doc_text.get(k));
							k++;
						}
						else
						{
							correct_size = true;
						}
					}
					
					out.add(new Guess(current_doc.doc_num, guess));
				}
			}	
			else
			{
				Collections.sort(results);
				if (results.size() < 5) {
					ArrayList<Triple> top = new ArrayList<Triple>(results.subList(0, results.size()));
					for (Triple t : top) {
						out.add(new Guess(docs.get(t.document).doc_num, Parser.lstToString(t.answer)));
					}
				}
				else {
					ArrayList<Triple> top = new ArrayList<Triple>(results.subList(0, 5));
					for (Triple t : top) {
						out.add(new Guess(docs.get(t.document).doc_num, Parser.lstToString(t.answer)));
					}
				}
			}
			
			guesses.add(out);
			
		}
		
		return guesses;
	}
	
	
	/*
	 * Find windows in a document, and return them as an array of arraylists
	 */
	public ArrayList<ArrayList<String>> findWindows( HashSet<String> words, ArrayList<String> docWords, int window_size, int threshold ) {
		
		// will return ArrayList<ArrayList> of windows
		
		ArrayList<ArrayList<String>> windows = new ArrayList<ArrayList<String>>();
			
		while(windows.isEmpty() && threshold > 0) {
			for (int i=0;i<docWords.size() - window_size;i++) {
				
				// create temp window
				ArrayList<String> window = new ArrayList<String>();
				
				for (String w : docWords.subList(i, i + window_size + 1))
					window.add(w.toLowerCase());
				
				boolean proceed = false;
				for (String checkWord : words) {
					if (window.contains(checkWord))
						proceed = true;
				}
				
				if (proceed) {
					int successCount = 0;
					// loop:
					for (String windowWord : window) {
						for (String targetWord : words) {
							if (targetWord.equals(windowWord)) {
								successCount += 1;
								if (successCount >= threshold) {
									windows.add(window);
									// break loop;
								}
							}
						}
					}
				}	
			}
			threshold--;
		}
		return windows;
	}	
	
	private ArrayList<DocData> get_docs(int question_number)
	{
		String file_name = String.format("docs/top_docs.%d", question_number);
		ArrayList<String> file_lines = Parser.get_file_lines(file_name);
		String doc_text = Parser.get_file(file_lines);
		Document all_ques_docs = Jsoup.parse(doc_text);
		Elements docs = all_ques_docs.getElementsByTag("DOC");
		
		ArrayList<DocData> all_docs = new ArrayList<DocData>();
		
		DocData current_doc;
		
		String line;
		int doc_index = 0;
		
		for(Element e : docs)
		{
			current_doc = new DocData();
			
			add_all_words(current_doc.full_doc_text, e.getElementsByTag("HEADLINE").first());
			add_all_words(current_doc.full_doc_text, e.getElementsByTag("H3").first());
			add_all_words(current_doc.full_doc_text, e.getElementsByTag("HL").first());
			add_all_words(current_doc.full_doc_text, e.getElementsByTag("FIRST").first());
			add_all_words(current_doc.full_doc_text, e.getElementsByTag("HEAD").first());
			add_all_words(current_doc.full_doc_text, e.getElementsByTag("CAPTION").first());
			add_all_words(current_doc.full_doc_text, e.getElementsByTag("LEADPARA").first());
			add_all_words(current_doc.full_doc_text, e.getElementsByTag("TEXT").first());
			
			
			current_doc.doc_num = e.getElementsByTag("DOCNO").first().text().trim();
			
			all_docs.add(current_doc);
		}
		
		
		
		for(int i = 0; i < file_lines.size(); i++)
		{
			line = file_lines.get(i);
			if(metadata_pattern.matcher(line.trim()).matches())
			{
				all_docs.get(doc_index).score = Double.parseDouble(line.substring(line.indexOf("Score:") + 6).trim());
				doc_index++;
			}
		}
		
		return all_docs;
	}

	private static void add_all_words(ArrayList<String> word_list, Element source)
	{ 		
		if(source != null)
		{
			String[] words = source.text().trim().split(" ");
			for(int i = 0; i < words.length; i++)
			{
				word_list.addAll(Parser.split_punctuation_from_word(words[i]));
			}
		}
	}
	
	public HashMap<ArrayList<String>, Double> findSequencesInWindows( ArrayList<ArrayList<String>> windows, GuessModel g, HashSet<String> content_words) {

		HashMap<PosSequence, Integer> posSequences = g.pos_sequences;
		HashMap<ArrayList<String>, Double> output = new HashMap<ArrayList<String>,Double>();
		// for each window
		for (ArrayList<String> window : windows) {
			
			//TODO
			
			int num_content_words = 0;
			for(String word : window)
			{
				for(String content_word : content_words)
				{
					if(word.equals(content_word))
					{
						num_content_words++;
					}
				}
			}
			
			//TODO
			
			String[] arrWindow = new String[window.size()];
			
			for(int j = 0; j < window.size(); j++) {
				arrWindow[j] = window.get(j);
			}
			
			ArrayList<String> windowSeq = new ArrayList<String>( Arrays.asList( Answerer.tagger.tag( arrWindow ) ) );
			// for each answer format
			for (PosSequence p : posSequences.keySet()) {
				//for each subsequence
				for (int k=0; k < windowSeq.size() ; k++) {
				
					if ( k <= windowSeq.size() - p.sequence.size()) {
						ArrayList<String> subsequence = new ArrayList<String>(windowSeq.subList(k, k + p.sequence.size()));
						//TODO
						//double score = fuzzyMatch(subsequence,p.sequence)*(double)subsequence.size();
						double score = fuzzyMatch(subsequence,p.sequence)*(double)subsequence.size()*num_content_words*posSequences.get(p);
						//TODO
						// how much can you add?
						short byteSize = 0;
						ArrayList<String> superWindow = new ArrayList<String>();
						for (int i=k;i<k+p.sequence.size();i++) {
							String w = window.get(i);
							byteSize += w.length();
			
							if (byteSize > 50)
								break;
							
							// got here, word fits
							superWindow.add(w);
						}
						
						if (byteSize < 50) {
							int remainingSpace = 50 - byteSize;
							for (int i=k-1;i>=0 && remainingSpace > 0;i--) {
								String w = window.get(i);
								remainingSpace -= w.length();
								
								if (remainingSpace < 0)
									break;
								
								// got here, word fits
								superWindow.add(0, w);
							}
							
							for (int i=k+p.sequence.size();i<window.size() && remainingSpace > 0;i++) {
								String w = window.get(i);
								remainingSpace -= w.length();
								
								if (remainingSpace < 0)
									break;
								
								// got here, word fits
								superWindow.add(w);
							}
						}
						
						output.put(superWindow,score);
					}
				}
			}
		}
		return output;
	}

	/**calculates the inverse of the levenshtein distance between two string sequences (i.e. fuzzy matching--the higher the score, the better the match)
	 * 
	 * @param input the sequence you want to test
	 * @param model the sequence to test again
	 * @return the inverse levenshtein distance
	 */
	public static double fuzzyMatch(ArrayList<String> input, ArrayList<String> model) {
		int[][] matrix = new int[input.size()][model.size()];
		for (int i=0; i < input.size(); i++) {
			matrix[i][0] = i;
		}
		for (int i=0; i < model.size(); i++) {
			matrix[0][i] = i;
		}
		
		for(int i = 1; i < input.size(); i++)
		{
			for(int j = 1; j < model.size(); j++) {
				if(input.get(i) == model.get(j))
					matrix[i][j] = matrix[i-1][j-1];
				else {
					matrix[i][j] = Math.min(Math.min(matrix[i-1][j] + 1, matrix[i][j-1] + 1), matrix[i-1][j-1] + 1);
				}
			}
		}
		return 1.0/(1.0 +(double)matrix[input.size() -1][model.size() -1]);
	}
}
