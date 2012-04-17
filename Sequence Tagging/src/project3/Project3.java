package project3;

import java.io.IOException;

import Strategy.HMMStrategy;;

public class Project3 {

    public static void main(String[] args) throws IOException {
    	HMMStrategy strategy = new HMMStrategy("pos_files/train.pos", "pos_files/test-obs.pos", "kaggle.csv");
        strategy.execute();
    }
}
