package fr.frogdevelopment.dico.jmdict;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class JMDictFetcher {

    private static final String URL = "ftp://ftp.monash.edu.au/pub/nihongo/JMdict.gz";

    private static final String JMDICT_START = "<JMdict>";
    private static final String JMDICT_END = "</JMdict>";

    private static final String ENTRY_START = "<entry>";
    private static final String ENTRY_END = "</entry>";
    private static final Pattern SEQ_PATTERN = Pattern.compile("^<ent_seq>(?<seq>\\d+)</ent_seq>$");

    private static final String KANJI_ELEMENT_START = "<k_ele>";
    private static final String KANJI_ELEMENT_END = "</k_ele>";

    private static final Pattern KANJI_PATTERN = Pattern.compile("<keb>(?<kanji>.*)</keb>");

    private static final String READING_ELEMENT_START = "<r_ele>";
    private static final String READING_ELEMENT_END = "</r_ele>";
    private static final Pattern READING_PATTERN = Pattern.compile("<reb>(?<reading>.*)</reb>");

    private static final String SENSE_START = "<sense>";
    private static final String SENSE_END = "</sense>";

    private static final Pattern POS_PATTERN = Pattern.compile("^<pos>&(?<pos>.*);</pos>$");
    private static final Pattern XREF_PATTERN = Pattern.compile("^<xref>(?<reading>.*)</xref>$");
    private static final Pattern ANT_PATTERN = Pattern.compile("^<ant>(?<reading>.*)</ant>$");
    private static final Pattern FIELD_PATTERN = Pattern.compile("^<field>&(?<field>.*);</field>$");
    private static final Pattern MISC_PATTERN = Pattern.compile("^<misc>&(?<misc>.*);</misc>$");
    private static final Pattern INFO_PATTERN = Pattern.compile("^<s_inf>(?<info>.*)</s_inf>$");
    private static final Pattern DIAL_PATTERN = Pattern.compile("^<dial>&(?<dial>.*);</dial>$");

    private static final Pattern GLOSS_PATTERN = Pattern.compile("^<gloss( xml:lang=\"(?<lang>\\w{3})\")?>(?<value>.*)(.*)</gloss>$");

    private final List<Entry> entries = new ArrayList<>();

    private boolean readJmDict = false;
    private Entry currentEntry = null;
    private Sense currentSense = null;

    private boolean readEntry = false;
    private boolean readKanji = false;
    private boolean readElement = false;
    private boolean readSense = false;

    private int n = 0;

    void fetch() throws IOException, URISyntaxException {
        try (BufferedInputStream in = new BufferedInputStream(new URL(URL).openStream());
             GzipCompressorInputStream gzIn = new GzipCompressorInputStream(in);
             BufferedReader br = new BufferedReader(new InputStreamReader(gzIn))) {

            System.out.println("reading " + URL);

            String line;
            while ((line = br.readLine()) != null) {
                n++;

                if (readJmDict) {
                    switch (line) {
                        case JMDICT_END:
                            readJmDict = false;
                            break;

                        case ENTRY_END:
                            // do not add entry if empty senses (filtered by language)
                            if (!currentEntry.senses.isEmpty()) {
                                entries.add(currentEntry);
                            }
                            continue;

                        case ENTRY_START:
                            currentEntry = new Entry();
                            readEntry = true;
                            continue;

                        case KANJI_ELEMENT_START:
                            readEntry = false;
                            readKanji = true;
                            continue;

                        case KANJI_ELEMENT_END:
                            readKanji = false;
                            continue;

                        case READING_ELEMENT_START:
                            readEntry = false;
                            readElement = true;
                            continue;

                        case READING_ELEMENT_END:
                            readElement = false;
                            continue;

                        case SENSE_START:
                            readSense = true;
                            currentSense = new Sense();
                            continue;

                        case SENSE_END:
                            readSense = false;
                            currentEntry.senses.add(currentSense);
                            continue;

                        default:
                            // nothing to do, keep
                            break;
                    }
                }

                if (readEntry) {
                    readEntry(line);
                } else if (readKanji) {
                    readKanji(line);
                } else if (readElement) {
                    readElement(line);
                } else if (readSense) {
                    readSense(line);

                } else if (JMDICT_START.equals(line)) {
                    readJmDict = true;
                }
            }
            System.out.println("nb lines : " + n);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readEntry(String line) {
        Matcher matcher = SEQ_PATTERN.matcher(line);
        if (matcher.matches()) {
            currentEntry.seq = matcher.group("seq");
        } else {
            throw new IllegalStateException("No entry_seq found in line : " + line);
        }
    }

    private void readKanji(String line) {
        Matcher matcher = KANJI_PATTERN.matcher(line);
        if (matcher.matches()) {
            currentEntry.kanji = matcher.group("kanji");
        }
    }

    private void readElement(String line) {
        Matcher matcher = READING_PATTERN.matcher(line);
        if (matcher.matches()) {
            currentEntry.reading = matcher.group("reading");
        }
    }

    private void readSense(String line) {
        Matcher matcher;
//                System.out.println(line);

        matcher = XREF_PATTERN.matcher(line);
        if (matcher.matches()) {
            // toLine manage cross-reference
            return;
        }

        matcher = ANT_PATTERN.matcher(line);
        if (matcher.matches()) {
            // toLine manage antonym
            return;
        }

        matcher = POS_PATTERN.matcher(line);
        if (matcher.matches()) {
            currentSense.pos.add(matcher.group("pos"));
            return;
        }

        matcher = FIELD_PATTERN.matcher(line);
        if (matcher.matches()) {
            currentSense.field.add(matcher.group("field"));
            return;
        }

        matcher = MISC_PATTERN.matcher(line);
        if (matcher.matches()) {
            currentSense.misc.add(matcher.group("misc"));
            return;
        }

        matcher = INFO_PATTERN.matcher(line);
        if (matcher.matches()) {
            currentSense.info = matcher.group("info");
            return;
        }

        matcher = DIAL_PATTERN.matcher(line);
        if (matcher.matches()) {
            currentSense.dial.add(matcher.group("dial"));
            return;
        }

        matcher = GLOSS_PATTERN.matcher(line);
        if (matcher.matches()) {
            String lang = matcher.group("lang");
            if (StringUtils.isBlank(lang)) {
                lang = "eng";
            }

            langs.add(lang);

            if (!currentSense.glossByLang.containsKey(lang)) {
                currentSense.glossByLang.put(lang, new HashSet<>());
            }
            currentSense.glossByLang.get(lang).add(matcher.group("value"));
        }
    }

    private final Set<String> langs = new HashSet<>();

    void write(String directoryPathName) throws IOException {
        for (String lang : langs) {
            File fileOut = Paths.get(directoryPathName, "entries_" + lang + ".txt").toFile();
            List<String> filteredEntries = entries.stream()
                                                 .filter(e -> e.containsLang(lang))
                                                 .map(e->e.toString(lang))
                                                 .collect(Collectors.toList());
            FileUtils.writeLines(fileOut, StandardCharsets.UTF_8.name(), filteredEntries, false);
        }

    }

}
