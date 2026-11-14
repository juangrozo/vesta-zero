package com.ignis.vesta.entrypoint.http;

import com.ignis.vesta.application.CompareInvestigationsUseCase;
import com.ignis.vesta.domain.port.LlmGateway;
import com.ignis.vesta.entrypoint.http.dto.AssistantDtos;
import com.ignis.vesta.entrypoint.http.dto.CompareDtos;
import com.ignis.vesta.entrypoint.http.dto.ErrorDto;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

/** HTTP handler for the comparator (spec F4). */
@Component
public class CompareHandler {

    private final CompareInvestigationsUseCase compareInvestigations;

    public CompareHandler(CompareInvestigationsUseCase compareInvestigations) {
        this.compareInvestigations = compareInvestigations;
    }

    /** {@code POST /api/compare} with body {@code {"psiIds": ["PSI-68", "PSI-69"]}} */
    public Mono<ServerResponse> compare(ServerRequest request) {
        return withErrors(body(request)
                .flatMap(b -> compareInvestigations.compare(b.psiIds()))
                .flatMap(c -> ServerResponse.ok().bodyValue(CompareDtos.CompareResponse.from(c))));
    }

    /** {@code POST /api/compare/narrative}: grounded AI comparison of the same selection. */
    public Mono<ServerResponse> narrative(ServerRequest request) {
        return withErrors(body(request)
                .flatMap(b -> compareInvestigations.narrative(b.psiIds()))
                .flatMap(r -> ServerResponse.ok().bodyValue(AssistantDtos.AskResponse.from(r))));
    }

    private static Mono<CompareDtos.CompareRequest> body(ServerRequest request) {
        return request.bodyToMono(CompareDtos.CompareRequest.class).defaultIfEmpty(new CompareDtos.CompareRequest(null));
    }

    private static Mono<ServerResponse> withErrors(Mono<ServerResponse> response) {
        return response
                .onErrorResume(IllegalArgumentException.class, e -> error(HttpStatus.BAD_REQUEST, e.getMessage()))
                .onErrorResume(DecodingException.class, e -> error(HttpStatus.BAD_REQUEST, "Malformed JSON body"))
                .onErrorResume(ServerWebInputException.class, e -> error(HttpStatus.BAD_REQUEST, "Malformed JSON body"))
                .onErrorResume(CompareInvestigationsUseCase.NotFoundException.class,
                        e -> error(HttpStatus.NOT_FOUND, e.getMessage()))
                .onErrorResume(LlmGateway.LlmUnavailableException.class,
                        e -> error(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage()));
    }

    private static Mono<ServerResponse> error(HttpStatus status, String message) {
        return ServerResponse.status(status).bodyValue(new ErrorDto(message));
    }
}
