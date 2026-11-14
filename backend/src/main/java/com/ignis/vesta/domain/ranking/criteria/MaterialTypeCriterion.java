package com.ignis.vesta.domain.ranking.criteria;

import com.ignis.vesta.domain.catalog.FuelState;
import com.ignis.vesta.domain.ranking.CriterionScore;
import com.ignis.vesta.domain.ranking.RankingCriterion;
import com.ignis.vesta.domain.ranking.RankingInput;
import com.ignis.vesta.domain.vo.Provenance;

/** Solids such as fabrics and plastics are what burns in a habitat, so they weigh most. */
public final class MaterialTypeCriterion implements RankingCriterion {

    public static final String ID = "materialType";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String name() {
        return "Material type";
    }

    @Override
    public String description() {
        return "Solids (fabrics, plastics) = 1.0; liquid droplets = 0.5; gases = 0.4; dust or aerosol = 0.3 "
                + "(curated classification).";
    }

    @Override
    public double defaultWeight() {
        return 0.20;
    }

    @Override
    public Provenance provenance() {
        return Provenance.CURATED;
    }

    @Override
    public CriterionScore evaluate(RankingInput input) {
        FuelState fuel = input.summary().classification().fuelState();
        return switch (fuel) {
            case SOLID -> new CriterionScore(1.0, "Solid fuels or materials");
            case LIQUID_DROPLET -> new CriterionScore(0.5, "Liquid fuel droplets");
            case GAS -> new CriterionScore(0.4, "Gaseous fuels");
            case AEROSOL -> new CriterionScore(0.3, "Dust or aerosol samples");
        };
    }
}
