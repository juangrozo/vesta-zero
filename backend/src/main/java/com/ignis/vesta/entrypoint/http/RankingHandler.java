package com.ignis.vesta.entrypoint.http;

import com.ignis.vesta.application.RankInvestigationsUseCase;
import com.ignis.vesta.domain.ranking.HabitatEnvironment;
import com.ignis.vesta.entrypoint.http.dto.ErrorDto;
import com.ignis.vesta.entrypoint.http.dto.RankingDtos;
import org.springframework.core.codec.DecodingException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/** HTTP handler for the explainable ranking (spec F3). */
@Component
public class RankingHandler {

    private final RankInvestigationsUseCase rankInvestigations;

    public RankingHandler(RankInvestigationsUseCase rankInvestigations) {
        this.rankInvestigations = rankInvestigations;
    }

    /** {@code GET /api/ranking/environments} */
    public Mono<ServerResponse> environments(ServerRequest request) {
        return ServerResponse.ok().bodyValue(
                Arrays.stream(HabitatEnvironment.values()).map(RankingDtos.EnvironmentDto::from).toList());
    }

    /** {@code GET /api/ranking/criteria?environment=LUNAR_BASE_ROVER} (environment optional) */
    public Mono<ServerResponse> criteria(ServerRequest request) {
        HabitatEnvironment environment;
        try {
            environment = HabitatEnvironment.parse(request.queryParam("environment").orElse(null));
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
        return ServerResponse.ok().bodyValue(
                rankInvestigations.criteria(environment).stream().map(RankingDtos.CriterionDto::from).toList());
    }

    /**
     * {@code POST /api/ranking} with optional body
     * {@code {"environment": "SPACECRAFT_STATION", "weights": {"atmosphere": 0.5, ...}}}
     */
    public Mono<ServerResponse> rank(ServerRequest request) {
        return request.bodyToMono(RankingDtos.RankingRequest.class)
                .defaultIfEmpty(new RankingDtos.RankingRequest(null, null))
                .flatMap(body -> rankInvestigations.rank(HabitatEnvironment.parse(body.environment()),
                        body.weightsOrEmpty()))
                .flatMap(result -> ServerResponse.ok().bodyValue(RankingDtos.RankingResponse.from(result)))
                .onErrorResume(IllegalArgumentException.class, e -> badRequest(e.getMessage()))
                .onErrorResume(DecodingException.class, e -> badRequest("Malformed JSON body"))
                .onErrorResume(ServerWebInputException.class, e -> badRequest("Malformed JSON body"));
    }

    private static Mono<ServerResponse> badRequest(String message) {
        return ServerResponse.badRequest().bodyValue(new ErrorDto(message));
    }
}
