package com.ignis.vesta.infrastructure.llm;

import reactor.core.publisher.Mono;

/** Persistent store for LLM responses keyed by a request hash. */
public interface LlmCacheStore {

    Mono<String> find(String hash);

    Mono<Void> save(String hash, String requestJson, String responseJson, String model);
}
