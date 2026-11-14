package com.ignis.vesta.domain.model;

import com.ignis.vesta.domain.catalog.CatalogEntry;
import com.ignis.vesta.domain.catalog.FuelState;
import com.ignis.vesta.domain.catalog.HardwareFamily;
import com.ignis.vesta.domain.catalog.SafetyPillar;
import com.ignis.vesta.domain.vo.Doi;
import com.ignis.vesta.domain.vo.NumericRange;
import com.ignis.vesta.domain.vo.PsiId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvestigationComparisonTest {

    // FLEX-like and FLEX-2-like: same hardware and fuel state, different pillar, overlapping O2 and pressure
    private final InvestigationDetail flex = detail("PSI-69", SafetyPillar.SUPPRESSION, List.of(
            row("PSI-69", "Methanol", new NumericRange(0.12, 0.34), new NumericRange(0.69, 3.05)),
            row("PSI-69", "Heptane", NumericRange.of(0.21), NumericRange.of(1.0))));
    private final InvestigationDetail flex2 = detail("PSI-68", SafetyPillar.FUNDAMENTAL, List.of(
            row("PSI-68", "heptane", new NumericRange(0.17, 0.30), new NumericRange(0.5, 3.0))));

    @Test
    void reportsSharedAndDifferentAspects() {
        var facts = factsByDimension(InvestigationComparison.of(List.of(flex, flex2)));

        assertThat(facts.get("Hardware family (curated)").same()).isTrue();
        assertThat(facts.get("Safety pillar (curated)").same()).isFalse();
        assertThat(facts.get("Safety pillar (curated)").detail()).contains("FUNDAMENTAL").contains("SUPPRESSION");
        assertThat(facts.get("Oxygen range").detail()).isEqualTo("All tested within 17 to 30 % O2");
        assertThat(facts.get("Pressure range").detail()).isEqualTo("All tested within 0.69 to 3 atm");
        assertThat(facts.get("Flow velocity range").same()).isFalse();
        assertThat(facts.get("Fuels or materials in common").detail()).isEqualTo("heptane");
    }

    @Test
    void nonOverlappingRangesAreReported() {
        InvestigationDetail lowO2 = detail("PSI-25", SafetyPillar.PREVENTION, List.of(
                row("PSI-25", "PMMA", new NumericRange(0.13, 0.22), null)));
        InvestigationDetail highO2 = detail("PSI-159", SafetyPillar.FUNDAMENTAL, List.of(
                row("PSI-159", "ethane", new NumericRange(0.4, 0.85), null)));

        var facts = factsByDimension(InvestigationComparison.of(List.of(lowO2, highO2)));

        assertThat(facts.get("Oxygen range").detail()).isEqualTo("Tested ranges do not overlap");
        assertThat(facts.get("Pressure range").detail()).isEqualTo("Not documented for any of them");
    }

    @Test
    void onlyTwoOrThreeInvestigationsCanBeCompared() {
        assertThatThrownBy(() -> InvestigationComparison.of(List.of(flex)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InvestigationComparison.of(List.of(flex, flex2, flex, flex2)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Map<String, InvestigationComparison.Fact> factsByDimension(InvestigationComparison c) {
        return c.facts().stream().collect(java.util.stream.Collectors.toMap(
                InvestigationComparison.Fact::dimension, f -> f));
    }

    private static InvestigationDetail detail(String id, SafetyPillar pillar, List<TestCondition> rows) {
        PsiId psiId = PsiId.of(id);
        Investigation inv = new Investigation(psiId, id, "Title", new Doi("10.60555/x"), "Microgravity Investigation",
                null, null, null, null, null, null, null, null, null, null, null, List.of(), List.of(),
                new CatalogEntry(psiId, HardwareFamily.CIR_MDCA, FuelState.LIQUID_DROPLET, pillar), "{}");
        return new InvestigationDetail(inv, ConditionsSummary.of(rows), List.of());
    }

    private static TestCondition row(String id, String fuel, NumericRange o2, NumericRange pressure) {
        return new TestCondition(PsiId.of(id), "t.csv", 1, null, fuel, o2, pressure, null, Map.of());
    }
}
