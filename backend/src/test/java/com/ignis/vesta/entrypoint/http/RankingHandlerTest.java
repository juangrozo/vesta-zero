package com.ignis.vesta.entrypoint.http;

import com.ignis.vesta.application.RankInvestigationsUseCase;
import com.ignis.vesta.domain.catalog.CatalogEntry;
import com.ignis.vesta.domain.catalog.FuelState;
import com.ignis.vesta.domain.catalog.HardwareFamily;
import com.ignis.vesta.domain.catalog.SafetyPillar;
import com.ignis.vesta.domain.model.InvestigationSummary;
import com.ignis.vesta.domain.model.TestCondition;
import com.ignis.vesta.domain.port.InvestigationCatalogReader;
import com.ignis.vesta.domain.ranking.DefaultRankingCriteria;
import com.ignis.vesta.domain.ranking.RankingEngine;
import com.ignis.vesta.domain.vo.Doi;
import com.ignis.vesta.domain.vo.NumericRange;
import com.ignis.vesta.domain.vo.PsiId;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.Map;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/** HTTP contract for F3 against an in-memory catalog (no database). */
class RankingHandlerTest {

    private final RankingHandler handler = new RankingHandler(new RankInvestigationsUseCase(new InvestigationCatalogReader() {
        @Override
        public Flux<InvestigationSummary> findAllSummaries() {
            return Flux.just(summary("PSI-25", FuelState.SOLID, SafetyPillar.PREVENTION),
                    summary("PSI-159", FuelState.GAS, SafetyPillar.FUNDAMENTAL));
        }

        @Override
        public Flux<TestCondition> findAllNormalizedConditions() {
            return Flux.just(new TestCondition(PsiId.of("PSI-159"), "t.csv", 3, "21088A1", "ethane",
                    NumericRange.of(0.85), NumericRange.of(0.49), null, Map.of()));
        }
    }, new RankingEngine(DefaultRankingCriteria.all())));

    private final WebTestClient client = WebTestClient.bindToRouterFunction(
            route(GET("/api/ranking/criteria"), handler::criteria).andRoute(POST("/api/ranking"), handler::rank))
            .build();

    @Test
    void criteriaListsDefaultsWithProvenance() {
        client.get().uri("/api/ranking/criteria").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(5)
                .jsonPath("$[0].id").isEqualTo("lunarAtmosphere")
                .jsonPath("$[0].defaultWeight").isEqualTo(0.30)
                .jsonPath("$[2].provenance").isEqualTo("CURATED");
    }

    @Test
    void rankWithoutBodyUsesDefaultsAndExplainsEveryResult() {
        client.post().uri("/api/ranking").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.results.length()").isEqualTo(2)
                .jsonPath("$.results[0].position").isEqualTo(1)
                .jsonPath("$.results[0].breakdown.length()").isEqualTo(5)
                .jsonPath("$.lunarTarget.pressurePsi").isEqualTo(10.2)
                .jsonPath("$.partialGravityNotice").value(Matchers.containsString("LUCI"));
    }

    @Test
    void weightsChangeTheWinner() {
        client.post().uri("/api/ranking").contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"weights\":{\"lunarAtmosphere\":1,\"habitatRelevance\":0,\"materialType\":0,"
                        + "\"safetyPillar\":0,\"structuredData\":0}}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.results[0].psiId").isEqualTo("PSI-159")
                .jsonPath("$.weights.lunarAtmosphere").isEqualTo(1.0);

        client.post().uri("/api/ranking").contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"weights\":{\"lunarAtmosphere\":0,\"habitatRelevance\":0,\"materialType\":1,"
                        + "\"safetyPillar\":0,\"structuredData\":0}}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.results[0].psiId").isEqualTo("PSI-25");
    }

    @Test
    void invalidWeightsAreBadRequest() {
        client.post().uri("/api/ranking").contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"weights\":{\"magic\":1}}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.error").value(Matchers.containsString("Unknown criterion"));

        client.post().uri("/api/ranking").contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{not json")
                .exchange()
                .expectStatus().isBadRequest();
    }

    private static InvestigationSummary summary(String id, FuelState fuel, SafetyPillar pillar) {
        PsiId psiId = PsiId.of(id);
        return new InvestigationSummary(psiId, id, "Title " + id, new Doi("10.60555/x"),
                "Microgravity Investigation", "Spacecraft Fire Safety", null,
                new CatalogEntry(psiId, HardwareFamily.MSG, fuel, pillar), 1, 10, 10);
    }
}
