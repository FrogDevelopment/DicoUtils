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
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        if (!pos.isEmpty()) {
            builder.append("pos", pos);
        }

        if (!field.isEmpty()) {
            builder.append("field", field);
        }

        if (!misc.isEmpty()) {
            builder.append("misc", misc);
        }

        builder.append("gloss", gloss);

        return builder.toString();
    }
}
