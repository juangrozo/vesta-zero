package com.ignis.vesta.infrastructure.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ignis.vesta.domain.assistant.EvidenceCorpus;
import com.ignis.vesta.domain.assistant.GeneratedAnswer;
import com.ignis.vesta.domain.port.LlmGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;

/**
 * Decorator: answers already given for the same question, corpus, model and prompt version come from the database,
 * so a demo never repeats an API call. Declined answers are not cached. A cache failure never blocks an answer.
 */
public final class CachingLlmGateway implements LlmGateway {

    private static final Logger log = LoggerFactory.getLogger(CachingLlmGateway.class);

    private final LlmGateway delegate;
    private final LlmCacheStore store;
    private final ObjectMapper objectMapper;
    private final String model;

    public CachingLlmGateway(LlmGateway delegate, LlmCacheStore store, ObjectMapper objectMapper, String model) {
        this.delegate = delegate;
        this.store = store;
        this.objectMapper = objectMapper;
        this.model = model;
    }

    @Override
    public Mono<GeneratedAnswer> answer(String question, EvidenceCorpus corpus) {
        String normalized = question.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        String hash = sha256(String.join("\n", AssistantPrompt.VERSION, model, corpus.fingerprint(), normalized));
        return store.find(hash)
                .onErrorResume(e -> {
                    log.warn("LLM cache read failed: {}", e.getMessage());
                    return Mono.empty();
                })
                .map(json -> read(json).withFromCache(true))
                .switchIfEmpty(Mono.defer(() -> delegate.answer(question, corpus)
                        .flatMap(answer -> answer.declined() ? Mono.just(answer)
                                : store.save(hash, write(Map.of("kind", "assistant", "question", question,
                                                "promptVersion", AssistantPrompt.VERSION,
                                                "corpusFingerprint", corpus.fingerprint())),
                                        write(answer), model)
                                .onErrorResume(e -> {
                                    log.warn("LLM cache write failed: {}", e.getMessage());
                                    return Mono.empty();
                                })
                                .thenReturn(answer))));
    }

    private GeneratedAnswer read(String json) {
        try {
            return objectMapper.readValue(json, GeneratedAnswer.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cached answer is not valid JSON", e);
        }
    }

    private String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize value", e);
        }
    }

    private static String sha256(String text) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
