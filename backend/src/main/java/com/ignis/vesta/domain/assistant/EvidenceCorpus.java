package com.ignis.vesta.domain.assistant;

import com.ignis.vesta.domain.vo.PsiId;

import java.util.Objects;
import java.util.Set;

/**
 * The only material the assistant may answer from: a deterministic text rendering of the stored investigations.
 * Same data gives the same text and fingerprint, which keeps the prompt cacheable and the answer cache valid.
 */
public record EvidenceCorpus(String text, Set<PsiId> psiIds, String fingerprint) {

    public EvidenceCorpus {
        Objects.requireNonNull(text);
        Objects.requireNonNull(fingerprint);
        psiIds = Set.copyOf(psiIds);
    }
}
