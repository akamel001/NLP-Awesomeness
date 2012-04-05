package org.tartarus.snowball;

public class TestApp {

    public static String main(String word) throws Throwable {


        Class stemClass = Class.forName("org.tartarus.snowball.ext.englishStemmer");
        SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();

        stemmer.setCurrent(word);
        stemmer.stem();
        String str=stemmer.getCurrent();
        //System.out.println(dtr);
        return str;

    }
}
