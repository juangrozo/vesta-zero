package com.ignis.vesta.domain.port;

import com.ignis.vesta.domain.extraction.ExtractedFact;
import com.ignis.vesta.domain.extraction.ExtractionSource;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Extracts summary facts from one investigation's PSI text with a language model. Results are unverified;
 * callers check them with {@link com.ignis.vesta.domain.extraction.ExtractionVerifier}. Errors with
 * {@link LlmGateway.LlmUnavailableException} when the model is not configured or not reachable.
 */
public interface FactExtractor {

    Mono<List<ExtractedFact>> extract(ExtractionSource source);

    /** Model id written next to every stored fact. */
    String model();

    /** Prompt version; a new version makes stored extractions stale. */
    String promptVersion();
}
