package Strategy;

public class Pair {
	public Pair priorPair;
    public double prob;
    public String tag;
    public String word;
    
    public Pair(String word, String tag, Pair prevPair, double prob){
        this.word = word;
        this.tag = tag;
        this.prob = prob;
        priorPair = prevPair;
    }
    public Pair(String word, String tag) {
        this.word = word;
        this.tag = tag;
        priorPair = null;
        prob = 0.0;
    }
}

