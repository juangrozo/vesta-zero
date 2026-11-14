package com.ignis.vesta.domain.ranking.criteria;

import com.ignis.vesta.domain.catalog.SafetyPillar;
import com.ignis.vesta.domain.ranking.CriterionScore;
import com.ignis.vesta.domain.ranking.RankingCriterion;
import com.ignis.vesta.domain.ranking.RankingInput;
import com.ignis.vesta.domain.vo.Provenance;

import java.util.Locale;

/** Direct contribution to a fire safety pillar (prevention, detection, suppression) versus fundamental physics. */
public final class SafetyPillarCriterion implements RankingCriterion {

    public static final String ID = "safetyPillar";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String name() {
        return "Safety pillar";
    }

    @Override
    public String description() {
        return "Prevention, detection or suppression = 1.0; fundamental flame physics = 0.3 (curated classification).";
    }

    @Override
    public double defaultWeight() {
        return 0.15;
    }

    @Override
    public Provenance provenance() {
        return Provenance.CURATED;
    }

    @Override
    public CriterionScore evaluate(RankingInput input) {
        SafetyPillar pillar = input.summary().classification().safetyPillar();
        String label = pillar.name().charAt(0) + pillar.name().substring(1).toLowerCase(Locale.ROOT);
        return pillar == SafetyPillar.FUNDAMENTAL
                ? new CriterionScore(0.3, "Fundamental combustion physics")
                : new CriterionScore(1.0, label + " pillar");
    }
}
