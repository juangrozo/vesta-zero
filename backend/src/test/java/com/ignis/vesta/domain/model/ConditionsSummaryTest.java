package com.ignis.vesta.domain.model;

import com.ignis.vesta.domain.vo.NumericRange;
import com.ignis.vesta.domain.vo.PsiId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConditionsSummaryTest {

    private static final PsiId ID = PsiId.of("PSI-159");

    @Test
    void envelopeSpansAllDocumentedRowsAndIgnoresMissingValues() {
        ConditionsSummary summary = ConditionsSummary.of(List.of(
                row(1, "ethane", new NumericRange(0.21, 0.40), NumericRange.of(1.0)),
                row(2, "Ethane", NumericRange.of(0.85), null),
                row(3, "propane", null, NumericRange.of(0.49)),
                row(4, null, null, null)));

        assertThat(summary.testRows()).isEqualTo(4);
        assertThat(summary.normalizedRows()).isEqualTo(3);
        assertThat(summary.o2Fraction()).contains(new NumericRange(0.21, 0.85));
        assertThat(summary.pressureAtm()).contains(new NumericRange(0.49, 1.0));
        assertThat(summary.flowCmPerS()).isEmpty();
        assertThat(summary.fuelsOrMaterials()).containsExactly("ethane", "propane");
    }

    @Test
    void noRowsMeansNothingDocumented() {
        ConditionsSummary summary = ConditionsSummary.of(List.of());
        assertThat(summary.testRows()).isZero();
        assertThat(summary.o2Fraction()).isEmpty();
        assertThat(summary.sourceFiles()).isEmpty();
    }

    private static TestCondition row(int index, String fuel, NumericRange o2, NumericRange pressure) {
        return new TestCondition(ID, "table.csv", index, null, fuel, o2, pressure, null, Map.of());
    }
}
