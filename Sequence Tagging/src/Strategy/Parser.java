package Strategy;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Represents actions taken on a line in a file
 * @author jonathan
 *
 */
public abstract class Parser {
    private String trainPath;
    private String testPath;
    private String kaggleFile;
    private ArrayList<String> kaggleOutput;

    public Parser(String trainPath, String testPath, String kaggleFile) {
        this.trainPath = trainPath;
        this.testPath = testPath;
        this.kaggleFile = kaggleFile;
    }

    protected abstract void train(String line);
    protected abstract void postProcess();
    protected abstract String test(String line);

    protected void parseFile(String path, boolean train) throws FileNotFoundException {
        Scanner in = new Scanner(new FileReader(path));
        String line;

        kaggleOutput = new ArrayList<String>();
        while (in.hasNextLine()) {
            line = in.nextLine();
            //System.out.println(line);
            if(train)
                train(line);
            else
                kaggleOutput.add(test(line));
        }
    }

    public void execute() throws IOException {
        parseFile(trainPath, true);
        System.out.println("Done with parsing file");
        postProcess();
        parseFile(testPath, false);
        printKaggle();
    }

    final protected void printKaggle() throws IOException {
        Writer out = new OutputStreamWriter(new FileOutputStream(kaggleFile));
        for(String line : kaggleOutput)
            out.write(line +"\n");
        out.close();
    }
}