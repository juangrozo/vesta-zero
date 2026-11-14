package com.ignis.vesta.entrypoint.http;

import com.ignis.vesta.application.RankInvestigationsUseCase;
import com.ignis.vesta.entrypoint.http.dto.ErrorDto;
import com.ignis.vesta.entrypoint.http.dto.RankingDtos;
import org.springframework.core.codec.DecodingException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

/** HTTP handler for the explainable ranking (spec F3). */
@Component
public class RankingHandler {

    private final RankInvestigationsUseCase rankInvestigations;

    public RankingHandler(RankInvestigationsUseCase rankInvestigations) {
        this.rankInvestigations = rankInvestigations;
    }

    /** {@code GET /api/ranking/criteria} */
    public Mono<ServerResponse> criteria(ServerRequest request) {
        return ServerResponse.ok().bodyValue(
                rankInvestigations.criteria().stream().map(RankingDtos.CriterionDto::from).toList());
    }

    /** {@code POST /api/ranking} with optional body {@code {"weights": {"lunarAtmosphere": 0.5, ...}}} */
    public Mono<ServerResponse> rank(ServerRequest request) {
        return request.bodyToMono(RankingDtos.RankingRequest.class)
                .defaultIfEmpty(new RankingDtos.RankingRequest(null))
                .flatMap(body -> rankInvestigations.rank(body.weightsOrEmpty()))
                .flatMap(result -> ServerResponse.ok().bodyValue(RankingDtos.RankingResponse.from(result)))
                .onErrorResume(IllegalArgumentException.class, e -> badRequest(e.getMessage()))
                .onErrorResume(DecodingException.class, e -> badRequest("Malformed JSON body"))
                .onErrorResume(ServerWebInputException.class, e -> badRequest("Malformed JSON body"));
    }

    private static Mono<ServerResponse> badRequest(String message) {
        return ServerResponse.badRequest().bodyValue(new ErrorDto(message));
    }
}
