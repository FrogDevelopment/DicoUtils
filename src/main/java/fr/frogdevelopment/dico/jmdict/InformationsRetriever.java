package fr.frogdevelopment.dico.jmdict;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InformationsRetriever {

    private static final String URL = "ftp://ftp.monash.edu.au/pub/nihongo/JMdict.gz";

    private static final Pattern GLOSS_PATTERN = Pattern.compile("^<gloss( xml:lang=\"(?<lang>\\w{3})\")?>(?<value>.*)(.*)</gloss>$");

    private static final Map<String, Integer> countByLang = new HashMap<>();

    public static void main(String[] args) throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(new URL(URL).openStream());
             GzipCompressorInputStream gzIn = new GzipCompressorInputStream(in);
             BufferedReader br = new BufferedReader(new InputStreamReader(gzIn))) {

            long start = System.currentTimeMillis();

            String line;
            Matcher matcher;
            int nbEntries = 0;
            while ((line = br.readLine()) != null) {

                if (line.equals("</entry>")) {
                    nbEntries++;
                }

                matcher = GLOSS_PATTERN.matcher(line);
                if (matcher.matches()) {
                    String lang = matcher.group("lang");
                    if (StringUtils.isEmpty(lang)) {
                        lang = "eng";
                    }

                    countByLang.merge(lang, 1, Integer::sum);
                }
            }

            countByLang.entrySet()
                    .stream()
                    .sorted(Comparator.comparing(Map.Entry::getValue))
                    .forEach(e -> System.out.println("lang : " + e.getKey() + " = " + e.getValue()));

            System.out.println(System.lineSeparator() + "=> " + nbEntries + " entries merge in " + (System.currentTimeMillis() - start) + "ms");
        }
    }
}
