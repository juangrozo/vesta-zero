package com.ignis.vesta.domain.ranking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Deterministic weighted-sum ranking. Score = Σ(normalized weight × criterion score). Ties are broken by
 * PSI id so the same inputs always give the same order. Closed for modification: criteria are injected.
 */
public final class RankingEngine {

    private final List<RankingCriterion> criteria;

    public RankingEngine(List<RankingCriterion> criteria) {
        if (criteria.isEmpty()) {
            throw new IllegalArgumentException("At least one ranking criterion is required");
        }
        this.criteria = List.copyOf(criteria);
    }

    public List<RankingCriterion> criteria() {
        return criteria;
    }

    public List<RankedInvestigation> rank(List<RankingInput> inputs, RankingWeights weights) {
        Objects.requireNonNull(weights);
        record Scored(RankingInput input, double total, List<RankedInvestigation.Contribution> breakdown) {
        }
        List<Scored> scored = new ArrayList<>();
        for (RankingInput input : inputs) {
            List<RankedInvestigation.Contribution> breakdown = new ArrayList<>();
            double total = 0;
            for (RankingCriterion criterion : criteria) {
                CriterionScore result = criterion.evaluate(input);
                double weight = weights.weightOf(criterion.id());
                double contribution = weight * result.score();
                total += contribution;
                breakdown.add(new RankedInvestigation.Contribution(criterion, weight, result.score(), contribution,
                        result.reason()));
            }
            scored.add(new Scored(input, total, breakdown));
        }
        scored.sort(Comparator.comparingDouble(Scored::total).reversed()
                .thenComparingInt(s -> s.input().summary().psiId().number()));
        List<RankedInvestigation> ranked = new ArrayList<>();
        for (int i = 0; i < scored.size(); i++) {
            Scored s = scored.get(i);
            ranked.add(new RankedInvestigation(i + 1, s.input().summary(), s.total(), s.breakdown()));
        }
        return ranked;
    }
}
