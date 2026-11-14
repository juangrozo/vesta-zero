package com.ignis.vesta.domain.ranking;

import com.ignis.vesta.domain.ranking.criteria.HabitatRelevanceCriterion;
import com.ignis.vesta.domain.ranking.criteria.AtmosphereProximityCriterion;
import com.ignis.vesta.domain.ranking.criteria.MaterialTypeCriterion;
import com.ignis.vesta.domain.ranking.criteria.SafetyPillarCriterion;
import com.ignis.vesta.domain.ranking.criteria.StructuredDataCriterion;

import java.util.List;

/** The criteria from the spec, in display order. Adding a criterion means adding a class here, nothing else. */
public final class DefaultRankingCriteria {

    private DefaultRankingCriteria() {
    }

    public static List<RankingCriterion> forEnvironment(HabitatEnvironment environment) {
        return List.of(
                new AtmosphereProximityCriterion(environment),
                new HabitatRelevanceCriterion(),
                new MaterialTypeCriterion(),
                new SafetyPillarCriterion(),
                new StructuredDataCriterion());
    }
}
