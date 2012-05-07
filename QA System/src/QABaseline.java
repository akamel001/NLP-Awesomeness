import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class QABaseline {
    private static HashMap<String, Integer> questionsMap = new LinkedHashMap<String, Integer>();
    private final static String DOC_PREFIX = "docs/top_docs.";
    private final static String OUTPUT_FILENAME = "kaggle.csv";
    
    public static void main(String args[])
    {
        getQuestions();
        getAnswers();
    }
    
   public static String getFile(String filename) {
       Scanner scanner = null;
       StringBuilder plaintext = new StringBuilder();
       try {
           scanner = new Scanner(new FileInputStream(filename));
           while (scanner.hasNextLine())
               plaintext.append(scanner.nextLine());
       } catch (FileNotFoundException e) {
           System.out.println("File not found:" + filename);
       }
       return plaintext.toString();
   }
   
   public static void getQuestions() {
       String fileText = getFile("Questions.txt");
       Document qDoc = Jsoup.parse(fileText);
       Elements tags = qDoc.getElementsByTag("top");
       for(Element e : tags) {
           Element topContent = e.getElementsByTag("num").first();
           String desc = e.getElementsByTag("desc").first().text().split(":")[1];
           //XML is malformed, so getting the num will contain the desc too
           //We remove it to just get the number text
           topContent.childNodes().get(1).remove();
           String num = e.getElementsByTag("num").first().text().split(" ")[1];
           questionsMap.put(desc, Integer.parseInt(num));
       }
   }
   
   public static void getAnswers() {
       BufferedWriter out = null;
       try {
           FileWriter fstream = new FileWriter(OUTPUT_FILENAME);
           out = new BufferedWriter(fstream);//267,228
           for(Map.Entry<String, Integer> doc : questionsMap.entrySet()) {
               System.out.println(doc.getValue() + ". " + doc.getKey() + ":"); 
               String fileText = getFile(DOC_PREFIX + doc.getValue());
               Document aDoc = Jsoup.parse(fileText);
               String[] headTags = new String[] {"HEADLINE", "H3", "HL", "FIRST", "HEAD"};
               Elements headlines = null;
               int tagNum = 0;
               while(headlines == null || headlines.isEmpty() && tagNum < headTags.length) {
                   headlines = aDoc.getElementsByTag(headTags[tagNum++]);
               }
               //System.out.println(headlines);
               if(!headlines.isEmpty()) {
                   int i;
                   for(i = 0; i < Math.min(5, headlines.size()); i++) {
                       doublePrint(out, doc.getValue() + " " + "top_docs." + doc.getValue() + " "+ headlines.get(i).text()+"\n");
                   }
                   for(; i < 5; i++)
                       doublePrint(out, doc.getValue() + " " + "top_docs." + doc.getValue() + " nil\n");
               } else {
                   System.out.println("Orville and Wilbur Wright");
                   doublePrint(out, doc.getValue() + " top_docs." + doc.getValue() + " Orville and Wilbur Wright\n");
                   for(int i = 0; i < 4; i++) {
                       doublePrint(out, doc.getValue() + " top_docs." + doc.getValue() + " nil\n");
                   }
               }
               System.out.println("");
           }
           out.close();
       } catch (Exception e) {
           System.err.println("Error writing "+ OUTPUT_FILENAME + ": " + e.getMessage());
       }
       
   }
   
   public static void doublePrint(BufferedWriter out, String line) {
       System.out.print(line);
       try {
           out.write(line);
       } catch (IOException e) {
           System.err.print("Failed to write line");
           e.printStackTrace();
       }
   }
}
