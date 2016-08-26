package fr.frogdevelopment.jmdict;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.HashSet;
import java.util.Set;

public class Sense {

    public final Set<String> pos = new HashSet<>();
    public final Set<String> field = new HashSet<>();
    public final Set<String> misc = new HashSet<>();
    public final Set<String> dial = new HashSet<>();
    public final Set<String> gloss = new HashSet<>();

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("pos", pos)
                .append("field", field)
                .append("misc", misc)
                .append("gloss", gloss)
                .toString();
    }
}
