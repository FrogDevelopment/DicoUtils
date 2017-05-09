package fr.frogdevelopment.dico.jmdict;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Sense {

    final Set<String> pos = new HashSet<>();
    final Set<String> field = new HashSet<>();
    final Set<String> misc = new HashSet<>();
    String info = "";
    final Set<String> dial = new HashSet<>();
    final Set<String> gloss = new HashSet<>();

    final Map<String, Set<String>> glossByLang = new HashMap<>();

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("pos", pos)
                .append("field", field)
                .append("misc", misc)
                .append("info", info)
                .append("dial", dial)
                .append("gloss", gloss)
                .append("glossByLang", glossByLang)
                .toString();
    }

    boolean containsLang(String lang) {
        return glossByLang.containsKey(lang);
    }

    // pos/field/misc/info/dial/gloss
    String toString(String lang) {
        StringBuilder builder = new StringBuilder()
                .append(String.join(";", pos)).append("/")
                .append(String.join(";", field)).append("/")
                .append(String.join(";", misc)).append("/")
                .append(info).append("/")
                .append(String.join(";", dial)).append("/");

        Optional<Map.Entry<String, Set<String>>> optional = glossByLang.entrySet()
                                                                    .stream()
                                                                    .filter(e -> e.getKey().equals(lang))
                                                                    .findFirst();
        if (optional.isPresent()) {
            Set<String> gloss = optional.get().getValue();
            builder.append(String.join(";", gloss));
        } else {
            System.out.println("");
        }

        return builder.toString();
    }

    static Sense fromString(String value) {
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
