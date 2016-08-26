package fr.frogdevelopment.jmdict;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.HashSet;
import java.util.Set;

public class Entry {

    public String kanji;
    public String reading;
    public final Set<Sense> senses = new HashSet<>();

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("kanji", kanji)
                .append("reading", reading)
                .append("senses", senses)
                .toString();
    }
}
