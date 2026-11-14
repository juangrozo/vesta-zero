package com.ignis.vesta.entrypoint.http;

import com.ignis.vesta.application.GetInvestigationUseCase;
import com.ignis.vesta.application.ListInvestigationsUseCase;
import com.ignis.vesta.domain.catalog.CatalogFilter;
import com.ignis.vesta.domain.catalog.FuelState;
import com.ignis.vesta.domain.catalog.HardwareFamily;
import com.ignis.vesta.domain.catalog.InvestigationType;
import com.ignis.vesta.domain.catalog.SafetyPillar;
import com.ignis.vesta.domain.vo.PsiId;
import com.ignis.vesta.entrypoint.http.dto.ErrorDto;
import com.ignis.vesta.entrypoint.http.dto.InvestigationDetailDto;
import com.ignis.vesta.entrypoint.http.dto.InvestigationSummaryDto;
import com.ignis.vesta.entrypoint.http.dto.TestConditionDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/** HTTP handler for investigations. Parses and validates input, delegates to use cases, maps to DTOs. */
@Component
public class InvestigationHandler {

    private final ListInvestigationsUseCase listInvestigations;
    private final GetInvestigationUseCase getInvestigation;

    public InvestigationHandler(ListInvestigationsUseCase listInvestigations, GetInvestigationUseCase getInvestigation) {
        this.listInvestigations = listInvestigations;
        this.getInvestigation = getInvestigation;
    }

    /** {@code GET /api/investigations?type=&hardware=&fuelState=&pillar=} */
    public Mono<ServerResponse> list(ServerRequest request) {
        CatalogFilter filter;
        try {
            filter = new CatalogFilter(
                    param(request, "type", InvestigationType.class),
                    param(request, "hardware", HardwareFamily.class),
                    param(request, "fuelState", FuelState.class),
                    param(request, "pillar", SafetyPillar.class));
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
        return listInvestigations.list(filter)
                .map(InvestigationSummaryDto::from)
                .collectList()
                .flatMap(items -> ServerResponse.ok().bodyValue(items));
    }

    /** {@code GET /api/investigations/{psiId}} */
    public Mono<ServerResponse> detail(ServerRequest request) {
        return withPsiId(request, id -> getInvestigation.detail(id)
                .map(InvestigationDetailDto::from)
                .flatMap(dto -> ServerResponse.ok().bodyValue(dto))
                .switchIfEmpty(notFound(id)));
    }

    /** {@code GET /api/investigations/{psiId}/test-conditions} */
    public Mono<ServerResponse> testConditions(ServerRequest request) {
        return withPsiId(request, id -> getInvestigation.testConditions(id)
                .map(rows -> rows.stream().map(TestConditionDto::from).toList())
                .flatMap(rows -> ServerResponse.ok().bodyValue(rows))
                .switchIfEmpty(notFound(id)));
    }

    private static Mono<ServerResponse> withPsiId(ServerRequest request,
                                                  java.util.function.Function<PsiId, Mono<ServerResponse>> action) {
        String raw = request.pathVariable("psiId").toUpperCase(Locale.ROOT);
        PsiId id;
        try {
            id = PsiId.of(raw);
        } catch (IllegalArgumentException e) {
            return badRequest("Invalid PSI id '" + request.pathVariable("psiId") + "'. Expected format PSI-<number>.");
        }
        return action.apply(id);
    }

    private static Mono<ServerResponse> notFound(PsiId id) {
        return Mono.defer(() -> ServerResponse.status(HttpStatus.NOT_FOUND)
                .bodyValue(new ErrorDto("Investigation " + id + " is not in the Vesta Zero catalog")));
    }

    private static Mono<ServerResponse> badRequest(String message) {
        return ServerResponse.badRequest().bodyValue(new ErrorDto(message));
    }

    private static <E extends Enum<E>> Optional<E> param(ServerRequest request, String name, Class<E> type) {
        Optional<String> raw = request.queryParam(name).map(String::trim).filter(s -> !s.isEmpty());
        if (raw.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Enum.valueOf(type, raw.get().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid value '" + raw.get() + "' for '" + name + "'. Allowed: "
                    + Arrays.toString(type.getEnumConstants()));
        }
    }
}
