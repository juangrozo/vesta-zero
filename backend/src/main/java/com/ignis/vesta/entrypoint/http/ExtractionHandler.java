package com.ignis.vesta.entrypoint.http;

import com.ignis.vesta.application.GetExtractionUseCase;
import com.ignis.vesta.domain.vo.PsiId;
import com.ignis.vesta.entrypoint.http.dto.ErrorDto;
import com.ignis.vesta.entrypoint.http.dto.ExtractionDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Locale;

/** HTTP handler for stored AI extractions. Never calls the model: extraction runs offline. */
@Component
public class ExtractionHandler {

    private final GetExtractionUseCase getExtraction;

    public ExtractionHandler(GetExtractionUseCase getExtraction) {
        this.getExtraction = getExtraction;
    }

    /** {@code GET /api/investigations/{psiId}/extraction} */
    public Mono<ServerResponse> extraction(ServerRequest request) {
        PsiId id;
        try {
            id = PsiId.of(request.pathVariable("psiId").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return ServerResponse.badRequest().bodyValue(new ErrorDto(
                    "Invalid PSI id '" + request.pathVariable("psiId") + "'. Expected format PSI-<number>."));
        }
        return getExtraction.find(id)
                .flatMap(stored -> ServerResponse.ok().bodyValue(ExtractionDto.from(id.value(), stored)))
                .switchIfEmpty(Mono.defer(() -> ServerResponse.status(HttpStatus.NOT_FOUND)
                        .bodyValue(new ErrorDto("Investigation " + id + " is not in the Vesta Zero catalog"))));
    }
}
