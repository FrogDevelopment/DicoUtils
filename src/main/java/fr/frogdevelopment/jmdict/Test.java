package fr.frogdevelopment.jmdict;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

    private static class Data {

        private String kanji;
        private String reading;
        private final Map<String, List<String>> senses = new HashMap<>();

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("kanji", kanji)
                    .append("reading", reading)
                    .append("senses", senses)
                    .toString();
        }
    }

    private static final Pattern ENTITY_PATTERN = Pattern.compile("<!ENTITY (?<code>.*) \"(?<label>.*)\">");

    private static final String JMDICT_START = "<JMdict>";

    private static final String ENTRY_START = "<entry>";
    private static final String ENTRY_END = "</entry>";

    private static final String KANJI_ELEMENT_START = "<k_ele>";

    private static final Pattern KANJI_PATTERN = Pattern.compile("<keb>((?<kanji>.*).*)</keb>");

    private static final String READING_ELEMENT_START = "<r_ele>";
    private static final Pattern READING_PATTERN = Pattern.compile("<reb>((?<reading>.*).*)</reb>");

    private static final String SENSE_START = "<sense>";
    private static final String SENSE_END = "</sense>";

    private static final Pattern KEY_PATTERN = Pattern.compile("^<(pos|field|misc)>&(?<key>.*);</(pos|field|misc)>$");

    private static final Pattern GLOSS_PATTERN = Pattern.compile("<gloss>(?<value>.*)(.*)</gloss>");

    //
    private static final Map<String, String> LEXIQUE = new HashMap<>();
    private static final List<Data> DATAS = new ArrayList<>();

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        try {
            URL dir_url = ClassLoader.getSystemResource("JMdict");
            File file = new File(dir_url.toURI());

            LineIterator it = FileUtils.lineIterator(file, "UTF-8");
            try {

                while (it.hasNext()) {
                    String line = it.nextLine();

                    if (JMDICT_START.equals(line)) {
                        fetchDatas(it);
                    } else {
                        Matcher matcher = ENTITY_PATTERN.matcher(line);
                        if (matcher.matches()) {
                            LEXIQUE.put(matcher.group("code"), matcher.group("label"));
                        }
                    }

                }
            } finally {
                LineIterator.closeQuietly(it);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("nb data : " + DATAS.size() + " in " + (System.currentTimeMillis() - start) + "ms");
    }

    private static void fetchDatas(LineIterator it) {
        Data data = null;
        while (it.hasNext()) {
            String line = it.nextLine();
            System.out.println(line);
            switch (line) {
                case ENTRY_END:
                    DATAS.add(data);
                    break;

                case ENTRY_START:
                    data = new Data();
                    break;

                case KANJI_ELEMENT_START:
                    data.kanji = getKanji(it);
                    break;

                case READING_ELEMENT_START:
                    data.reading = getReading(it);
                    break;

                case SENSE_START:
                    getSenses(it, data.senses);
                    break;
            }
        }
    }

    private static String getKanji(LineIterator it) {
        String line = it.nextLine();
        Matcher matcher = KANJI_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group("kanji");
        } else {
            return null;
        }
    }

    private static String getReading(LineIterator it) {
        String line = it.nextLine();
        Matcher matcher = READING_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group("reading");
        } else {
            return null;
        }
    }

    private static void getSenses(LineIterator it, Map<String, List<String>> senses) {
        String line = it.nextLine();

        String key;

        Matcher matcher = KEY_PATTERN.matcher(line);
        if (matcher.matches()) {
            key = matcher.group("key");
        } else {
            key = ""; // empty
        }

        if (senses.containsKey(key)) {
            System.out.println(senses);
        }else {
            senses.put(key, new ArrayList<>());
        }

        while (it.hasNext()) {
            line = it.nextLine();

            if (SENSE_END.equals(line)) {
                break;
            }

            matcher = GLOSS_PATTERN.matcher(line);
            if (matcher.matches()) {
                senses.get(key).add(matcher.group("value"));
            }
        }
    }
}
