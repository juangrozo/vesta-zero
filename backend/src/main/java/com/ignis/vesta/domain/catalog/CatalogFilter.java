package com.ignis.vesta.domain.catalog;

import com.ignis.vesta.domain.model.InvestigationSummary;

import java.util.Optional;

/** Catalog filter (spec F1). An empty criterion matches everything; criteria combine with AND. */
public record CatalogFilter(
        Optional<InvestigationType> type,
        Optional<HardwareFamily> hardwareFamily,
        Optional<FuelState> fuelState,
        Optional<SafetyPillar> safetyPillar) {

    public static final CatalogFilter NONE =
            new CatalogFilter(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    public boolean matches(InvestigationSummary summary) {
        CatalogEntry c = summary.classification();
        return type.map(t -> t == summary.type()).orElse(true)
                && hardwareFamily.map(h -> h == c.hardwareFamily()).orElse(true)
                && fuelState.map(f -> f == c.fuelState()).orElse(true)
                && safetyPillar.map(p -> p == c.safetyPillar()).orElse(true);
    }
}
