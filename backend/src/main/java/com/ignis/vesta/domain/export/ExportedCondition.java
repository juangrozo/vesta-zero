package com.ignis.vesta.domain.export;

import com.ignis.vesta.domain.model.InvestigationSummary;
import com.ignis.vesta.domain.model.TestCondition;

import java.util.Objects;

/** One exported row: the documented test condition plus the identity of its investigation, for citation. */
public record ExportedCondition(InvestigationSummary investigation, TestCondition condition) {

    public ExportedCondition {
        Objects.requireNonNull(investigation);
        Objects.requireNonNull(condition);
    }
}
