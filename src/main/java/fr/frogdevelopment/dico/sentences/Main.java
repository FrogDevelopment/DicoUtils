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


        try {
            new Main().execute();

            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }


    private static final String[] LANGS = {"dut", "eng", "fra", "ger", "hun", "ita", "rus", "spa", "swe", "slv"};
    private static final Pattern PATTERN_INDICES = Pattern.compile("(?<jap>\\d+)\\t(?<eng>\\d+)\\t(?<text>.*)");
    private static final String PATTERN_TATOEBA = "(?<ref>\\d+)\t(?<language>%s)\t(?<text>.*)";

    private static final Pattern PATTERN_JAPANESE = Pattern.compile(String.format(PATTERN_TATOEBA, "jpn"));
    private final Map<String, Pattern> patterns = new HashMap<>();

    private Map<String, Set<String>> linesByLang = new HashMap<>();

    private final Map<String, String> mapIndices = new HashMap<>();
    private final Map<String, String> mapJapanese = new HashMap<>();
    private final Map<String, Map<String, String>> translationByLang = new HashMap<>();


    private Main() {
        for (String lang : LANGS) {
            patterns.put(lang, Pattern.compile(String.format(PATTERN_TATOEBA, lang)));
            linesByLang.put(lang, new HashSet<>());
            translationByLang.put(lang, new HashMap<>());
        }
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

        read("http://downloads.tatoeba.org/exports/sentences.tar.bz2", line -> {
            // is japanese sentence ?
            Matcher matcher = PATTERN_JAPANESE.matcher(line);
            if (matcher.matches()) {
                String ref = matcher.group("ref");
                // take sentence only if presents in jpn_indices.csv
                if (mapIndices.containsKey(ref)) {
                    mapJapanese.put(ref, matcher.group("text"));
                }
            } else {
                for (Map.Entry<String, Pattern> entry : patterns.entrySet()) {
                    matcher = entry.getValue().matcher(line);
                    if (matcher.matches()) {
                        translationByLang.get(entry.getKey()).put(matcher.group("ref"), matcher.group("text"));
                    }
                }
            }
        });

        System.out.println("step 2 : translated sentences by lang");
        System.out.println(" \tjpn : " + mapJapanese.size());
        translationByLang.forEach((k, v) -> System.out.println("\t" + k + " : " + v.size()));
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

            for (Map.Entry<String, Map<String, String>> entry : translationByLang.entrySet()) {
                String lang = entry.getKey();
                Map<String, String> mapLang = entry.getValue();

                if (mapIndices.containsKey(id_left) && mapJapanese.containsKey(id_left) && mapLang.containsKey(id_right)) {
                    japaneseId = id_left;
                    translationId = id_right;
                } else if (mapIndices.containsKey(id_right) && mapJapanese.containsKey(id_right) && mapLang.containsKey(id_left)) {
                    japaneseId = id_right;
                    translationId = id_left;
                } else {
                    continue;
                }

                japaneseSentence = mapJapanese.get(japaneseId);
                indices = mapIndices.get(japaneseId);
                translationSentence = mapLang.get(translationId);

                // line format : Jpn_seq_no[TAB]Eng_seq_no[TAB]Japanese sentence[TAB]English sentence[TAB]Indices
                linesByLang.get(lang).add(japaneseId + "\t" + translationId + "\t" + japaneseSentence + "\t" + translationSentence + "\t" + indices);
            }
        });

        System.out.println("step 3 : lines by lang");
        linesByLang.forEach((k, v) -> System.out.println("\t-" + k + " : " + v.size()));
    }

    private void write() throws IOException {
        for (String lang : LANGS) {
            String pathname = "d:/Temp/examples_jpn_" + lang + ".csv";
            File fileOut = new File(pathname);
            FileUtils.writeLines(fileOut, linesByLang.get(lang));
            System.out.println("File saved : " + pathname);
        }
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
