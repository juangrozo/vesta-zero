package com.ignis.vesta.domain.ranking;

import com.ignis.vesta.domain.vo.Provenance;

/**
 * Strategy for one ranking criterion. Contract (Liskov): {@link #evaluate} is deterministic, never throws for a
 * valid input, and returns a score in [0, 1] with a reason; missing data scores 0 and says "not documented".
 */
public interface RankingCriterion {

    /** Stable identifier used in the API for weights. */
    String id();

    String name();

    /** How the score is computed, shown in the UI ("how is this scored?"). */
    String description();

    double defaultWeight();

    /** Where the data behind this criterion comes from. */
    Provenance provenance();

    CriterionScore evaluate(RankingInput input);
}
