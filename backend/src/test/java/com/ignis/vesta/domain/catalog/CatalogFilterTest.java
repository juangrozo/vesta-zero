package com.ignis.vesta.domain.catalog;

import com.ignis.vesta.domain.model.InvestigationSummary;
import com.ignis.vesta.domain.vo.Doi;
import com.ignis.vesta.domain.vo.PsiId;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogFilterTest {

    private final InvestigationSummary bassII = summary("PSI-25", "Microgravity Investigation",
            HardwareFamily.MSG, FuelState.SOLID, SafetyPillar.PREVENTION);
    private final InvestigationSummary spiceModel = summary("PSI-60", "Ground Investigation",
            HardwareFamily.GROUND, FuelState.GAS, SafetyPillar.FUNDAMENTAL);

    @Test
    void emptyFilterMatchesEverything() {
        assertThat(CatalogFilter.NONE.matches(bassII)).isTrue();
        assertThat(CatalogFilter.NONE.matches(spiceModel)).isTrue();
    }

    @Test
    void typeIsDerivedFromDocumentedProjectType() {
        CatalogFilter ground = new CatalogFilter(Optional.of(InvestigationType.GROUND),
                Optional.empty(), Optional.empty(), Optional.empty());
        assertThat(ground.matches(spiceModel)).isTrue();
        assertThat(ground.matches(bassII)).isFalse();
    }

    @Test
    void criteriaCombineWithAnd() {
        CatalogFilter solidPrevention = new CatalogFilter(Optional.empty(), Optional.empty(),
                Optional.of(FuelState.SOLID), Optional.of(SafetyPillar.PREVENTION));
        CatalogFilter solidDetection = new CatalogFilter(Optional.empty(), Optional.empty(),
                Optional.of(FuelState.SOLID), Optional.of(SafetyPillar.DETECTION));
        assertThat(solidPrevention.matches(bassII)).isTrue();
        assertThat(solidDetection.matches(bassII)).isFalse();
    }

    static InvestigationSummary summary(String id, String projectType, HardwareFamily hardware, FuelState fuel,
                                        SafetyPillar pillar) {
        PsiId psiId = PsiId.of(id);
        return new InvestigationSummary(psiId, id, "Title " + id, new Doi("10.60555/" + id.toLowerCase()),
                projectType, null, null, new CatalogEntry(psiId, hardware, fuel, pillar), 1, 10, 5);
    }
}
