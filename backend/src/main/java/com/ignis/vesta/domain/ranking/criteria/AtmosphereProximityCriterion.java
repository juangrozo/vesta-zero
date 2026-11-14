package com.ignis.vesta.domain.ranking.criteria;

import com.ignis.vesta.domain.model.TestCondition;
import com.ignis.vesta.domain.ranking.CriterionScore;
import com.ignis.vesta.domain.ranking.HabitatEnvironment;
import com.ignis.vesta.domain.ranking.RankingCriterion;
import com.ignis.vesta.domain.ranking.RankingInput;
import com.ignis.vesta.domain.ranking.TargetBound;
import com.ignis.vesta.domain.vo.NumericRange;
import com.ignis.vesta.domain.vo.Provenance;

import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;

/**
 * How close any documented test came to the atmosphere of the selected crew environment. Per test, an O2 and a
 * pressure component each score 1 when the test meets the target and fall linearly to 0 at a fixed tolerance:
 * 9 O2 percentage points (Earth's 21 % to the lunar 30 %) and 0.306 atm (Earth's 14.7 psi to the lunar 10.2 psi).
 * The test score is their mean; a missing component counts 0. The investigation keeps its best test.
 * With the lunar preset this is exactly the original lunar proximity score.
 */
public final class AtmosphereProximityCriterion implements RankingCriterion {

    public static final String ID = "atmosphere";

    static final double O2_TOLERANCE = 0.09;
    static final double PRESSURE_TOLERANCE_ATM = 1.0 - 10.2 / HabitatEnvironment.PSI_PER_ATM;

    private final HabitatEnvironment environment;

    public AtmosphereProximityCriterion(HabitatEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String name() {
        return "Atmosphere proximity";
    }

    @Override
    public String description() {
        return "Best documented test compared with the " + environment.displayName().toLowerCase(Locale.ROOT)
                + " target (" + targetText() + "): mean of an O2 score and a pressure score, each 1 when the test "
                + "meets the target and 0 at 9 O2 points or 4.5 psi away. A component that is not documented "
                + "counts 0.";
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
                .filter(c -> c.o2().isPresent() || c.pressure().isPresent())
                .map(this::score)
                .max(Comparator.comparingDouble(Scored::score)
                        .thenComparing(s -> s.condition().rowIndex(), Comparator.reverseOrder()));
        if (best.isEmpty()) {
            return new CriterionScore(0.0, "No O2 or pressure documented in experimental tables");
        }
        return new CriterionScore(best.get().score(), "Best test: " + describe(best.get().condition()));
    }

    private record Scored(TestCondition condition, double score) {
    }

    private Scored score(TestCondition c) {
        double o2 = c.o2().map(r -> component(r, environment.o2Fraction(), environment.o2Bound(), O2_TOLERANCE))
                .orElse(0.0);
        double pressure = c.pressure().map(r -> component(r, environment.pressureAtm(), environment.pressureBound(),
                PRESSURE_TOLERANCE_ATM)).orElse(0.0);
        return new Scored(c, (o2 + pressure) / 2.0);
    }

    /** Score in [0, 1] of one tested range against one target value. */
    public static double component(NumericRange tested, double target, TargetBound bound, double tolerance) {
        double distance = switch (bound) {
            case AT_LEAST -> Math.max(0.0, target - tested.max());
            case AT_MOST -> Math.max(0.0, tested.min() - target);
            case AROUND -> target < tested.min() ? tested.min() - target
                    : target > tested.max() ? target - tested.max() : 0.0;
        };
        return CriterionScore.clamp(1.0 - distance / tolerance);
    }

    private String targetText() {
        return bound(environment.o2Bound()) + String.format(Locale.ROOT, "%.0f %% O2", environment.o2Fraction() * 100)
                + " at " + bound(environment.pressureBound())
                + String.format(Locale.ROOT, "%.1f psi", environment.pressurePsi());
    }

    private static String bound(TargetBound bound) {
        return switch (bound) {
            case AT_LEAST -> "≥ ";
            case AT_MOST -> "≤ ";
            case AROUND -> "";
        };
    }

    private static String describe(TestCondition c) {
        String o2 = c.o2().map(r -> range(r.min() * 100, r.max() * 100, "%.1f") + " % O2").orElse("O2 not documented");
        String pressure = c.pressure()
                .map(r -> range(r.min(), r.max(), "%.3f") + " atm ("
                        + range(r.min() * HabitatEnvironment.PSI_PER_ATM, r.max() * HabitatEnvironment.PSI_PER_ATM,
                        "%.1f") + " psi)")
                .orElse("pressure not documented");
        String label = c.testLabel() == null ? "" : " " + c.testLabel();
        String text = o2 + " at " + pressure + " (test" + label + ", row " + c.rowIndex() + ")";
        boolean ranges = c.o2().map(r -> r.min() != r.max()).orElse(false)
                && c.pressure().map(r -> r.min() != r.max()).orElse(false);
        return ranges ? text + "; this row documents ranges across several tests, so this O2/pressure pair may "
                + "not have been tested together" : text;
    }

    private static String range(double min, double max, String format) {
        String a = String.format(Locale.ROOT, format, min);
        String b = String.format(Locale.ROOT, format, max);
        return a.equals(b) ? a : a + "–" + b;
    }
}
