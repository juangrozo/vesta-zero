package com.ignis.vesta.domain.ranking.criteria;

import com.ignis.vesta.domain.catalog.InvestigationType;
import com.ignis.vesta.domain.model.InvestigationSummary;
import com.ignis.vesta.domain.ranking.CriterionScore;
import com.ignis.vesta.domain.ranking.RankingCriterion;
import com.ignis.vesta.domain.ranking.RankingInput;
import com.ignis.vesta.domain.vo.Provenance;

import java.util.Locale;

/** Relevance to crewed habitats from documented PSI fields: flight fire-safety work ranks above ground modeling. */
public final class HabitatRelevanceCriterion implements RankingCriterion {

    public static final String ID = "habitatRelevance";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String name() {
        return "Habitat relevance";
    }

    @Override
    public String description() {
        return "Flight investigation whose PSI sub-area mentions fire = 1.0; other flight investigation = 0.6; "
                + "ground modeling study = 0.3.";
    }

    @Override
    public double defaultWeight() {
        return 0.20;
    }

    @Override
    public Provenance provenance() {
        return Provenance.DOCUMENTED;
    }

    @Override
    public CriterionScore evaluate(RankingInput input) {
        InvestigationSummary s = input.summary();
        String subArea = s.subArea() == null ? "" : s.subArea();
        if (s.type() == InvestigationType.GROUND) {
            return new CriterionScore(0.3, "Ground study (" + s.projectType() + ")");
        }
        if (subArea.toLowerCase(Locale.ROOT).contains("fire")) {
            return new CriterionScore(1.0, "Flight investigation, sub-area \"" + subArea + "\"");
        }
        return new CriterionScore(0.6, "Flight investigation, sub-area \""
                + (subArea.isBlank() ? "not documented" : subArea) + "\"");
    }
}
