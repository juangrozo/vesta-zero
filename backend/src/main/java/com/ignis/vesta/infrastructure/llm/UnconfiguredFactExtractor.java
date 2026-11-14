package com.ignis.vesta.infrastructure.llm;

import com.ignis.vesta.domain.extraction.ExtractedFact;
import com.ignis.vesta.domain.extraction.ExtractionSource;
import com.ignis.vesta.domain.port.FactExtractor;
import com.ignis.vesta.domain.port.LlmGateway.LlmUnavailableException;
import reactor.core.publisher.Mono;

import java.util.List;

/** Null object used when no API key is configured: every extraction fails with a clear message. */
public final class UnconfiguredFactExtractor implements FactExtractor {

    private final String model;

    public UnconfiguredFactExtractor(String model) {
        this.model = model;
    }

    @Override
    public Mono<List<ExtractedFact>> extract(ExtractionSource source) {
        return Mono.error(new LlmUnavailableException("ANTHROPIC_API_KEY is not set"));
    }

    @Override
    public String model() {
        return model;
    }

    @Override
    public String promptVersion() {
        return ExtractionPrompt.VERSION;
    }
}
