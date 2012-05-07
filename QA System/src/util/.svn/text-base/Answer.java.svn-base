package util;

import java.util.HashMap;
import java.util.LinkedList;

// Represents a question and potential answers, parsed from an answer file
public class Answer
{
    String question; // Question string
    String number_string; // String representing the question number

    HashMap<String, LinkedList<String>> docs_to_ans = new HashMap<String, LinkedList<String>>();

    public Answer(final String num, final String q, final String doc,
            final String ans)
    {
        number_string = num;
        question = q;
        LinkedList<String> ansList = new LinkedList<String>();
        ansList.add(ans);
        docs_to_ans.put(doc, ansList);
    }

    // Question getter and setter
    public String getQuestion()
    {
        return question;
    }
    public void setQuestion(final String question)
    {
        this.question = question;
    }

    // Getter and setter for HashMap of documents to answers
    public HashMap<String, LinkedList<String>> getDocs_to_ans()
    {
        return docs_to_ans;
    }
    public void setDocs_to_ans(final HashMap<String, LinkedList<String>> docs_to_ans)
    {
        this.docs_to_ans = docs_to_ans;
    }
    
    // Returns a list of all answers to a given question
    public LinkedList<String> getAnsList()
    {
    	LinkedList<String> all_answers = new LinkedList<String>();
    	
    	for(LinkedList<String> ansList : docs_to_ans.values())
    	{
    		all_answers.addAll(ansList);
    	}
    	return all_answers;
    }
}