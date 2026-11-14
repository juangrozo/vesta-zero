package com.ignis.vesta.domain.model;

import com.ignis.vesta.domain.catalog.CatalogEntry;
import com.ignis.vesta.domain.vo.Doi;
import com.ignis.vesta.domain.vo.PsiId;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * A PSI investigation as documented by PSI, plus its curated catalog classification.
 * {@code rawJson} is the sanitized PSI record (no file lists, no contact emails or phones).
 */
public record Investigation(
        PsiId psiId,
        String acronym,
        String title,
        Doi doi,
        String projectType,
        String researchArea,
        String subArea,
        String flightProgram,
        String hardware,
        String objective,
        String approach,
        String hypothesis,
        String researchImpacts,
        LocalDate startDate,
        LocalDate endDate,
        List<Publication> publications,
        List<ExperimentalTableRef> experimentalTables,
        CatalogEntry classification,
        String rawJson) {

    public Investigation {
        Objects.requireNonNull(psiId);
        Objects.requireNonNull(title);
        Objects.requireNonNull(doi);
        Objects.requireNonNull(classification);
        Objects.requireNonNull(rawJson);
        publications = List.copyOf(publications);
        experimentalTables = List.copyOf(experimentalTables);
    }
}
