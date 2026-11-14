package com.ignis.vesta.domain.extraction;

import com.ignis.vesta.domain.vo.PsiId;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** The verified AI extraction stored for one investigation. */
public record InvestigationExtraction(PsiId psiId, String model, String promptVersion, Instant extractedAt,
                                      List<ExtractedFact> facts) {

    public InvestigationExtraction {
        Objects.requireNonNull(psiId);
        Objects.requireNonNull(model);
        Objects.requireNonNull(promptVersion);
        facts = List.copyOf(facts);
    }

    public List<ExtractedFact> facts(ExtractionField field) {
        return facts.stream().filter(f -> f.field() == field).toList();
    }
}
