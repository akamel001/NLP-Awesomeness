package Strategy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

import util.Util;

public class HMMStrategy {
	
	File fiel = null;
	String trainingFile = null;
	String testFile = null;
	String outputFile = null;
	HashMap<String, Integer> tagFreq = new HashMap<String, Integer>();
    HashMap<String, HashMap<String, Integer>> wordFreq = new HashMap<String, HashMap<String,Integer>>();
    HashMap<String, HashMap<String, Integer>> bigramFreq= new HashMap<String, HashMap<String,Integer>>();
    HashMap<String, HashMap<String, Integer>> tagWordFreq= new HashMap<String, HashMap<String, Integer>>();
    
    Writer out = null;
    
    int numBigrams = 0;
    
	public HMMStrategy(String trainFile, String testFile, String output) throws FileNotFoundException{
		trainingFile = trainFile;
		this.testFile = testFile;
		outputFile = output;
		out = new OutputStreamWriter(new FileOutputStream(outputFile));
	}
	
    public void execute() throws IOException{
        parseFile();
        System.out.println("Done parsing...");
        viterbi(getWords(testFile));
    }
    
    public ArrayList<String> getWords(String file) throws FileNotFoundException{
        ArrayList<String> list = new ArrayList<String>();
        File testFile = new File(file);
        Scanner scanner = new Scanner(testFile);;
        
		while (scanner .hasNext()){
            list.add(scanner.next());
        }
        return list;
    }
    
    private void parseFile() throws FileNotFoundException {
        File file = new File(trainingFile);
        Scanner scanner = new Scanner(file);
        String line = scanner.nextLine();
        String[] tokens = line.split(" ");
        
        String prevTag = tokens[0];
        
        while(scanner.hasNext()){
        	
        	//TODO split these into smaller function to make it more readable...
        	
            String tag = scanner.next();
            String word = scanner.next();
            
            //update tag frequency 
            if(tagFreq.containsKey(tag))
            	tagFreq.put(tag, tagFreq.get(tag)+1);
             else
            	tagFreq.put(tag, 1);
            
            //update word frequency 
            if(wordFreq.containsKey(tag)){
            	HashMap<String, Integer> newMap = new HashMap<String, Integer>();
            	newMap = wordFreq.get(tag);
            	
                if(newMap.containsKey(word))
                	newMap.put(word, newMap.get(word)+1);
                 else
                	newMap.put(word, 1);
               
               
            } else {
                HashMap<String, Integer> newMap = new HashMap<String, Integer>();
                newMap.put(word, 1);
                wordFreq.put(tag, newMap);
            }
            
            //update bigram frequency 
            if(bigramFreq.containsKey(prevTag)){
            	HashMap<String, Integer> newMap = new HashMap<String, Integer>();
            	newMap = bigramFreq.get(prevTag);
            	
                if(newMap.containsKey(tag))
                	newMap.put(tag, newMap.get(tag)+1);
                 else
                	newMap.put(tag, 1);
               
            } else {
                HashMap<String, Integer> newMap = new HashMap<String, Integer>();
                newMap.put(tag, 1);
                bigramFreq.put(prevTag, newMap);
            }
            
            //update word -> tag frequency 
            if(tagWordFreq.containsKey(word)){
            	HashMap<String, Integer> newMap = new HashMap<String, Integer>();
            	newMap = tagWordFreq.get(word);
            	
                if(newMap.containsKey(tag))
                	newMap.put(tag, newMap.get(tag)+1);
                 else
                	newMap.put(tag, 1);
               
            } else {
                HashMap<String, Integer> newMap = new HashMap<String, Integer>();
                newMap.put(tag, 1);
                tagWordFreq.put(word, newMap);
            }
            
            numBigrams++;
            prevTag = tag;
        }
    }
    
    public void viterbi(ArrayList<String> words) throws IOException{
        //two-dimensional Viterbi Matrix
        boolean startOfSent = true;
        HashMap<String, pair> prevMap = null;
        for(int i=0; i<words.size(); i++){
            String word = words.get(i);
            HashMap<String, pair> newMap = new HashMap<String,pair>();
            
            if(startOfSent){
                pair n = new pair(word, "<s>", null, 1.0);
                newMap.put(word, n);
                startOfSent = false;
            } else {
                //add all tags 
                if(tagWordFreq.containsKey(word)){
                    // Only Training Set tags
                    HashMap<String, Integer> tagcounts = tagWordFreq.get(word);
                    for(String tag : tagcounts.keySet()){
                        newMap.put(tag, computeNodeProb(word, tag, prevMap));               
                    }
                } else {
                    for (String tag : tagFreq.keySet()) 
                        newMap.put(tag, computeNodeProb(word, tag, prevMap));
                }
                
                if((i == words.size()-1) || words.get(i+1).equals("<s>")){
                	vitrbiBptr(newMap);
                    startOfSent = true;
                }
            }
            prevMap = newMap;
        }
        out.close();
    }
    
    private void vitrbiBptr(HashMap<String, pair> map) throws IOException {
        pair n = new pair("", "");
        for(String key : map.keySet()){
            pair currentNode = map.get(key);
            if(currentNode.prob >= n.prob){
                n = currentNode;
            }
        }
        
        Stack<pair> stack = new Stack<pair>();
        while(n != null){
            stack.push(n);
            n = n.priorPair;
        }

        while(!stack.isEmpty()){
            n = stack.pop();
            out.write(n.tag + " " + n.word + "\n");
        }
    }
    
    //using add one prob... 
    public double probTagGivenTag(String tag1, String tag2){
    	int vocabSize = tagFreq.keySet().size();
        return (double) (Util.counts(bigramFreq,tag1,tag2)+1) / (double) (Util.counts(tagFreq,tag1)+vocabSize);
    }
    
    private pair computeNodeProb(String word, String tag, HashMap<String, pair> prevMap){
        pair n = new pair(word,tag);
        double maxProb = 0.0;
        for(String prevTag : prevMap.keySet()){
            pair prevNode = prevMap.get(prevTag);
            double prevProb = prevNode.prob;
            prevProb = prevProb * probTagGivenTag(prevTag, tag);
            if(prevProb >= maxProb){
                maxProb = prevProb;
                n.priorPair = prevNode;
            }
        }
        
        int tagSize = tagWordFreq.keySet().size();
        double prob =  (double) (Util.counts(wordFreq,tag,word)+1) / (double) (Util.counts(tagFreq,tag)+tagSize);
        
        n.prob = maxProb * prob;
        return n;
    }
}