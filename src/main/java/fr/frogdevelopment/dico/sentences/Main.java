package fr.frogdevelopment.dico.sentences;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        String from = "jpn";
        String to = "fra";

        try {
            long start = System.currentTimeMillis();

            Main main = new Main(from, to);
            main.getSentences();
            System.out.println(" in " + (System.currentTimeMillis() - start) + "ms");
            main.getLinks();
            System.out.println(" in " + (System.currentTimeMillis() - start) + "ms");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Map<String, String> mapFrom = new HashMap<>();
    private final Map<String, String> mapTo = new HashMap<>();

    private final String from;
    private final String to;

    public Main(String from, String to) {
        this.from = from.toLowerCase();
        this.to = to.toLowerCase();
    }

    private void getSentences() throws IOException, URISyntaxException {
        URL sentences = ClassLoader.getSystemResource("sentences.csv");
        File file = new File(sentences.toURI());

        String regex = "(?<ref>\\d+)\t(?<language>(%s))\t(?<text>.*)";
        Pattern patternFrom = Pattern.compile(String.format(regex, from));
        Pattern patternTo = Pattern.compile(String.format(regex, to));

        LineIterator it = FileUtils.lineIterator(file, StandardCharsets.UTF_8.name());
        try {
            Matcher matcherFrom;
            Matcher matcherTo;
            while (it.hasNext()) {
                String line = it.nextLine();

                matcherFrom = patternFrom.matcher(line);
                if (matcherFrom.matches()) {
                    mapFrom.put(matcherFrom.group("ref"), matcherFrom.group("text"));
                }

                matcherTo = patternTo.matcher(line);
                if (matcherTo.matches()) {
                    mapTo.put(matcherTo.group("ref"),  matcherTo.group("text"));
                }
            }
        } finally {
            LineIterator.closeQuietly(it);
        }

        System.out.print("fini 1");
    }

    private void getLinks() throws IOException, URISyntaxException {
        URL sentences = ClassLoader.getSystemResource("links.csv");
        File file = new File(sentences.toURI());

        File fileOut = new File("e:/Temp/examples_" + from + "_" + to + ".csv");

        LineIterator it = FileUtils.lineIterator(file);
        final Set<String> lines = new HashSet<>();
        try {
            while (it.hasNext()) {
                String line = it.nextLine();

                String[] ids = line.split("\t");

                String from;
                String to;
                if (mapFrom.containsKey(ids[0]) && mapTo.containsKey(ids[1])) {
                    from = mapFrom.get(ids[0]);
                    to = mapTo.get(ids[1]);
                } else if (mapFrom.containsKey(ids[1]) && mapTo.containsKey(ids[0])) {
                    from = mapFrom.get(ids[1]);
                    to = mapTo.get(ids[0]);
                } else {
                    continue;
                }

                // from|to
                lines.add(from + "|" + to);
            }
        } finally {
            LineIterator.closeQuietly(it);
        }

        FileUtils.writeLines(fileOut, lines);
        System.out.print("fini 2");
    }
}
