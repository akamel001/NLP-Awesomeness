package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.Answerer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

// Contains several static functions for parsing Strings and documents
public class Parser {

	// Regular Expression patterns representing document numbers
    private static Pattern[] doc_patterns;

	// All tags representing closed classes
	public static java.util.HashSet<String> closed_class_tags;
	
	// All punctuation we are ignoring
	public static java.util.HashSet<Character> punctuation;
	
	// All words we are ignoring
	public static java.util.HashSet<String> word_blacklist;
    
    // Question Parser
    public static void init()
    {
    	// Initialize document number regex patterns
        doc_patterns = new Pattern[6];
        doc_patterns[0] = Pattern.compile("FT[0-9]{3}-[0-9]*");
        doc_patterns[1] = Pattern.compile("WSJ[0-9]{6}-[0-9]*");
        doc_patterns[2] = Pattern.compile("SJMN[0-9]{2}-[0-9]*");
        doc_patterns[3] = Pattern.compile("AP[0-9]{6}-[0-9]*");
        doc_patterns[4] = Pattern.compile("LA[0-9]{6}-[0-9]*");
        doc_patterns[5] = Pattern.compile("FBIS[0-9]-[0-9]*");
        
        // Initialize closed class tag set (to ignore as content words)
		closed_class_tags = new java.util.HashSet<String>();
		closed_class_tags.add("CC"); 	// Coordinating Conjunction
		closed_class_tags.add("DT"); 	// Determiner
		closed_class_tags.add("EX"); 	// Existential "There"
		closed_class_tags.add("IN"); 	// Preposition
		closed_class_tags.add("MD"); 	// Modal
		closed_class_tags.add("PDT"); 	// Pre-Determiner
		closed_class_tags.add("POS"); 	// Possessive
		closed_class_tags.add("PRP"); 	// Personal Pronoun
		closed_class_tags.add("PRP$"); 	// Possessive Pronoun
		closed_class_tags.add("TO"); 	// The Word "To"
		closed_class_tags.add("WDT"); 	// Wh-Determiner
		closed_class_tags.add("WP"); 	// "Wh" Pronoun
		closed_class_tags.add("WP$"); 	// "Wh" Possessive Pronoun
		closed_class_tags.add("WRB"); 	// "Wh" Adverb
		closed_class_tags.add("<s>"); 	// Sentence Start
		closed_class_tags.add("."); 	// Period or Sentence End
		closed_class_tags.add(","); 	// Comma
		closed_class_tags.add(":"); 	// Colon or Semicolon
		closed_class_tags.add("''"); 	// Double Quotation Marks
		closed_class_tags.add("``"); 	// Slanted Double Quotation Marks
		closed_class_tags.add("#"); 	// Number Sign
		closed_class_tags.add("$"); 	// Currency Sign
		
		// Initialize set of punctuation characters
		punctuation = new java.util.HashSet<Character>();
		punctuation.add('.');
		punctuation.add('?');
		punctuation.add('!');
		punctuation.add(',');
		punctuation.add(':');
		punctuation.add(';');
		punctuation.add('"');
		punctuation.add('\'');
		punctuation.add('`');
		
		// Initialize list of non-closed-class words to ignore as content words
		word_blacklist = new java.util.HashSet<String>();
		word_blacklist.add("is");
		word_blacklist.add("was");
		word_blacklist.add("are");
		word_blacklist.add("were");

    }

    // Parse a question file for an ArrayList of Strings representing questions
    public static ArrayList<String> parse_questions(final String file_name)
    {
        final ArrayList<String> questions = new ArrayList<String>();
        final String fileText = get_file(file_name);
        final Document qDoc = Jsoup.parse(fileText);
        final Elements tags = qDoc.getElementsByTag("top");
        for (final Element e : tags) {
            final Element topContent = e.getElementsByTag("num").first();
            final String desc = e.getElementsByTag("desc").first().text()
                    .split(":")[1];
            // XML is malformed, so getting the num will contain the desc too
            // We remove it to just get the number text
            topContent.childNodes().get(1).remove();
            questions.add(desc);
        }
        return questions;

    }

    // Parse an answer file for an ArrayList of Answer objects representing a question and the potential answers
    public static List<Answer> parse_answers(final String file_name) {

        final List<Answer> answers = new ArrayList<Answer>();
        final File file = new File(file_name);

        try {
            final Scanner scanner = new Scanner(new FileReader(file));
            while (scanner.hasNext())
                answers.add(process_question(scanner));
        } catch (final FileNotFoundException e) {
            System.out.print("cannot find file");
        }
        return answers;
    }

    // Parse and individual answer
    private static Answer process_question(final Scanner s) {

        final String questionNum = s.nextLine();
        final String question = s.nextLine();
        final String doc = s.nextLine();
        final String an = s.nextLine();
        
        final Answer ans = new Answer(questionNum, question, doc, an);
        String next = s.nextLine();
        while (!next.isEmpty())
        {
            if (matches_doc_pattern(next))
            {
                final LinkedList<String> answers = new LinkedList<String>();
                final String add = s.nextLine();
                answers.add(add);
                ans.docs_to_ans.put(next, answers);
            }
            else
            {
                ans.docs_to_ans.get(doc).add(next);
            }
            next = s.nextLine();
        }
        return ans;
    }

    // Checks if a string is a document number using regular expressions 
    private static boolean matches_doc_pattern(final String str) {
        Matcher ma;

        for (final Pattern doc : doc_patterns) {
            ma = doc.matcher(str);
            if (ma.matches())
                return ma.matches();
        }
        return false;
    }

    // Gets all lines of a file as a single string, given a file name
    public static String get_file(final String filename) {
        final ArrayList<String> file_lines = get_file_lines(filename);
        final StringBuilder plaintext = new StringBuilder();

        for (int i = 0; i < file_lines.size(); i++)
            plaintext.append(file_lines.get(i));

        return plaintext.toString();
    }

    // Gets all lines of a file as a single string, given an ArrayList of Strings representing all lines of the file
    public static String get_file(final ArrayList<String> file_lines) {
        final StringBuilder plaintext = new StringBuilder();

        for (int i = 0; i < file_lines.size(); i++)
            plaintext.append(file_lines.get(i));

        return plaintext.toString();
    }

    // Gets all lines of a file as an ArrayList, given a file name
    public static ArrayList<String> get_file_lines(final String file_name) {
        Scanner scanner = null;
        final ArrayList<String> file_lines = new ArrayList<String>();
        try {
            scanner = new Scanner(new FileInputStream(file_name));
            while (scanner.hasNextLine())
                file_lines.add(scanner.nextLine() + " ");
        } catch (final FileNotFoundException e) {
            System.out.println("File not found:" + file_name);
        }
        return file_lines;
    }
    
    // Gets all content words from a sentence
	public static HashSet<String> get_content_words(String sentence)
	{
		String[] question_words = sentence.trim().split(" "); 
		ArrayList<String> split_question = new ArrayList<String>();
		for(int i = 0; i < question_words.length; i++)
		{
			split_question.addAll(split_punctuation_from_word(question_words[i]));
		}
		question_words = new String[split_question.size()];
		for(int i = 0; i < split_question.size(); i++)
		{
			question_words[i] = split_question.get(i);
		}
		
		ArrayList<String> question_pos_seq = new ArrayList<String>(Arrays.asList(Answerer.tagger.tag(question_words)));
		HashSet<String> content_words = new HashSet<String>();
		for(int i = 0; i < question_pos_seq.size(); i++)
		{
			if((!closed_class_tags.contains(question_pos_seq.get(i))) && (!word_blacklist.contains(question_words[i])))
			{
				content_words.add(question_words[i].toLowerCase());
			}
		}
		
		return content_words;
	}
	
	// Splits a String representing a word into an ArrayList of Strings, where any leading or
	// trailing punctuation has been split into separate Strings
	public static ArrayList<String> split_punctuation_from_word(String word)
    {
    	ArrayList<String> word_list = new ArrayList<String>();
    	
		String end_punctuation_string;
		String start_punctuation_string;
		boolean ends_with_punctuation;
		boolean starts_with_punctuation;
		
		// Gets all punctuation at the end of a word
		end_punctuation_string = "";
		ends_with_punctuation = false;
		for(int j = word.length() - 1; j >= 0; j--)
		{
			if(punctuation.contains(word.charAt(j)))
			{
				end_punctuation_string = word.substring(j);
			}
			else
			{
				j = -1;
			}
		}
		
		// Gets all punctuation at the start of a word
		start_punctuation_string = "";
		starts_with_punctuation = false;
		for(int j = 0; j < word.length(); j++)
		{
			if(punctuation.contains(word.charAt(j)))
			{
				start_punctuation_string = word.substring(0,j);
			}
			else
			{
				j = word.length();
			}
		}
		
		// Checks if the starting and ending punctuation strings are empty
		ends_with_punctuation = (end_punctuation_string.length() < word.length()) && (end_punctuation_string.length() > 0);
		starts_with_punctuation = (start_punctuation_string.length() < word.length()) && (start_punctuation_string.length() > 0);
		
		// Removes starting and ending punctuation from the word
		if(ends_with_punctuation)
		{
			word = word.substring(0, word.length() - end_punctuation_string.length());
		}
		
		if(starts_with_punctuation)
		{
			word = word.substring(word.length() - start_punctuation_string.length());
		}
		
		// Splits starting punctuation into individual punctuation marks, except in the case
		// of two single quotes representing a double quote
		// Then adds starting punctuation to the word list
		if(starts_with_punctuation)
		{
			if(start_punctuation_string.length() == 1)
			{
				word_list.add(start_punctuation_string);
			}
			else
			{						
				for(int j = 0; j < start_punctuation_string.length(); j++)
				{
					if(((start_punctuation_string.charAt(j) == '\'') || (start_punctuation_string.charAt(j) == '`')) && ((j < start_punctuation_string.length() - 1) && (start_punctuation_string.charAt(j+1) == start_punctuation_string.charAt(j))))
					{
						word_list.add(String.format("%c%c",start_punctuation_string.charAt(j),start_punctuation_string.charAt(j+1)));
						j++;
					}
					else
					{
						word_list.add(String.valueOf(start_punctuation_string.charAt(j)));
					}
				}
			}
		}
		
		// Adds the word to the word list
		word_list.add(word);
		
		// Splits ending punctuation into individual punctuation marks, except in the cases
		// of two single quotes representing a double quote or a word ending with a
		// normal punctuation mark followed by a semicolon
		// Then adds starting punctuation to the word list
		if(ends_with_punctuation)
		{
			if(end_punctuation_string.length() == 1)
			{
				word_list.add(end_punctuation_string);
			}
			else
			{
				if(end_punctuation_string.charAt(end_punctuation_string.length() - 1) == ';')
				{
					end_punctuation_string = end_punctuation_string.substring(end_punctuation_string.length() - 1);
				}
				
				for(int j = 0; j < end_punctuation_string.length(); j++)
				{
					if(((end_punctuation_string.charAt(j) == '\'') || (end_punctuation_string.charAt(j) == '`')) && ((j < end_punctuation_string.length() - 1) && (end_punctuation_string.charAt(j+1) == end_punctuation_string.charAt(j))))
					{
						word_list.add(String.format("%c%c",end_punctuation_string.charAt(j),end_punctuation_string.charAt(j+1)));
						j++;
					}
					else
					{
						word_list.add(String.valueOf(end_punctuation_string.charAt(j)));
					}
				}
			}
		}
    	
    	return word_list;
    }
	
	// Given an ArraayList of Strings, returns a single String of all component strings, separated by spaces
	public static String lstToString (ArrayList<String> ar) {
		String out = "";
		for (String st : ar) {
			out += " " + st;
		}
		return out;
	}
}
