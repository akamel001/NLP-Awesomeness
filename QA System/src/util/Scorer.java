package util;

import java.util.ArrayList;
import java.util.Set;

public class Scorer {
    /**
     * This function takes an array list of answers. There should be exactly 5 answers provided.
     * Ensure that the answer objects have the question number being answer properly set along with
     * with the question being answered. The function uses those attributes to look up a given
     * question.
     * 
     * @param guesses
     *            which are Answer objects that have been properly instantiated.
     * @return
     */
    public static double score(final Answer answer,
            final ArrayList<Guess> guesses) {
        if (guesses.size() != 5) {
            System.out.println("Invalid number of guesses!");
            return -1;
        }

        double subMRR = 0.0;
        for (final String ans : answer.getAnsList())
            for (int i = 1; i <= guesses.size(); i++)
                if (sets_share_word(Parser.get_content_words(ans),
                        Parser.get_content_words(guesses.get(i - 1).guess)))
                    subMRR += 1.0 / (i);
        return subMRR / guesses.size();
    }

    /**
     * Returns true if at least one element of the string sets are equal
     * 
     * @return
     */
    public static boolean sets_share_word(final Set<String> set1,
            final Set<String> set2) {
        for (final String s1 : set1)
            for (final String s2 : set2)
                if (s1.toLowerCase().equals(s2.toLowerCase()))
                    return true;
        return false;
    }
}
