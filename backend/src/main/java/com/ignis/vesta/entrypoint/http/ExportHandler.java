package com.ignis.vesta.entrypoint.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ignis.vesta.application.ExportTestConditionsUseCase;
import com.ignis.vesta.domain.export.TestConditionQuery;
import com.ignis.vesta.domain.ranking.HabitatEnvironment;
import com.ignis.vesta.domain.vo.PsiId;
import com.ignis.vesta.entrypoint.http.dto.ErrorDto;
import com.ignis.vesta.entrypoint.http.dto.ExportDtos;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@code GET /api/test-conditions/export?format=json|csv&psiIds=PSI-25,PSI-69&minO2Percent=30&maxPressurePsi=10.2}.
 * All parameters are optional; JSON is the default format. CSV is sent as a file download.
 */
@Component
public class ExportHandler {

    private static final MediaType CSV = new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8);

    private final ExportTestConditionsUseCase exportTestConditions;
    private final ObjectMapper objectMapper;

    public ExportHandler(ExportTestConditionsUseCase exportTestConditions, ObjectMapper objectMapper) {
        this.exportTestConditions = exportTestConditions;
        this.objectMapper = objectMapper;
    }

    public Mono<ServerResponse> export(ServerRequest request) {
        TestConditionQuery query;
        String format = request.queryParam("format").orElse("json").trim().toLowerCase(Locale.ROOT);
        try {
            if (!format.equals("json") && !format.equals("csv")) {
                throw new IllegalArgumentException("Invalid format '" + format + "'. Allowed: json, csv");
            }
            query = new TestConditionQuery(psiIds(request),
                    number(request, "minO2Percent").map(v -> v / 100.0),
                    number(request, "maxPressurePsi").map(v -> v / HabitatEnvironment.PSI_PER_ATM));
        } catch (IllegalArgumentException e) {
            return ServerResponse.badRequest().bodyValue(new ErrorDto(e.getMessage()));
        }
        var rows = exportTestConditions.export(query).collectList();
        if (format.equals("csv")) {
            return rows.flatMap(list -> ServerResponse.ok()
                    .contentType(CSV)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"vesta-zero-test-conditions.csv\"")
                    .bodyValue(ExportDtos.toCsv(list, this::json)));
        }
        return rows.flatMap(list -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ExportDtos.ExportDto(ExportDtos.CITATION_NOTE, ExportDtos.FiltersDto.from(query),
                        list.size(), list.stream().map(ExportDtos.RowDto::from).toList())));
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Set<PsiId> psiIds(ServerRequest request) {
        return request.queryParam("psiIds").map(raw -> Arrays.stream(raw.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(s -> {
                            try {
                                return PsiId.of(s.toUpperCase(Locale.ROOT));
                            } catch (IllegalArgumentException e) {
                                throw new IllegalArgumentException("Invalid PSI id '" + s + "'");
                            }
                        })
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    private static Optional<Double> number(ServerRequest request, String name) {
        Optional<String> raw = request.queryParam(name).map(String::trim).filter(s -> !s.isEmpty());
        if (raw.isEmpty()) {
            return Optional.empty();
        }
        try {
            double value = Double.parseDouble(raw.get());
            if (!Double.isFinite(value)) {
                throw new NumberFormatException();
            }
            return Optional.of(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number '" + raw.get() + "' for '" + name + "'");
        }
    }
}
