package com.ignis.vesta.domain.assistant;

import com.ignis.vesta.domain.catalog.CatalogEntry;
import com.ignis.vesta.domain.catalog.FuelState;
import com.ignis.vesta.domain.catalog.HardwareFamily;
import com.ignis.vesta.domain.catalog.SafetyPillar;
import com.ignis.vesta.domain.model.ConditionsSummary;
import com.ignis.vesta.domain.model.Investigation;
import com.ignis.vesta.domain.model.InvestigationDetail;
import com.ignis.vesta.domain.model.TestCondition;
import com.ignis.vesta.domain.vo.Doi;
import com.ignis.vesta.domain.vo.NumericRange;
import com.ignis.vesta.domain.vo.PsiId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CorpusRendererTest {

    @Test
    void renderingIsDeterministicAndOrderedByPsiNumber() {
        InvestigationDetail flex = detail("PSI-69", "FLEX", List.of(
                condition("PSI-69", NumericRange.of(0.21), NumericRange.of(757.7 / 760.0))));
        InvestigationDetail spice = detail("PSI-107", "SPICE", List.of());

        EvidenceCorpus a = CorpusRenderer.render(List.of(spice, flex));
        EvidenceCorpus b = CorpusRenderer.render(List.of(flex, spice));

        assertThat(a.text()).isEqualTo(b.text());
        assertThat(a.fingerprint()).isEqualTo(b.fingerprint()).hasSize(64);
        assertThat(a.text().indexOf("PSI-69")).isLessThan(a.text().indexOf("PSI-107"));
        assertThat(a.psiIds()).containsExactlyInAnyOrder(PsiId.of("PSI-69"), PsiId.of("PSI-107"));
    }

    @Test
    void rendersDocumentedRangesAndMarksMissingData() {
        EvidenceCorpus corpus = CorpusRenderer.render(List.of(
                detail("PSI-69", "FLEX", List.of(condition("PSI-69", new NumericRange(0.12, 0.34), null))),
                detail("PSI-60", null, List.of())));

        assertThat(corpus.text())
                .contains("Documented O2 range: 12 to 34 % O2")
                .contains("Documented pressure range: not documented")
                .contains("Experimental table: none documented in PSI")
                .contains("Curated classification (Vesta Zero, not PSI)")
                .contains("doi=\"10.60555/psi-69\"");
    }

    private static InvestigationDetail detail(String id, String acronym, List<TestCondition> rows) {
        PsiId psiId = PsiId.of(id);
        Investigation inv = new Investigation(psiId, acronym, "Title " + id, new Doi("10.60555/" + id.toLowerCase()),
                "Microgravity Investigation", "Combustion Science", "Spacecraft Fire Safety", "ISS", "CIR",
                "objective", "approach", null, "impacts", null, null, "CC0-1.0", List.of(), List.of(),
                new CatalogEntry(psiId, HardwareFamily.CIR_MDCA, FuelState.LIQUID_DROPLET, SafetyPillar.SUPPRESSION),
                "{}");
        return new InvestigationDetail(inv, ConditionsSummary.of(rows), List.of());
    }

    private static TestCondition condition(String id, NumericRange o2, NumericRange pressure) {
        return new TestCondition(PsiId.of(id), "t.csv", 1, "1", "Methanol", o2, pressure, null, Map.of());
    }
}
