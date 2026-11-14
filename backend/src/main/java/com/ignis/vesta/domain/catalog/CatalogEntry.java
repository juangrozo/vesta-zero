package com.ignis.vesta.domain.catalog;

import com.ignis.vesta.domain.vo.PsiId;

import java.util.Objects;

/** One in-scope investigation and its curated classification (shown in the UI as curated, not documented). */
public record CatalogEntry(PsiId psiId, HardwareFamily hardwareFamily, FuelState fuelState, SafetyPillar safetyPillar) {

    public CatalogEntry {
        Objects.requireNonNull(psiId);
        Objects.requireNonNull(hardwareFamily);
        Objects.requireNonNull(fuelState);
        Objects.requireNonNull(safetyPillar);
    }
}
