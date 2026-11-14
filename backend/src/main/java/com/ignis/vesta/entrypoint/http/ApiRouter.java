package com.ignis.vesta.entrypoint.http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/** Functional routes for the public HTTP API. Handlers hold no business logic; they delegate to use cases. */
@Configuration
public class ApiRouter {

    @Bean
    public RouterFunction<ServerResponse> apiRoutes(HealthHandler healthHandler) {
        return route(GET("/api/meta/health"), healthHandler::health);
    }
}
