package train;

import java.util.ArrayList;

/**
 * Stores a POS sequence. 
 * In order to a POS sequence in a HashMap,
 * we need to override hashCode (and consequently equals)
 * @author jonathanpullano
 *
 */
public class PosSequence {
    public ArrayList<String> sequence = new ArrayList<String>();
    
    /**
     * Constructor (For convenience)
     */
    public PosSequence(ArrayList<String> sequence) {
        this.sequence = sequence;
    }
    
    @Override
    public int hashCode() {
        if(sequence.isEmpty())
            return 0;
        else {
            int code = 0;
            for(String tag : sequence)
                code += tag.hashCode();
            return code;
        }
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PosSequence)) return false;
        PosSequence otherSequence = (PosSequence)other;
        if(otherSequence.sequence.size() != this.sequence.size()) return false;
        for(int i = 0; i < sequence.size(); i++) {
            if(!sequence.get(i).equals(otherSequence.sequence.get(i)))
                return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return sequence.toString();
    }
}
