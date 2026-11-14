package com.ignis.vesta.domain.assistant;

import java.util.Locale;
import java.util.regex.Pattern;

/** Detects questions about the Moon, Mars or partial gravity, which must carry the partial-gravity data gap. */
public final class PartialGravityTopic {

    private static final Pattern TOPIC = Pattern.compile(
            "\\b(moon|lunar|luna|mars|martian|marte|marciano|partial[- ]gravity|reduced[- ]gravity|"
                    + "gravedad parcial|artemis|regolith|regolito)\\b");

    private PartialGravityTopic() {
    }

    public static boolean mentionedIn(String question) {
        return question != null && TOPIC.matcher(question.toLowerCase(Locale.ROOT)).find();
    }
}
