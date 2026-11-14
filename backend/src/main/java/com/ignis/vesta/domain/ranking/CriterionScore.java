package com.ignis.vesta.domain.ranking;

import java.util.Objects;

/** A criterion's verdict: a score in [0, 1] and a one-line reason a person can check. */
public record CriterionScore(double score, String reason) {

    public CriterionScore {
        if (Double.isNaN(score) || score < 0.0 || score > 1.0) {
            throw new IllegalArgumentException("Criterion score must be in [0, 1], was " + score);
        }
        Objects.requireNonNull(reason);
    }

    public static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
