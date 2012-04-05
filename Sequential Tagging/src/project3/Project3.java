/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project3;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import org.tartarus.snowball.TestApp;

/**
 *
 * @author Aseel
 */
public class Project3 {

    /**
     * @param args the command line arguments
     */
    static Scanner in;
    static TreeMap WORDS_TREE = new TreeMap();
    static TreeMap FINAL_TREE = new TreeMap();

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, Throwable {

        //read the training set
        in = new Scanner(new FileReader(System.getProperty("user.dir") + "/pos_files/train.pos"));

        //count the # of words in the file
        wordCount(in);

        //print the Word_tree map
        printWordTree();

    }

    public static void wordCount(Scanner in) throws FileNotFoundException, UnsupportedEncodingException, Throwable {

        String word, tag = "";
        int count = 0;
        TreeMap T;
        String line;

        while (in.hasNextLine()) {

            line = in.nextLine();

            String[] tokens = line.split("[ ]+");
            tag = tokens[0];
            word = tokens[1];

            //stemm the word
            TestApp t = new TestApp();
            word = t.main(word);

            if (word.matches("<s>")) {
                continue;
            }

            if (WORDS_TREE.containsKey(word)) {
                T = (TreeMap) WORDS_TREE.get(word);//get the tags map

                if (!T.containsKey(tag)) {
                    T.put(tag, 1);//
                } else {
                    int temp = Integer.parseInt(T.get(tag).toString());
                    T.put(tag, temp + 1);//
                }
            } else {
                TreeMap Tags_tree = new TreeMap();//new treemap for tags
                Tags_tree.put(tag, 1);
                WORDS_TREE.put(word, Tags_tree);
            }
        }//end while

    }

    //print the Word_tree map
    //Save the words and there tags to the FINAL_TREE
    public static void printWordTree() {
        String tag = "";
        String word;
        Iterator e = WORDS_TREE.entrySet().iterator();
        while (e.hasNext()) {
            Map.Entry<String, TreeMap> entry = (Map.Entry) e.next();

            word = entry.getKey();
            System.out.print("word = " + word);

            TreeMap temp = (TreeMap) entry.getValue();//file tree map
            Iterator ent = temp.entrySet().iterator();
            int count = 0;
            while (ent.hasNext()) {
                Map.Entry<String, Integer> entr = (Map.Entry) ent.next();
                //System.out.print("tag"+ entr.getKey());
                //System.out.println("count"+ entr.getValue());

                if (entr.getValue() >= count) {
                    tag = entr.getKey();
                    count = entr.getValue();
                }
            }
            System.out.print("  tag = " + tag);
            System.out.println("  count = " + count);
            FINAL_TREE.put(word, tag);

        }


    }

    //query the FINAL_TREE 
    //input:word
    //output:tag
    public static String query(String word) {

        String tag = "This word does not have a tag";

        if (FINAL_TREE.containsKey(word)) {
            tag = (String) FINAL_TREE.get(word);
        }

        return tag;

    }
}
