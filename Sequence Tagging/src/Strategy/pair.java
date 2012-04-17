package Strategy;

public class pair {
	public pair priorPair;
    public double prob;
    public String tag;
    public String word;
    
    public pair(String word, String tag, pair prevPair, double prob){
        this.word = word;
        this.tag = tag;
        this.prob = prob;
        priorPair = prevPair;
    }
    public pair(String word, String tag) {
        this.word = word;
        this.tag = tag;
        priorPair = null;
        prob = 0.0;
    }
}

