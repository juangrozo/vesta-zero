package com.ignis.vesta.entrypoint.http.dto;

import com.ignis.vesta.domain.extraction.ExtractedFact;
import com.ignis.vesta.domain.extraction.ExtractionField;
import com.ignis.vesta.domain.extraction.InvestigationExtraction;
import com.ignis.vesta.domain.vo.Provenance;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * AI-extracted summary of one investigation (spec F2). {@code extracted=false} means the extraction has not been
 * run; an empty list means the record does not document that kind of fact.
 */
public record ExtractionDto(
        String psiId,
        boolean extracted,
        Provenance provenance,
        String model,
        String promptVersion,
        Instant extractedAt,
        List<FactDto> fuelsAndSamples,
        List<FactDto> measurements,
        List<FactDto> testVariables,
        List<FactDto> testConstants,
        List<FactDto> earthApplications) {

    public record FactDto(String label, String detail, String sourceSpan) {
        static FactDto from(ExtractedFact fact) {
            return new FactDto(fact.label(), fact.detail().isEmpty() ? null : fact.detail(), fact.sourceSpan());
        }
    }

    public static ExtractionDto from(String psiId, Optional<InvestigationExtraction> stored) {
        if (stored.isEmpty()) {
            return new ExtractionDto(psiId, false, Provenance.AI_EXTRACTED, null, null, null, List.of(), List.of(),
                    List.of(), List.of(), List.of());
        }
        InvestigationExtraction e = stored.get();
        return new ExtractionDto(psiId, true, Provenance.AI_EXTRACTED, e.model(), e.promptVersion(),
                e.extractedAt(), facts(e, ExtractionField.FUELS_AND_SAMPLES), facts(e, ExtractionField.MEASUREMENTS),
                facts(e, ExtractionField.TEST_VARIABLES), facts(e, ExtractionField.TEST_CONSTANTS),
                facts(e, ExtractionField.EARTH_APPLICATIONS));
    }

    private static List<FactDto> facts(InvestigationExtraction e, ExtractionField field) {
        return e.facts(field).stream().map(FactDto::from).toList();
    }
}
