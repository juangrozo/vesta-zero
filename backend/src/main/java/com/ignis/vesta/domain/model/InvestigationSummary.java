package com.ignis.vesta.domain.model;

import com.ignis.vesta.domain.catalog.CatalogEntry;
import com.ignis.vesta.domain.catalog.InvestigationType;
import com.ignis.vesta.domain.vo.Doi;
import com.ignis.vesta.domain.vo.PsiId;

import java.util.Objects;

/** Catalog row: documented identity fields, curated classification and experimental-table coverage. */
public record InvestigationSummary(
        PsiId psiId,
        String acronym,
        String title,
        Doi doi,
        String projectType,
        String subArea,
        String hardware,
        CatalogEntry classification,
        int experimentalTableCount,
        long testCount,
        long normalizedTestCount) {

    public InvestigationSummary {
        Objects.requireNonNull(psiId);
        Objects.requireNonNull(title);
        Objects.requireNonNull(doi);
        Objects.requireNonNull(classification);
    }

    public InvestigationType type() {
        return InvestigationType.fromProjectType(projectType);
    }

    public boolean hasExperimentalTable() {
        return experimentalTableCount > 0;
    }
}
