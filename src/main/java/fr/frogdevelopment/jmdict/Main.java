package fr.frogdevelopment.jmdict;

import java.io.File;
import java.net.URL;

public class Main {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        Parser parser = new Parser("fre");

        try {
            URL dir_url = ClassLoader.getSystemResource("JMdict");
            File file = new File(dir_url.toURI());

            parser.parse(file);

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("nb entries : " + parser.ENTRIES.size() + " in " + (System.currentTimeMillis() - start) + "ms");
    }

}
