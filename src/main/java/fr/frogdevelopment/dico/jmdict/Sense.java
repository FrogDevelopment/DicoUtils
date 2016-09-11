package fr.frogdevelopment.dico.jmdict;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Sense {

    public final Set<String> pos = new HashSet<>();
    public final Set<String> field = new HashSet<>();
    public final Set<String> misc = new HashSet<>();
    public String info = "";
    public final Set<String> dial = new HashSet<>();
    public final Set<String> gloss = new HashSet<>();

//    @Override
//    public String toString() {
//        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
//        if (!pos.isEmpty()) {
//            builder.append("pos", pos);
//        }
//
//        if (!field.isEmpty()) {
//            builder.append("field", field);
//        }
//
//        if (!misc.isEmpty()) {
//            builder.append("misc", misc);
//        }
//
//        builder.append("gloss", gloss);
//
//        return builder.toString();
//    }

    // pos/field/misc/info/dial/gloss
    @Override
    public String toString() {
        return String.join(";", pos) + "/" + String.join(";", field) + "/" + String.join(";", misc) + "/" + info + "/" + String.join(";", dial) + "/" + String.join(";", gloss);
    }

    public static Sense fromString(String value) {
        Sense sense = new Sense();

        String[] values = value.split("/");
        sense.pos.addAll(Arrays.stream(values[0].split(";")).collect(Collectors.toSet()));
        sense.field.addAll(Arrays.stream(values[1].split(";")).collect(Collectors.toSet()));
        sense.misc.addAll(Arrays.stream(values[2].split(";")).collect(Collectors.toSet()));
        sense.info = values[3];
        sense.dial.addAll(Arrays.stream(values[4].split(";")).collect(Collectors.toSet()));
        sense.gloss.addAll(Arrays.stream(values[5].split(";")).collect(Collectors.toSet()));

        return sense;
    }
}
