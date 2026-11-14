package com.ignis.vesta.entrypoint.http;

import com.ignis.vesta.application.ListInvestigationsUseCase;
import com.ignis.vesta.domain.catalog.CatalogEntry;
import com.ignis.vesta.domain.catalog.FuelState;
import com.ignis.vesta.domain.catalog.HardwareFamily;
import com.ignis.vesta.domain.catalog.SafetyPillar;
import com.ignis.vesta.domain.model.InvestigationSummary;
import com.ignis.vesta.domain.vo.Doi;
import com.ignis.vesta.domain.vo.PsiId;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

/** HTTP contract for F1 against an in-memory catalog reader (no database). */
class InvestigationHandlerTest {

    private final WebTestClient client = WebTestClient.bindToRouterFunction(
            new ApiRouter().apiRoutes(new HealthHandler(), new InvestigationHandler(
                    new ListInvestigationsUseCase(() -> Flux.just(
                            summary("PSI-100", "Microgravity Investigation", HardwareFamily.CYGNUS, FuelState.SOLID,
                                    SafetyPillar.PREVENTION, 1),
                            summary("PSI-60", "Ground Investigation", HardwareFamily.GROUND, FuelState.GAS,
                                    SafetyPillar.FUNDAMENTAL, 0),
                            summary("PSI-25", "Microgravity Investigation", HardwareFamily.MSG, FuelState.SOLID,
                                    SafetyPillar.PREVENTION, 1))))))
            .build();

    @Test
    void listsAllInNaturalIdOrderWithDoiUrl() {
        client.get().uri("/api/investigations").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[0].psiId").isEqualTo("PSI-25")
                .jsonPath("$[1].psiId").isEqualTo("PSI-60")
                .jsonPath("$[2].psiId").isEqualTo("PSI-100")
                .jsonPath("$[0].doiUrl").isEqualTo("https://doi.org/10.60555/psi-25")
                .jsonPath("$[0].classification.provenance").isEqualTo("CURATED")
                .jsonPath("$[1].type").isEqualTo("GROUND")
                .jsonPath("$[1].experimentalData.available").isEqualTo(false);
    }

    @Test
    void filtersAreCaseInsensitiveAndCombined() {
        client.get().uri("/api/investigations?fuelState=solid&hardware=MSG").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].psiId").isEqualTo("PSI-25");
    }

    @Test
    void invalidFilterValueIsBadRequest() {
        client.get().uri("/api/investigations?pillar=magic").exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").value(org.hamcrest.Matchers.containsString("pillar"));
    }

    private static InvestigationSummary summary(String id, String projectType, HardwareFamily hardware,
                                                FuelState fuel, SafetyPillar pillar, int tables) {
        PsiId psiId = PsiId.of(id);
        return new InvestigationSummary(psiId, id, "Title " + id, new Doi("10.60555/" + id.toLowerCase()),
                projectType, null, null, new CatalogEntry(psiId, hardware, fuel, pillar), tables,
                tables * 10L, tables * 5L);
    }
}
