package com.ignis.vesta.domain.export;

import com.ignis.vesta.domain.model.TestCondition;
import com.ignis.vesta.domain.vo.NumericRange;
import com.ignis.vesta.domain.vo.PsiId;

import java.util.Optional;
import java.util.Set;

/**
 * Selection of experimental-table rows for export. All parts are optional and combined with AND.
 * <ul>
 *   <li>{@code psiIds}: empty means every investigation.</li>
 *   <li>{@code minO2Fraction}: keeps rows whose documented O2 reaches the value (highest O2 in the row).</li>
 *   <li>{@code maxPressureAtm}: keeps rows whose documented pressure goes down to the value (lowest pressure).</li>
 * </ul>
 * A row without the value a filter needs is left out: nothing is assumed about undocumented conditions.
 */
public record TestConditionQuery(Set<PsiId> psiIds, Optional<Double> minO2Fraction, Optional<Double> maxPressureAtm) {

    public static final TestConditionQuery ALL = new TestConditionQuery(Set.of(), Optional.empty(), Optional.empty());

    public TestConditionQuery {
        psiIds = Set.copyOf(psiIds);
        minO2Fraction.ifPresent(v -> {
            if (v < 0 || v > 1) {
                throw new IllegalArgumentException("Minimum O2 must be between 0 and 100 %");
            }
        });
        maxPressureAtm.ifPresent(v -> {
            if (v <= 0) {
                throw new IllegalArgumentException("Maximum pressure must be greater than zero");
            }
        });
    }

    public boolean includes(PsiId psiId) {
        return psiIds.isEmpty() || psiIds.contains(psiId);
    }

    public boolean matches(TestCondition row) {
        if (minO2Fraction.isPresent()
                && row.o2().map(NumericRange::max).filter(o2 -> o2 >= minO2Fraction.get() - 1e-9).isEmpty()) {
            return false;
        }
        return maxPressureAtm.isEmpty()
                || row.pressure().map(NumericRange::min).filter(p -> p <= maxPressureAtm.get() + 1e-9).isPresent();
    }
}
