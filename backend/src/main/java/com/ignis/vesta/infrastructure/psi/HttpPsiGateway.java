package com.ignis.vesta.infrastructure.psi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ignis.vesta.domain.catalog.CatalogEntry;
import com.ignis.vesta.domain.model.ExperimentalTableRef;
import com.ignis.vesta.domain.model.Investigation;
import com.ignis.vesta.domain.port.PsiGateway;
import com.ignis.vesta.domain.vo.PsiId;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * PSI adapter over the unofficial geode-py API. Downloads use a signed S3 URL that expires within minutes:
 * it is requested right before use, kept only in memory, and stripped from every error message.
 */
public final class HttpPsiGateway implements PsiGateway {

    private final WebClient psiClient;
    private final WebClient downloadClient;
    private final PsiRecordMapper mapper;
    private final Duration timeout;

    public HttpPsiGateway(WebClient psiClient, WebClient downloadClient, ObjectMapper objectMapper, Duration timeout) {
        this.psiClient = psiClient;
        this.downloadClient = downloadClient;
        this.mapper = new PsiRecordMapper(objectMapper);
        this.timeout = timeout;
    }

    @Override
    public Mono<Investigation> fetchInvestigation(CatalogEntry entry) {
        PsiId id = entry.psiId();
        return psiClient.get()
                .uri("/repo/investigations/{id}", id.value())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(timeout)
                .retryWhen(transientRetry())
                .onErrorMap(e -> sanitized("metadata request for " + id, e))
                .map(json -> mapper.toInvestigation(json, entry));
    }

    @Override
    public Mono<byte[]> downloadExperimentalTable(PsiId psiId, ExperimentalTableRef table) {
        return signedUrl(psiId, table)
                .flatMap(url -> downloadClient.get()
                        .uri(URI.create(url))
                        .retrieve()
                        .bodyToMono(byte[].class)
                        .timeout(timeout))
                .retryWhen(transientRetry())
                .onErrorMap(e -> sanitized("download of " + table.fileName(), e));
    }

    private Mono<String> signedUrl(PsiId psiId, ExperimentalTableRef table) {
        return psiClient.get()
                .uri(builder -> builder.path("/studies/{id}/download")
                        .queryParam("file", "{file}")
                        .queryParam("version", "{version}")
                        .queryParam("redirect", "false")
                        .build(Map.of("id", psiId.value(), "file", table.fileName(),
                                "version", String.valueOf(table.version()))))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(timeout)
                .map(String::trim)
                .flatMap(body -> body.startsWith("https://")
                        ? Mono.just(body)
                        : Mono.error(new PsiAccessException("PSI did not return a download URL")));
    }

    /** One retry on timeouts and connection errors (observed in practice), never on 4xx. */
    private static Retry transientRetry() {
        return Retry.backoff(1, Duration.ofSeconds(3))
                .filter(e -> e instanceof TimeoutException || e instanceof WebClientRequestException
                        || e instanceof WebClientResponseException r && r.getStatusCode().is5xxServerError());
    }

    /** Rebuilds errors without URLs: WebClient messages embed the request URI, which may be a signed URL. */
    private static Throwable sanitized(String what, Throwable error) {
        Throwable cause = Exceptions.isRetryExhausted(error) && error.getCause() != null ? error.getCause() : error;
        if (cause instanceof PsiAccessException) {
            return cause;
        }
        String detail = switch (cause) {
            case WebClientResponseException r -> "HTTP " + r.getStatusCode().value();
            case TimeoutException t -> "timeout";
            case WebClientRequestException r -> "connection error";
            default -> cause.getClass().getSimpleName();
        };
        return new PsiAccessException(what + " failed: " + detail);
    }
}
