package fr.frogdevelopment.dico.jmdict;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Entry {

    String seq = "";
    String kanji = "";
    String reading = "";
    final Set<Sense> senses = new HashSet<>();

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("seq", seq)
                .append("kanji", kanji)
                .append("reading", reading)
                .append("senses", senses)
                .toString();
    }

    boolean containsLang(String lang) {
        return senses.stream().anyMatch(s -> s.containsLang(lang));
    }

    // kanji|reading|senses
    public String toString(String lang) {
        return kanji + "|" + reading + "|" + senses.stream()
                                                   .filter(s -> s.containsLang(lang))
                                                   .map(sense -> sense.toString(lang))
                                                   .collect(Collectors.joining("|"));
    }

    public static Entry fromString(String value) {
        Entry entry = new Entry();

        String[] values = value.split("\\|", 3);

        entry.kanji = values[0];
        entry.reading = values[1];

        String[] senses = values[2].split("\\|");

        entry.senses.addAll(Arrays.stream(senses).map(Sense::fromString).collect(Collectors.toSet()));

        return entry;
    }
}
