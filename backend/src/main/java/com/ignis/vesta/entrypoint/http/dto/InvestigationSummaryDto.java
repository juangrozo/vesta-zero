package com.ignis.vesta.entrypoint.http.dto;

import com.ignis.vesta.domain.model.InvestigationSummary;

/**
 * Catalog row as exposed over HTTP. {@code classification} values are curated by Vesta Zero;
 * every other field is documented by PSI.
 */
public record InvestigationSummaryDto(
        String psiId,
        String acronym,
        String title,
        String doi,
        String doiUrl,
        String type,
        String projectType,
        String subArea,
        String hardware,
        Classification classification,
        ExperimentalData experimentalData) {

    public record Classification(String hardwareFamily, String fuelState, String safetyPillar, String provenance) {
    }

    public record ExperimentalData(boolean available, int tables, long testRows, long normalizedTestRows) {
    }

    public static InvestigationSummaryDto from(InvestigationSummary s) {
        var c = s.classification();
        return new InvestigationSummaryDto(
                s.psiId().value(),
                s.acronym(),
                s.title(),
                s.doi().value(),
                s.doi().url(),
                s.type().name(),
                s.projectType(),
                s.subArea(),
                s.hardware(),
                new Classification(c.hardwareFamily().name(), c.fuelState().name(), c.safetyPillar().name(), "CURATED"),
                new ExperimentalData(s.hasExperimentalTable(), s.experimentalTableCount(), s.testCount(),
                        s.normalizedTestCount()));
    }
}
