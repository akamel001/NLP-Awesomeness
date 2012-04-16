/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project3;

import java.io.IOException;

import Strategy.HMMStrategy;
import Strategy.ParseStrategy;

/**
 *
 * @author Aseel
 */
public class Project3 {

    public static void main(String[] args) throws IOException {
        ParseStrategy strategy = new HMMStrategy("pos_files/train.pos", "pos_files/test-obs.pos", "kaggle.csv", 2);
        //ParseStrategy strategy = new HMMStrategy("pos_files/train.d.pos", "pos_files/test.d.pos", "kaggle.d.csv", 2);
        strategy.execute();
    }
}
