package train;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import main.Answerer;
import util.GuessModel;
import util.QuestionType;
import util.QuestionType.Type;

public class Trainer extends TrainModel {

    public Trainer() {}

    /**
     * This function parses the training file and counts a the occurrence of a specific POS
     * sequence. POS tagging is done by using OpenNLP.
     * 
     */
    @Override
    public HashMap<Type, GuessModel> train_models(
            final HashMap<String, String> questionAnswers) {
        // Sanity check
        if ((questionAnswers == null) || questionAnswers.isEmpty()) {
            System.out
                    .println("(function: train_model) Input is null or empty!");
            return null;
        }

        final HashMap<Type, GuessModel> retMap = new HashMap<Type, GuessModel>();

        for (final Entry<String, String> entry : questionAnswers.entrySet()) {
            final String question = entry.getKey();
            final String answer = entry.getValue();
            final Type type = QuestionType.classify(question);
            final PosSequence answerPOSSeq = new PosSequence(
                    new ArrayList<String>(Arrays.asList(Answerer.tagger
                            .tag(answer.split(" ")))));

            // create new guess model and insert it into the map
            if (!retMap.containsKey(type)) {
                final GuessModel guess = new GuessModel();
                guess.pos_sequences.put(answerPOSSeq, 1);
                retMap.put(type, guess);
            } else {
                // Model already exists
                final GuessModel subGuess = retMap.get(type);
                // Does this pos sequence exist?
                if (!subGuess.pos_sequences.containsKey(answerPOSSeq))
                    subGuess.pos_sequences.put(answerPOSSeq, 1);
                else {
                    final int count = subGuess.pos_sequences.get(answerPOSSeq);
                    subGuess.pos_sequences.put(answerPOSSeq, count + 1);
                }

                retMap.put(type, subGuess);
            }
        }

        return retMap;
    }
}
