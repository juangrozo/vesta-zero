package com.ignis.vesta.domain.model;

import java.util.List;
import java.util.Objects;

/** Everything the detail view needs for one investigation (spec F2). */
public record InvestigationDetail(Investigation investigation, ConditionsSummary conditions,
                                  List<IngestionWarning> warnings) {

    public InvestigationDetail {
        Objects.requireNonNull(investigation);
        Objects.requireNonNull(conditions);
        warnings = List.copyOf(warnings);
    }
}
