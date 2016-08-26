package fr.frogdevelopment.jmdict;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private static final Pattern ENTITY_PATTERN = Pattern.compile("<!ENTITY (?<code>.*) \"(?<label>.*)\">");

    private static final String JMDICT_START = "<JMdict>";

    private static final String ENTRY_START = "<entry>";
    private static final String ENTRY_END = "</entry>";

    private static final String KANJI_ELEMENT_START = "<k_ele>";

    private static final Pattern KANJI_PATTERN = Pattern.compile("<keb>(?<kanji>.*)</keb>");

    private static final String READING_ELEMENT_START = "<r_ele>";
    private static final Pattern READING_PATTERN = Pattern.compile("<reb>(?<reading>.*)</reb>");

    private static final String SENSE_START = "<sense>";
    private static final String SENSE_END = "</sense>";

    private static final Pattern POS_PATTERN = Pattern.compile("^<pos>&(?<pos>.*);</pos>$");
    private static final Pattern XREF_PATTERN = Pattern.compile("^<xref>(?<reading>.*)</xref>$");
    private static final Pattern ANT_PATTERN = Pattern.compile("^<ant>(?<reading>.*)</ant>$");
    private static final Pattern FIELD_PATTERN = Pattern.compile("^<field>&(?<field>.*);</field>$");
    private static final Pattern MISC_PATTERN = Pattern.compile("^<misc>&(?<misc>.*);</misc>$");
    private static final Pattern DIAL_PATTERN = Pattern.compile("^<dial>&(?<dial>.*);</dial>$");

    private static final Pattern GLOSS_PATTERN = Pattern.compile("^<gloss( xml:lang=\"(?<lang>\\w{2,3})\")?>(?<value>.*)(.*)</gloss>$");

    //
    private final Map<String, String> LEXICON = new HashMap<>();
    final List<Entry> ENTRIES = new ArrayList<>();

    private final String language;
    private boolean isDefaultLanguage;

    public Parser(String language) {
        this.language = language;
        isDefaultLanguage = "eng".equalsIgnoreCase(language);
    }

    public void parse(File file) {

        try {
            LineIterator it = FileUtils.lineIterator(file, "UTF-8");
            try {

                while (it.hasNext()) {
                    String line = it.nextLine();

                    if (JMDICT_START.equals(line)) {
                        fetchEntries(it);
                    } else {
                        Matcher matcher = ENTITY_PATTERN.matcher(line);
                        if (matcher.matches()) {
                            LEXICON.put(matcher.group("code"), matcher.group("label"));
                        }
                    }
                }
            } finally {
                LineIterator.closeQuietly(it);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchEntries(LineIterator it) {
        Entry entry = new Entry();
        while (it.hasNext()) {
            String line = it.nextLine();
//            System.out.println(line);

            switch (line) {
                case ENTRY_END:
                    if (!entry.senses.isEmpty()) {
                        ENTRIES.add(entry);
                    }
                    break;

                case KANJI_ELEMENT_START:
                    getKanji(it, entry);
                    break;

                case READING_ELEMENT_START:
                    getReading(it, entry);
                    break;

                case SENSE_START:
                    getSenses(it, entry);
                    break;
            }
        }
    }

    private void getKanji(LineIterator it, Entry entry) {
        String line = it.nextLine();
//        System.out.println(line);

        Matcher matcher = KANJI_PATTERN.matcher(line);
        if (matcher.matches()) {
            entry.kanji = matcher.group("kanji");
        }
    }

    private void getReading(LineIterator it, Entry data) {
        String line = it.nextLine();
//        System.out.println(line);

        Matcher matcher = READING_PATTERN.matcher(line);
        if (matcher.matches()) {
            data.reading = matcher.group("reading");
        }
    }

    private void getSenses(LineIterator it, Entry entry) {
        Sense sense = new Sense();
        Matcher matcher;
        while (it.hasNext()) {
            String line = it.nextLine();
//            System.out.println(line);

            if (SENSE_END.equals(line)) {
                if (!sense.gloss.isEmpty()) {
                    entry.senses.add(sense);
                }
                break;
            }

            matcher = XREF_PATTERN.matcher(line);
            if (matcher.matches()) {
                // todo manage cross-reference
                continue;
            }

            matcher = ANT_PATTERN.matcher(line);
            if (matcher.matches()) {
                // todo manage antonym
                continue;
            }

            matcher = POS_PATTERN.matcher(line);
            if (matcher.matches()) {
                sense.pos.add(matcher.group("pos"));
                continue;
            }

            matcher = FIELD_PATTERN.matcher(line);
            if (matcher.matches()) {
                sense.field.add(matcher.group("field"));
                continue;
            }

            matcher = MISC_PATTERN.matcher(line);
            if (matcher.matches()) {
                sense.misc.add(matcher.group("misc"));
                continue;
            }
            matcher = DIAL_PATTERN.matcher(line);
            if (matcher.matches()) {
                sense.dial.add(matcher.group("dial"));
                continue;
            }

            matcher = GLOSS_PATTERN.matcher(line);
            if (matcher.matches()) {
                String lang = matcher.group("lang");
                // by default eng language country is absent
                if ((isDefaultLanguage && lang == null) || language.equalsIgnoreCase(lang)) {
                    sense.gloss.add(matcher.group("value"));
                }
                continue;
            } else {
                // todo
//                System.out.println(line);
            }
        }
    }
}
