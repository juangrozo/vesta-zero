package com.ignis.vesta.entrypoint.http;

import com.ignis.vesta.application.AskAssistantUseCase;
import com.ignis.vesta.application.GetInvestigationUseCase;
import com.ignis.vesta.application.ListInvestigationsUseCase;
import com.ignis.vesta.application.RankInvestigationsUseCase;
import com.ignis.vesta.domain.catalog.CatalogEntry;
import com.ignis.vesta.domain.catalog.FuelState;
import com.ignis.vesta.domain.catalog.HardwareFamily;
import com.ignis.vesta.domain.catalog.SafetyPillar;
import com.ignis.vesta.domain.model.IngestionWarning;
import com.ignis.vesta.domain.model.Investigation;
import com.ignis.vesta.domain.model.InvestigationSummary;
import com.ignis.vesta.domain.model.Publication;
import com.ignis.vesta.domain.model.TestCondition;
import com.ignis.vesta.domain.port.InvestigationCatalogReader;
import com.ignis.vesta.domain.port.InvestigationDetailReader;
import com.ignis.vesta.domain.ranking.DefaultRankingCriteria;
import com.ignis.vesta.domain.ranking.RankingEngine;
import com.ignis.vesta.domain.vo.Doi;
import com.ignis.vesta.domain.vo.NumericRange;
import com.ignis.vesta.domain.vo.PsiId;
import com.ignis.vesta.infrastructure.llm.UnconfiguredLlmGateway;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** HTTP contract for F1 and F2 against in-memory readers (no database). */
class InvestigationHandlerTest {

    private static final PsiId BASS_II = PsiId.of("PSI-25");

    private final WebTestClient client = WebTestClient.bindToRouterFunction(
            new ApiRouter().apiRoutes(new HealthHandler(), new InvestigationHandler(
                    new ListInvestigationsUseCase(catalog(Flux.just(
                            summary("PSI-100", "Microgravity Investigation", HardwareFamily.CYGNUS, FuelState.SOLID,
                                    SafetyPillar.PREVENTION, 1),
                            summary("PSI-60", "Ground Investigation", HardwareFamily.GROUND, FuelState.GAS,
                                    SafetyPillar.FUNDAMENTAL, 0),
                            summary("PSI-25", "Microgravity Investigation", HardwareFamily.MSG, FuelState.SOLID,
                                    SafetyPillar.PREVENTION, 1)))),
                    new GetInvestigationUseCase(new FakeDetailReader())),
                    new RankingHandler(new RankInvestigationsUseCase(catalog(Flux.empty()),
                            new RankingEngine(DefaultRankingCriteria.all()))),
                    new AssistantHandler(new AskAssistantUseCase(catalog(Flux.empty()), new FakeDetailReader(),
                            new UnconfiguredLlmGateway()))))
            .build();

    private static InvestigationCatalogReader catalog(Flux<InvestigationSummary> summaries) {
        return new InvestigationCatalogReader() {
            @Override
            public Flux<InvestigationSummary> findAllSummaries() {
                return summaries;
            }

            @Override
            public Flux<TestCondition> findAllNormalizedConditions() {
                return Flux.empty();
            }
        };
    }

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
                .jsonPath("$.error").value(Matchers.containsString("pillar"));
    }

    @Test
    void detailIncludesCitationConditionsEnvelopeAndWarnings() {
        client.get().uri("/api/investigations/psi-25").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.psiId").isEqualTo("PSI-25")
                .jsonPath("$.citation").value(Matchers.allOf(
                        Matchers.containsString("(BASS-II)"),
                        Matchers.containsString("https://doi.org/10.60555/4qc4-de67")))
                .jsonPath("$.license").isEqualTo("CC0-1.0")
                .jsonPath("$.publications[0].doiUrl").isEqualTo("https://doi.org/10.1016/j.combustflame.2017.03.001")
                .jsonPath("$.publications[1].doiUrl").doesNotExist()
                .jsonPath("$.conditions.testRows").isEqualTo(2)
                .jsonPath("$.conditions.o2Fraction.min").isEqualTo(0.17)
                .jsonPath("$.conditions.o2Fraction.max").isEqualTo(0.222)
                .jsonPath("$.conditions.pressureAtm").doesNotExist()
                .jsonPath("$.conditions.fuelsOrMaterials.length()").isEqualTo(2)
                .jsonPath("$.dataQualityWarnings[0].reason").isEqualTo("no numeric value");
    }

    @Test
    void testConditionsKeepRawColumnOrder() {
        client.get().uri("/api/investigations/PSI-25/test-conditions").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].testLabel").isEqualTo("B1")
                .jsonPath("$[0].o2Fraction.max").isEqualTo(0.222)
                .jsonPath("$[0].raw['Test #']").isEqualTo("B1");
    }

    @Test
    void unknownInvestigationIsNotFound() {
        client.get().uri("/api/investigations/PSI-9999").exchange().expectStatus().isNotFound();
        client.get().uri("/api/investigations/PSI-9999/test-conditions").exchange().expectStatus().isNotFound();
    }

    @Test
    void malformedIdIsBadRequest() {
        client.get().uri("/api/investigations/bass").exchange().expectStatus().isBadRequest();
    }

    private static InvestigationSummary summary(String id, String projectType, HardwareFamily hardware,
                                                FuelState fuel, SafetyPillar pillar, int tables) {
        PsiId psiId = PsiId.of(id);
        return new InvestigationSummary(psiId, id, "Title " + id, new Doi("10.60555/" + id.toLowerCase()),
                projectType, null, null, new CatalogEntry(psiId, hardware, fuel, pillar), tables,
                tables * 10L, tables * 5L);
    }

    private static final class FakeDetailReader implements InvestigationDetailReader {

        @Override
        public Mono<Investigation> findById(PsiId psiId) {
            if (!psiId.equals(BASS_II)) {
                return Mono.empty();
            }
            return Mono.just(new Investigation(BASS_II, "BASS-II", "Burning and Suppression of Solids - II",
                    new Doi("10.60555/4qc4-de67"), "Microgravity Investigation", "Combustion Science",
                    "Spacecraft Fire Safety", "International Space Station (ISS)",
                    "Microgravity Science Glovebox (MSG)", "objective", "approach", "hypothesis", "impacts",
                    null, null, "CC0-1.0",
                    List.of(new Publication("Paper", "Olson S.L.", "10.1016/j.combustflame.2017.03.001"),
                            new Publication("Report", "Ferkul P.", "")),
                    List.of(), new CatalogEntry(BASS_II, HardwareFamily.MSG, FuelState.SOLID, SafetyPillar.PREVENTION),
                    "{}"));
        }

        @Override
        public Flux<TestCondition> findTestConditions(PsiId psiId) {
            Map<String, String> raw = new LinkedHashMap<>();
            raw.put("PI", "Bhattacharjee");
            raw.put("Test #", "B1");
            return Flux.just(
                    new TestCondition(psiId, "f.csv", 1, "B1", "PMMA", new NumericRange(0.222, 0.222), null, null, raw),
                    new TestCondition(psiId, "f.csv", 2, "B2", "Cotton", NumericRange.of(0.17), null, null, raw));
        }

        @Override
        public Flux<IngestionWarning> findWarnings(PsiId psiId) {
            return Flux.just(new IngestionWarning(psiId, "f.csv", 3, "O2", "RAMP", "no numeric value"));
        }
    }
}
