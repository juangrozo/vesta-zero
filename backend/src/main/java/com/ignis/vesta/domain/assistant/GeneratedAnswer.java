package com.ignis.vesta.domain.assistant;

import java.util.List;
import java.util.Objects;

/**
 * Raw answer produced by the language model, before grounding checks.
 * {@code declined} is true when the model refused; then {@code text} explains why.
 */
public record GeneratedAnswer(String text, List<String> citedPsiIds, boolean insufficientData, boolean declined,
                              String model, boolean fromCache) {

    public GeneratedAnswer {
        Objects.requireNonNull(text);
        citedPsiIds = List.copyOf(citedPsiIds);
    }

    public GeneratedAnswer withFromCache(boolean cached) {
        return new GeneratedAnswer(text, citedPsiIds, insufficientData, declined, model, cached);
    }
}
