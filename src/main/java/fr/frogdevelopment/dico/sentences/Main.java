package fr.frogdevelopment.dico.sentences;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        String to = "fra";

        try {
            new Main(to).execute();

            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }


    private static final Pattern PATTERN_INDICES = Pattern.compile("(?<jap>\\d+)\\t(?<eng>\\d+)\\t(?<text>.*)");

    private Set<String> lines = new HashSet<>();


    private final Map<String, String> mapIndices = new HashMap<>();
    private final Map<String, String> mapJapanese = new HashMap<>();
    private final Map<String, String> mapTranslation = new HashMap<>();

    private final String jpn = "jpn";
    private final String translation;

    private Main(String translation) {
        this.translation = translation.toLowerCase();
    }

    private void execute() throws IOException, URISyntaxException {
        long start;

        // links between JMDict & Tatoeba sentences
        start = System.currentTimeMillis();
        step1();
        System.out.println(" in " + (System.currentTimeMillis() - start) + "ms");

        // tatoeba sentences
        start = System.currentTimeMillis();
        step2();
        System.out.println(" in " + (System.currentTimeMillis() - start) + "ms");

        // links between tatoeba sentences
        start = System.currentTimeMillis();
        step3();
        System.out.println(" in " + (System.currentTimeMillis() - start) + "ms");

        // write file
        write();
    }

    private void step1() throws IOException, URISyntaxException {
        read("http://downloads.tatoeba.org/exports/jpn_indices.tar.bz2", line -> {
            Matcher matcher = PATTERN_INDICES.matcher(line);
            if (matcher.matches()) {
                String text = matcher.group("text");
                if (StringUtils.isBlank(text)) {
                    // exits empty indices ??
                    return;
                }
                mapIndices.put(matcher.group("jap"), text);
            }
        });

        // cf http://www.edrdg.org/wiki/index.php/Sentence-Dictionary_Linking
        // order :JMDict_word(reading)[sense_number]{form_in_sentence}~

        System.out.print("step 1 : " + mapIndices.size() + " indices");
    }

    private void step2() throws IOException, URISyntaxException {
        String regex = "(?<ref>\\d+)\t(?<language>%s)\t(?<text>.*)";
        Pattern patternJapanese = Pattern.compile(String.format(regex, jpn));
        Pattern patternTranslation = Pattern.compile(String.format(regex, translation));

        read("http://downloads.tatoeba.org/exports/sentences.tar.bz2", line -> {
            // is japanese sentence ?
            Matcher matcherJapanese = patternJapanese.matcher(line);
            if (matcherJapanese.matches()) {
                String ref = matcherJapanese.group("ref");
                // take sentence only if presents in jpn_indices.csv
                if (mapIndices.containsKey(ref)) {
                    mapJapanese.put(ref, matcherJapanese.group("text"));
                }
            }

            // is wanted language sentence ?
            Matcher matcherTranslation = patternTranslation.matcher(line);
            if (matcherTranslation.matches()) {
                mapTranslation.put(matcherTranslation.group("ref"), matcherTranslation.group("text"));
            }
        });

        System.out.print("step 2 : " + mapJapanese.size() + " japanese sentences and " + mapTranslation.size() + " translated sentences");
    }

    private void step3() throws IOException, URISyntaxException {
        read("http://downloads.tatoeba.org/exports/links.tar.bz2", line -> {
            String[] ids = line.split("\t");

            String japaneseSentence;
            String translationSentence;
            String indices;
            String id_left = ids[0];
            String id_right = ids[1];
            String japaneseId;
            String translationId;
            if (mapIndices.containsKey(id_left) && mapJapanese.containsKey(id_left) && mapTranslation.containsKey(id_right)) {
                japaneseId = id_left;
                translationId = id_right;
            } else if (mapIndices.containsKey(id_right) && mapJapanese.containsKey(id_right) && mapTranslation.containsKey(id_left)) {
                japaneseId = id_right;
                translationId = id_left;
            } else {
                return;
            }

            japaneseSentence = mapJapanese.get(japaneseId);
            indices = mapIndices.get(japaneseId);
            translationSentence = mapTranslation.get(translationId);

            // line format : Jpn_seq_no[TAB]Eng_seq_no[TAB]Japanese sentence[TAB]English sentence[TAB]Indices
            lines.add(japaneseId + "\t" + translationId + "\t" + japaneseSentence + "\t" + translationSentence + "\t" + indices);
        });

        System.out.print("step 3 : " + lines.size() + " lines");
    }

    private void write() throws IOException {
        String pathname = "d:/Temp/examples_" + jpn + "_" + translation + ".csv";
        File fileOut = new File(pathname);
        FileUtils.writeLines(fileOut, lines);
        System.out.println("File saved : " + pathname);
    }

    // read directly from tatoeba.org => always last version
    private static void read(String url, Consumer<String> consumer) throws IOException, URISyntaxException {
        System.out.println("reading " + url);
        // cf http://stackoverflow.com/a/28029231/244911
        try (
                // 1st InputStream from your compressed file
                BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
                // wrap in a 2nd InputStream that deals with compression
                BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
                // wrap in a 3rd InputStream that deals with tar
                TarArchiveInputStream tarIn = new TarArchiveInputStream(bzIn)) {

            ArchiveEntry entry;
            BufferedReader br;
            while (null != (entry = tarIn.getNextEntry())) {
                if (entry.getSize() < 1) {
                    continue;
                }

                // cf http://stackoverflow.com/a/25749756/244911
                br = new BufferedReader(new InputStreamReader(tarIn)); // Read directly from tarInput
                System.out.println("\tFile name : " + entry.getName());
                String line;
                while ((line = br.readLine()) != null) {
                    consumer.accept(line);
                }
            }
        }
    }
}
