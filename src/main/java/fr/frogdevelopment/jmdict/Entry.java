package fr.frogdevelopment.jmdict;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Entry {

    public String kanji = "";
    public String reading = "";
    public final Set<Sense> senses = new HashSet<>();

//    @Override
//    public String toString() {
//        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
//                .append("kanji", kanji)
//                .append("reading", reading)
//                .append("senses", senses)
//                .toString();
//    }

    // kanji|reading|senses
    @Override
    public String toString() {
        return kanji + "|" + reading + "|" + senses.stream().map(Sense::toString).collect(Collectors.joining("|"));
    }

    public static Entry fromString(String value) {
        Entry entry = new Entry();

        String[] values = value.split("\\|", 3);

        entry.kanji= values[0];
        entry.reading= values[1];

        String[] senses = values[2].split("\\|");

        entry.senses.addAll(Arrays.stream(senses).map(Sense::fromString).collect(Collectors.toSet()));

        return entry;
    }
}
