package com.ignis.vesta.domain.ranking;

import com.ignis.vesta.domain.catalog.CatalogEntry;
import com.ignis.vesta.domain.catalog.FuelState;
import com.ignis.vesta.domain.catalog.HardwareFamily;
import com.ignis.vesta.domain.catalog.SafetyPillar;
import com.ignis.vesta.domain.model.InvestigationSummary;
import com.ignis.vesta.domain.model.TestCondition;
import com.ignis.vesta.domain.ranking.criteria.AtmosphereProximityCriterion;
import com.ignis.vesta.domain.vo.Doi;
import com.ignis.vesta.domain.vo.NumericRange;
import com.ignis.vesta.domain.vo.PsiId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class RankingEngineTest {

    private final RankingEngine engine = new RankingEngine(DefaultRankingCriteria.forEnvironment(HabitatEnvironment.LUNAR_BASE_ROVER));

    // BASS-II-like: flight, fire safety, solid, prevention, O2 only at Earth level, 1 atm not documented
    private final RankingInput solidsAtEarthO2 = input("PSI-25", "Microgravity Investigation",
            "Spacecraft Fire Safety", FuelState.SOLID, SafetyPillar.PREVENTION, 129, 129,
            condition("PSI-25", 1, NumericRange.of(0.222), null));
    // CFI-G-like: flight, gas, fundamental, 85 % O2 at 0.49 atm
    private final RankingInput gasAtLunarAtmosphere = input("PSI-159", "Microgravity Investigation",
            "Flames of Gaseous Fuels", FuelState.GAS, SafetyPillar.FUNDAMENTAL, 333, 333,
            condition("PSI-159", 7, NumericRange.of(0.85), NumericRange.of(0.49)));
    // Ground study without tables
    private final RankingInput groundModel = input("PSI-62", "Ground Investigation", "Spacecraft Fire Safety",
            FuelState.SOLID, SafetyPillar.PREVENTION, 0, 0);

    @Test
    void scoreIsWeightedSumAndBreakdownAddsUp() {
        List<RankedInvestigation> ranked = engine.rank(List.of(solidsAtEarthO2, gasAtLunarAtmosphere, groundModel),
                RankingWeights.defaults(engine.criteria()));

        for (RankedInvestigation r : ranked) {
            double sum = r.breakdown().stream().mapToDouble(RankedInvestigation.Contribution::contribution).sum();
            assertThat(r.score()).isCloseTo(sum, within(1e-12));
            assertThat(r.breakdown()).hasSize(5).allSatisfy(c -> assertThat(c.reason()).isNotBlank());
        }
        assertThat(ranked).extracting(RankedInvestigation::position).containsExactly(1, 2, 3);
    }

    @Test
    void sameInputGivesSameOrder() {
        var weights = RankingWeights.defaults(engine.criteria());
        var first = engine.rank(List.of(groundModel, solidsAtEarthO2, gasAtLunarAtmosphere), weights);
        var second = engine.rank(List.of(gasAtLunarAtmosphere, groundModel, solidsAtEarthO2), weights);
        assertThat(first).extracting(r -> r.summary().psiId().value())
                .containsExactlyElementsOf(second.stream().map(r -> r.summary().psiId().value()).toList());
    }

    @Test
    void changingWeightsReordersResults() {
        var onlyAtmosphere = RankingWeights.of(engine.criteria(), Map.of(
                "atmosphere", 1.0, "habitatRelevance", 0.0, "materialType", 0.0,
                "safetyPillar", 0.0, "structuredData", 0.0));
        var onlyMaterial = RankingWeights.of(engine.criteria(), Map.of(
                "atmosphere", 0.0, "habitatRelevance", 0.0, "materialType", 1.0,
                "safetyPillar", 0.0, "structuredData", 0.0));

        assertThat(engine.rank(List.of(solidsAtEarthO2, gasAtLunarAtmosphere), onlyAtmosphere).getFirst()
                .summary().psiId().value()).isEqualTo("PSI-159");
        assertThat(engine.rank(List.of(solidsAtEarthO2, gasAtLunarAtmosphere), onlyMaterial).getFirst()
                .summary().psiId().value()).isEqualTo("PSI-25");
    }

    @Test
    void lunarProximityUsesBestTestAndExplainsIt() {
        CriterionScore score = new AtmosphereProximityCriterion(HabitatEnvironment.LUNAR_BASE_ROVER).evaluate(gasAtLunarAtmosphere);
        assertThat(score.score()).isEqualTo(1.0);
        assertThat(score.reason()).contains("85.0 % O2").contains("0.490 atm").contains("row 7");
    }

    @Test
    void lunarProximityWarnsWhenRowSummarizesRangesOfSeveralTests() {
        // FLEX-2-like summary row: O2 0.17..0.30 and pressure 0.5..3.0 atm in the same row
        RankingInput summaryRow = input("PSI-68", "Microgravity Investigation", "Spacecraft Fire Safety",
                FuelState.LIQUID_DROPLET, SafetyPillar.FUNDAMENTAL, 8, 8,
                condition("PSI-68", 1, new NumericRange(0.17, 0.30), new NumericRange(0.5, 3.0)));
        CriterionScore score = new AtmosphereProximityCriterion(HabitatEnvironment.LUNAR_BASE_ROVER).evaluate(summaryRow);
        assertThat(score.score()).isEqualTo(1.0);
        assertThat(score.reason()).contains("may not have been tested together");
    }

    @Test
    void lunarProximityWithoutConditionsIsZeroAndSaysSo() {
        CriterionScore score = new AtmosphereProximityCriterion(HabitatEnvironment.LUNAR_BASE_ROVER).evaluate(groundModel);
        assertThat(score.score()).isZero();
        assertThat(score.reason()).contains("No O2 or pressure documented");
    }

    @Test
    void lunarProximityCountsMissingPressureAsZero() {
        // 0.222 O2 -> (0.222 - 0.21) / 0.09 = 0.1333; pressure missing -> 0; mean = 0.0667
        CriterionScore score = new AtmosphereProximityCriterion(HabitatEnvironment.LUNAR_BASE_ROVER).evaluate(solidsAtEarthO2);
        assertThat(score.score()).isCloseTo(0.0667, within(1e-3));
    }

    @Test
    void spacecraftPresetRewardsEarthLikeTestsOverOxygenRichOnes() {
        var spacecraft = new AtmosphereProximityCriterion(HabitatEnvironment.SPACECRAFT_STATION);
        RankingInput airAtOneAtm = input("PSI-102", "Microgravity Investigation", "Spacecraft Fire Safety",
                FuelState.SOLID, SafetyPillar.DETECTION, 1, 1,
                condition("PSI-102", 1, NumericRange.of(0.21), NumericRange.of(1.0)));
        assertThat(spacecraft.evaluate(airAtOneAtm).score()).isCloseTo(1.0, within(1e-3));
        // 85 % O2 is far above 21 % -> 0; 0.49 atm is 0.51 atm below 1 atm -> 0; mean 0
        assertThat(spacecraft.evaluate(gasAtLunarAtmosphere).score()).isZero();
        assertThat(spacecraft.description()).contains("21 % O2 at 14.7 psi");
    }

    @Test
    void aroundTargetScoresOneWhenTheTestedRangeContainsIt() {
        assertThat(AtmosphereProximityCriterion.component(new NumericRange(0.17, 0.30), 0.21, TargetBound.AROUND,
                0.09)).isEqualTo(1.0);
        assertThat(AtmosphereProximityCriterion.component(NumericRange.of(0.255), 0.21, TargetBound.AROUND, 0.09))
                .isCloseTo(0.5, within(1e-9));
    }

    @Test
    void spacesuitPresetNeedsNearlyPureOxygen() {
        var suit = new AtmosphereProximityCriterion(HabitatEnvironment.SPACESUIT);
        // 85 % O2 vs >= 100 %: 15 points short of a 9-point tolerance -> 0; 0.49 atm vs <= 4.3 psi (0.293 atm):
        // 0.197 atm over a 0.306 atm tolerance -> 0.356; mean 0.178
        assertThat(suit.evaluate(gasAtLunarAtmosphere).score()).isCloseTo(0.178, within(1e-3));
    }

    @Test
    void marsPresetIsMarkedAsAnAssumption() {
        assertThat(HabitatEnvironment.MARS_BASE.assumption()).isTrue();
        assertThat(HabitatEnvironment.MARS_BASE.basis()).contains("no Martian habitat atmosphere");
        assertThat(HabitatEnvironment.LUNAR_BASE_ROVER.assumption()).isFalse();
        assertThat(HabitatEnvironment.parse(null)).isEqualTo(HabitatEnvironment.LUNAR_BASE_ROVER);
        assertThat(HabitatEnvironment.parse("spacesuit")).isEqualTo(HabitatEnvironment.SPACESUIT);
        assertThatThrownBy(() -> HabitatEnvironment.parse("venus"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unknown environment");
    }

    @Test
    void weightsAreNormalizedAndValidated() {
        var weights = RankingWeights.of(engine.criteria(), Map.of("atmosphere", 3.0));
        double total = weights.asMap().values().stream().mapToDouble(Double::doubleValue).sum();
        assertThat(total).isCloseTo(1.0, within(1e-12));

        assertThatThrownBy(() -> RankingWeights.of(engine.criteria(), Map.of("magic", 1.0)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unknown criterion");
        assertThatThrownBy(() -> RankingWeights.of(engine.criteria(), Map.of("materialType", -1.0)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RankingWeights.of(engine.criteria(), Map.of(
                "atmosphere", 0.0, "habitatRelevance", 0.0, "materialType", 0.0,
                "safetyPillar", 0.0, "structuredData", 0.0)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("greater than zero");
    }

    private static RankingInput input(String id, String projectType, String subArea, FuelState fuel,
                                      SafetyPillar pillar, long rows, long normalized, TestCondition... conditions) {
        PsiId psiId = PsiId.of(id);
        var summary = new InvestigationSummary(psiId, id, "Title", new Doi("10.60555/x"), projectType, subArea, null,
                new CatalogEntry(psiId, HardwareFamily.MSG, fuel, pillar), rows > 0 ? 1 : 0, rows, normalized);
        return new RankingInput(summary, List.of(conditions));
    }

    private static TestCondition condition(String id, int row, NumericRange o2, NumericRange pressure) {
        return new TestCondition(PsiId.of(id), "t.csv", row, "T" + row, null, o2, pressure, null, Map.of());
    }
}
