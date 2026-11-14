package com.ignis.vesta.domain.assistant;

import com.ignis.vesta.domain.vo.PsiId;

import java.util.List;
import java.util.Objects;

/**
 * Answer after grounding checks: only citations to investigations in the corpus survive, and any number that
 * does not appear in the source records is listed in {@code unverifiedNumbers} so the UI can warn about it.
 */
public record AssistantAnswer(
        String text,
        List<PsiId> citations,
        List<String> droppedCitations,
        List<String> unverifiedNumbers,
        boolean insufficientData,
        boolean declined,
        boolean partialGravityNotice,
        String model,
        boolean fromCache) {

    public AssistantAnswer {
        Objects.requireNonNull(text);
        citations = List.copyOf(citations);
        droppedCitations = List.copyOf(droppedCitations);
        unverifiedNumbers = List.copyOf(unverifiedNumbers);
    }
}
