package util;

import java.util.HashMap;

import train.PosSequence;

// Represents all POS sequences for a given question type in the training set
// and the frequency of each POS sequence
public class GuessModel
{
    public HashMap<PosSequence, Integer> pos_sequences;
    
    public GuessModel()
    {
    	pos_sequences = new HashMap<PosSequence, Integer>();
    }
}