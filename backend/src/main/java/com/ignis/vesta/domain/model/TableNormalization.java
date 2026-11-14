package com.ignis.vesta.domain.model;

import java.util.List;

/** Result of normalizing one experimental table. */
public record TableNormalization(String sourceFile, String parserName, List<TestCondition> conditions,
                                 List<IngestionWarning> warnings) {

    public TableNormalization {
        conditions = List.copyOf(conditions);
        warnings = List.copyOf(warnings);
    }
}
