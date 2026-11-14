package com.ignis.vesta.domain.model;

import java.util.List;

/** Everything ingested for one investigation; persisted atomically. */
public record IngestedInvestigation(Investigation investigation, List<TestCondition> conditions,
                                    List<IngestionWarning> warnings) {

    public IngestedInvestigation {
        conditions = List.copyOf(conditions);
        warnings = List.copyOf(warnings);
    }

    public long conditionsWithNormalizedValues() {
        return conditions.stream().filter(TestCondition::hasNormalizedConditions).count();
    }
}
