package fr.frogdevelopment.jmdict;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.URL;

public class Main {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        String language = "fre";
        Parser parser = new Parser(language);

        try {
            URL dir_url = ClassLoader.getSystemResource("JMdict");
            File file = new File(dir_url.toURI());

            parser.parse(file);

            ObjectMapper mapper = new ObjectMapper();

//            mapper.writeValue(new File("d:/Temp/lexicon.json"), parser.getLexicon());
            mapper.writeValue(new File("d:/Temp/entries_"+language+".json"), parser.getEntries());

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("nb entries : " + parser.getEntries().size() + " in " + (System.currentTimeMillis() - start) + "ms");
    }

}
