package com.ignis.vesta.entrypoint.http;

import com.ignis.vesta.application.AskAssistantUseCase;
import com.ignis.vesta.domain.port.LlmGateway;
import com.ignis.vesta.entrypoint.http.dto.AssistantDtos;
import com.ignis.vesta.entrypoint.http.dto.ErrorDto;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

/** HTTP handler for the grounded assistant (spec F5). */
@Component
public class AssistantHandler {

    private final AskAssistantUseCase askAssistant;

    public AssistantHandler(AskAssistantUseCase askAssistant) {
        this.askAssistant = askAssistant;
    }

    /** {@code POST /api/assistant/ask} with body {@code {"question": "..."}} */
    public Mono<ServerResponse> ask(ServerRequest request) {
        return request.bodyToMono(AssistantDtos.AskRequest.class)
                .defaultIfEmpty(new AssistantDtos.AskRequest(null))
                .flatMap(body -> askAssistant.ask(body.question()))
                .flatMap(result -> ServerResponse.ok().bodyValue(AssistantDtos.AskResponse.from(result)))
                .onErrorResume(IllegalArgumentException.class, e -> error(HttpStatus.BAD_REQUEST, e.getMessage()))
                .onErrorResume(DecodingException.class, e -> error(HttpStatus.BAD_REQUEST, "Malformed JSON body"))
                .onErrorResume(ServerWebInputException.class, e -> error(HttpStatus.BAD_REQUEST, "Malformed JSON body"))
                .onErrorResume(LlmGateway.LlmUnavailableException.class,
                        e -> error(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage()));
    }

    private static Mono<ServerResponse> error(HttpStatus status, String message) {
        return ServerResponse.status(status).bodyValue(new ErrorDto(message));
    }
}
