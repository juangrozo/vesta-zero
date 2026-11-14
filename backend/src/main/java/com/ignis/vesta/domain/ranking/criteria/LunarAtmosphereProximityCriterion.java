package com.ignis.vesta.domain.ranking.criteria;

import com.ignis.vesta.domain.model.TestCondition;
import com.ignis.vesta.domain.ranking.CriterionScore;
import com.ignis.vesta.domain.ranking.RankingCriterion;
import com.ignis.vesta.domain.ranking.RankingInput;
import com.ignis.vesta.domain.vo.NumericRange;
import com.ignis.vesta.domain.vo.Provenance;

import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;

import static com.ignis.vesta.domain.ranking.LunarHabitatTarget.EARTH_O2_FRACTION;
import static com.ignis.vesta.domain.ranking.LunarHabitatTarget.EARTH_PRESSURE_ATM;
import static com.ignis.vesta.domain.ranking.LunarHabitatTarget.O2_FRACTION;
import static com.ignis.vesta.domain.ranking.LunarHabitatTarget.PRESSURE_ATM;
import static com.ignis.vesta.domain.ranking.LunarHabitatTarget.PSI_PER_ATM;

/**
 * How close any documented test came to a lunar habitat atmosphere. Per test, the O2 component goes from 0 at
 * Earth's 21 % to 1 at 30 % (using the highest O2 in the row) and the pressure component from 0 at 1 atm to 1 at
 * 10.2 psi (using the lowest pressure); the test score is their mean and a missing component counts 0.
 * The investigation keeps its best test.
 */
public final class LunarAtmosphereProximityCriterion implements RankingCriterion {

    public static final String ID = "lunarAtmosphere";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String name() {
        return "Lunar atmosphere proximity";
    }

    @Override
    public String description() {
        return "Best documented test: mean of an O2 score (21 % O2 = 0, 30 % or more = 1) and a pressure score "
                + "(1 atm = 0, 10.2 psi or less = 1). A component that is not documented counts 0.";
    }

    @Override
    public double defaultWeight() {
        return 0.30;
    }

    @Override
    public Provenance provenance() {
        return Provenance.DOCUMENTED;
    }

    @Override
    public CriterionScore evaluate(RankingInput input) {
        Optional<Scored> best = input.conditions().stream()
                .filter(TestCondition::hasNormalizedConditions)
                .map(Scored::of)
                .filter(s -> s.o2 != null || s.pressure != null)
                .max(Comparator.comparingDouble(Scored::score)
                        .thenComparing(s -> s.condition.rowIndex(), Comparator.reverseOrder()));
        if (best.isEmpty()) {
            return new CriterionScore(0.0, "No O2 or pressure documented in experimental tables");
        }
        Scored s = best.get();
        return new CriterionScore(s.score(), "Best test: " + s.describe());
    }

    private record Scored(TestCondition condition, Double o2, Double pressure) {

        static Scored of(TestCondition c) {
            return new Scored(c, c.o2().map(NumericRange::max).orElse(null),
                    c.pressure().map(NumericRange::min).orElse(null));
        }

        double score() {
            double o2Score = o2 == null ? 0.0
                    : CriterionScore.clamp((o2 - EARTH_O2_FRACTION) / (O2_FRACTION - EARTH_O2_FRACTION));
            double pressureScore = pressure == null ? 0.0
                    : CriterionScore.clamp((EARTH_PRESSURE_ATM - pressure) / (EARTH_PRESSURE_ATM - PRESSURE_ATM));
            return (o2Score + pressureScore) / 2.0;
        }

        String describe() {
            String o2Text = o2 == null ? "O2 not documented"
                    : String.format(Locale.ROOT, "%.1f %% O2", o2 * 100);
            String pText = pressure == null ? "pressure not documented"
                    : String.format(Locale.ROOT, "%.3f atm (%.1f psi)", pressure, pressure * PSI_PER_ATM);
            String label = condition.testLabel() == null ? "" : " " + condition.testLabel();
            String text = o2Text + " at " + pText + " (test" + label + ", row " + condition.rowIndex() + ")";
            return documentsRanges() ? text + "; this row documents ranges across several tests, so this "
                    + "O2/pressure pair may not have been tested together" : text;
        }

        private boolean documentsRanges() {
            return condition.o2().map(r -> r.min() != r.max()).orElse(false)
                    && condition.pressure().map(r -> r.min() != r.max()).orElse(false);
        }
    }
}
