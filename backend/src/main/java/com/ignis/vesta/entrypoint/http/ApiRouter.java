package com.ignis.vesta.entrypoint.http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/** Functional routes for the public HTTP API. Handlers hold no business logic; they delegate to use cases. */
@Configuration
public class ApiRouter {

    @Bean
    public RouterFunction<ServerResponse> apiRoutes(HealthHandler healthHandler,
                                                    InvestigationHandler investigationHandler,
                                                    RankingHandler rankingHandler,
                                                    AssistantHandler assistantHandler,
                                                    CompareHandler compareHandler,
                                                    ExtractionHandler extractionHandler) {
        return route(GET("/api/meta/health"), healthHandler::health)
                .andRoute(GET("/api/investigations"), investigationHandler::list)
                .andRoute(GET("/api/investigations/{psiId}"), investigationHandler::detail)
                .andRoute(GET("/api/investigations/{psiId}/test-conditions"), investigationHandler::testConditions)
                .andRoute(GET("/api/investigations/{psiId}/extraction"), extractionHandler::extraction)
                .andRoute(GET("/api/ranking/environments"), rankingHandler::environments)
                .andRoute(GET("/api/ranking/criteria"), rankingHandler::criteria)
                .andRoute(POST("/api/ranking"), rankingHandler::rank)
                .andRoute(POST("/api/assistant/ask"), assistantHandler::ask)
                .andRoute(POST("/api/compare"), compareHandler::compare)
                .andRoute(POST("/api/compare/narrative"), compareHandler::narrative);
    }
}
