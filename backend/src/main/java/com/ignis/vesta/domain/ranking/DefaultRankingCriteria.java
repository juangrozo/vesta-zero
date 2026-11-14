package com.ignis.vesta.domain.ranking;

import com.ignis.vesta.domain.ranking.criteria.HabitatRelevanceCriterion;
import com.ignis.vesta.domain.ranking.criteria.LunarAtmosphereProximityCriterion;
import com.ignis.vesta.domain.ranking.criteria.MaterialTypeCriterion;
import com.ignis.vesta.domain.ranking.criteria.SafetyPillarCriterion;
import com.ignis.vesta.domain.ranking.criteria.StructuredDataCriterion;

import java.util.List;

/** The criteria from the spec, in display order. Adding a criterion means adding a class here, nothing else. */
public final class DefaultRankingCriteria {

    private DefaultRankingCriteria() {
    }

    public static List<RankingCriterion> all() {
        return List.of(
                new LunarAtmosphereProximityCriterion(),
                new HabitatRelevanceCriterion(),
                new MaterialTypeCriterion(),
                new SafetyPillarCriterion(),
                new StructuredDataCriterion());
    }
}
