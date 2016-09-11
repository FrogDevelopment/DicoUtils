package fr.frogdevelopment.jmdict;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Main {

    //    public static void main(String[] args) {
//
////        Entry.fromString("御返し|おかえし|/////revenge|vs, n/////return favour (favor), return gift|/////change (in a cash transaction)");
//        Entry.fromString("|かっかと|adv,adv-to,vs//on-mim///burning hotly,burning redly");
//
//    }
    public static void main(String[] args) {
        try {
            long start = System.currentTimeMillis();

            String language = "fre";
            Parser parser = new Parser(language);
            URL dir_url = ClassLoader.getSystemResource("JMdict");
            File file = new File(dir_url.toURI());

            parser.parse(file);

            File fileOut = new File("e:/Temp/entries_" + language + ".txt");
            FileUtils.write(fileOut, String.valueOf(parser.getEntries().size()) + IOUtils.LINE_SEPARATOR, StandardCharsets.UTF_8, false);
            FileUtils.writeLines(fileOut, StandardCharsets.UTF_8.name(), parser.getEntries(), true);

            System.out.println("nb entries : " + parser.getEntries().size() + " in " + (System.currentTimeMillis() - start) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
