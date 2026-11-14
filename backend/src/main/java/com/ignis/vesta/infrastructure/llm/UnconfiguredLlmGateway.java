package com.ignis.vesta.infrastructure.llm;

import com.ignis.vesta.domain.assistant.EvidenceCorpus;
import com.ignis.vesta.domain.assistant.GeneratedAnswer;
import com.ignis.vesta.domain.port.LlmGateway;
import reactor.core.publisher.Mono;

/** Null object used when ANTHROPIC_API_KEY is not set: the rest of the dashboard keeps working. */
public final class UnconfiguredLlmGateway implements LlmGateway {

    @Override
    public Mono<GeneratedAnswer> answer(String question, EvidenceCorpus corpus) {
        return Mono.error(new LlmUnavailableException("The AI assistant is not configured (ANTHROPIC_API_KEY is not set)"));
    }
}
