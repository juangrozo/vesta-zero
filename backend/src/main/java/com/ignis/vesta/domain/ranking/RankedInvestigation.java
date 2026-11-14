package com.ignis.vesta.domain.ranking;

import com.ignis.vesta.domain.model.InvestigationSummary;

import java.util.List;

/** One ranked result with its full, explainable breakdown. */
public record RankedInvestigation(int position, InvestigationSummary summary, double score,
                                  List<Contribution> breakdown) {

    /** {@code contribution = weight × score}; contributions add up to the total score. */
    public record Contribution(RankingCriterion criterion, double weight, double score, double contribution,
                               String reason) {
    }

    public RankedInvestigation {
        breakdown = List.copyOf(breakdown);
    }
}
