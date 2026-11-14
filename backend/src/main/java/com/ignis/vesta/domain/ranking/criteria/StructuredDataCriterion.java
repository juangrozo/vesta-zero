package com.ignis.vesta.domain.ranking.criteria;

import com.ignis.vesta.domain.model.InvestigationSummary;
import com.ignis.vesta.domain.ranking.CriterionScore;
import com.ignis.vesta.domain.ranking.RankingCriterion;
import com.ignis.vesta.domain.ranking.RankingInput;
import com.ignis.vesta.domain.vo.Provenance;

/** Availability of structured test data that supports predictive flammability modeling. */
public final class StructuredDataCriterion implements RankingCriterion {

    public static final String ID = "structuredData";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String name() {
        return "Structured data";
    }

    @Override
    public String description() {
        return "Experimental table with normalized O2, pressure or flow = 1.0; table without those conditions = 0.5; "
                + "no experimental table = 0.";
    }

    @Override
    public double defaultWeight() {
        return 0.15;
    }

    @Override
    public Provenance provenance() {
        return Provenance.DOCUMENTED;
    }

    @Override
    public CriterionScore evaluate(RankingInput input) {
        InvestigationSummary s = input.summary();
        if (s.normalizedTestCount() > 0) {
            return new CriterionScore(1.0, s.normalizedTestCount() + " of " + s.testCount()
                    + " test rows with normalized conditions");
        }
        if (s.hasExperimentalTable()) {
            return new CriterionScore(0.5, s.testCount() + " test rows, none with O2, pressure or flow");
        }
        return new CriterionScore(0.0, "No experimental table in PSI");
    }
}
