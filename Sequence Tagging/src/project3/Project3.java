/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project3;

import java.io.IOException;

import Strategy.BaselineStrategy;
import Strategy.ParseStrategy;

/**
 *
 * @author Aseel
 */
public class Project3 {

    public static void main(String[] args) throws IOException {
        ParseStrategy strategy = new BaselineStrategy("pos_files/train.pos", "pos_files/test-obs.pos", "kaggle.csv");
        strategy.execute();
    }
}
