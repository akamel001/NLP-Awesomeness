package util;

// Stores the potential types of questions
public class QuestionType
{
	// Types of questions
    public enum Type
    {
        WHERE,
        WHEN,
        HOW,
        WHO_IS,
        WHO_OTHER,
        OTHER
    }

    // Classifies a question as a certain question type
    public static Type classify(final String question)
    {
        final String ques = question.toLowerCase();
        if (ques.startsWith("who"))
        {
            if (ques.contains(" is ") || ques.contains(" are ") || ques.contains(" was ") || ques.contains(" were "))
            {
                return Type.WHO_IS;
            }
            else
            {
            	return Type.WHO_OTHER;
            }
        }
        else if (ques.startsWith("where"))
        {
        	return Type.WHERE;
        }
        else if (ques.startsWith("when"))
        {
        	return Type.WHEN;
        }
        else if (ques.startsWith("how"))
        {
            return Type.HOW;
        }
        else
        {
        	return Type.OTHER;
        }
    }
}
