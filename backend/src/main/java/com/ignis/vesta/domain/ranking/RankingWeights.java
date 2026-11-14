package com.ignis.vesta.domain.ranking;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validated, normalized weights (they sum to 1). Requested weights may use any non-negative scale; a criterion
 * that is not mentioned keeps its default weight before normalization.
 */
public final class RankingWeights {

    private final Map<String, Double> normalized;

    private RankingWeights(Map<String, Double> normalized) {
        this.normalized = Map.copyOf(normalized);
    }

    public static RankingWeights defaults(List<RankingCriterion> criteria) {
        return of(criteria, Map.of());
    }

    public static RankingWeights of(List<RankingCriterion> criteria, Map<String, Double> requested) {
        Set<String> known = criteria.stream().map(RankingCriterion::id).collect(Collectors.toSet());
        for (Map.Entry<String, Double> entry : requested.entrySet()) {
            if (!known.contains(entry.getKey())) {
                throw new IllegalArgumentException("Unknown criterion '" + entry.getKey() + "'. Known: " + known);
            }
            Double value = entry.getValue();
            if (value == null || !Double.isFinite(value) || value < 0) {
                throw new IllegalArgumentException("Weight for '" + entry.getKey() + "' must be a non-negative number");
            }
        }
        Map<String, Double> raw = new LinkedHashMap<>();
        criteria.forEach(c -> raw.put(c.id(), requested.getOrDefault(c.id(), c.defaultWeight())));
        double total = raw.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total <= 0) {
            throw new IllegalArgumentException("At least one weight must be greater than zero");
        }
        Map<String, Double> normalized = new LinkedHashMap<>();
        raw.forEach((id, w) -> normalized.put(id, w / total));
        return new RankingWeights(normalized);
    }

    public double weightOf(String criterionId) {
        Double weight = normalized.get(criterionId);
        if (weight == null) {
            throw new IllegalArgumentException("No weight for criterion " + criterionId);
        }
        return weight;
    }

    public Map<String, Double> asMap() {
        return normalized;
    }
}
