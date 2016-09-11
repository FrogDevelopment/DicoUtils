package fr.frogdevelopment.dico.sentences;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static final Set<String> IDS = new HashSet<>();

    public static void main(String[] args) {
        String from = "fra";
        String to = "jpn";

        try {
            long start = System.currentTimeMillis();
            getSentences(from, to);
            System.out.println(" in " + (System.currentTimeMillis() - start) + "ms");
            getLinks(from, to);
            System.out.println(" in " + (System.currentTimeMillis() - start) + "ms");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getSentences(String from, String to) throws IOException, URISyntaxException {
        URL sentences = ClassLoader.getSystemResource("sentences.csv");
        File file = new File(sentences.toURI());

        File fileOut = new File("e:/Temp/sentences_" + from + "_" + to + ".csv");

        String regex = "(?<id>\\d+)\t(?<language>(%s|%s))\t(?<text>.*)";
        Pattern pattern = Pattern.compile(String.format(regex, to, from));

        LineIterator it = FileUtils.lineIterator(file, StandardCharsets.UTF_8.name());
        final Set<String> lines = new HashSet<>();
        try {
            while (it.hasNext()) {
                String line = it.nextLine();

                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    lines.add(line);
                    IDS.add(matcher.group("id"));
                }
            }
        } finally {
            LineIterator.closeQuietly(it);
        }

        FileUtils.writeLines(fileOut, StandardCharsets.UTF_8.name(), lines);

        System.out.print("fini 1");
    }

    private static void getLinks(String from, String to) throws IOException, URISyntaxException {
        URL sentences = ClassLoader.getSystemResource("links.csv");
        File file = new File(sentences.toURI());

        File fileOut = new File("e:/Temp/links_" + from + "_" + to + ".csv");

        LineIterator it = FileUtils.lineIterator(file);
        final Set<String> lines = new HashSet<>();
        try {
            while (it.hasNext()) {
                String line = it.nextLine();

                String[] ids = line.split("\t");

                if(IDS.contains(ids[0]) && IDS.contains(ids[1])) {
                    lines.add(line);
                }
            }
        } finally {
            LineIterator.closeQuietly(it);
        }

        FileUtils.writeLines(fileOut, lines);
        System.out.print("fini 2");
    }
}
