package com.ignis.vesta.domain.port;

import com.ignis.vesta.domain.assistant.EvidenceCorpus;
import com.ignis.vesta.domain.assistant.GeneratedAnswer;
import reactor.core.publisher.Mono;

/** Access to the language model. Implementations must answer only from the given corpus. */
public interface LlmGateway {

    /**
     * Answers a question using only {@code corpus}. Errors with {@link LlmUnavailableException} when the model
     * cannot be reached or is not configured.
     */
    Mono<GeneratedAnswer> answer(String question, EvidenceCorpus corpus);

    /** The language model is not configured or not reachable. Messages never contain credentials. */
    final class LlmUnavailableException extends RuntimeException {
        public LlmUnavailableException(String message) {
            super(message);
        }
    }
}
