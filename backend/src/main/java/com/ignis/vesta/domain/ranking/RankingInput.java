package com.ignis.vesta.domain.ranking;

import com.ignis.vesta.domain.model.InvestigationSummary;
import com.ignis.vesta.domain.model.TestCondition;

import java.util.List;
import java.util.Objects;

/** What a criterion may look at: catalog data plus the investigation's normalized test conditions. */
public record RankingInput(InvestigationSummary summary, List<TestCondition> conditions) {

    public RankingInput {
        Objects.requireNonNull(summary);
        conditions = List.copyOf(conditions);
    }
}
