package com.ignis.vesta.infrastructure.persistence;

import com.ignis.vesta.infrastructure.llm.LlmCacheStore;
import io.r2dbc.postgresql.codec.Json;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

/** llm_cache table adapter. First write wins; later writes for the same hash are ignored. */
public final class R2dbcLlmCacheStore implements LlmCacheStore {

    private final DatabaseClient db;

    public R2dbcLlmCacheStore(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Mono<String> find(String hash) {
        return db.sql("SELECT response FROM llm_cache WHERE hash = :hash")
                .bind("hash", hash)
                .map(row -> row.get("response", Json.class))
                .one()
                .map(Json::asString);
    }

    @Override
    public Mono<Void> save(String hash, String requestJson, String responseJson, String model) {
        return db.sql("""
                        INSERT INTO llm_cache (hash, request, response, model)
                        VALUES (:hash, CAST(:request AS jsonb), CAST(:response AS jsonb), :model)
                        ON CONFLICT (hash) DO NOTHING
                        """)
                .bind("hash", hash)
                .bind("request", requestJson)
                .bind("response", responseJson)
                .bind("model", model)
                .then();
    }
}
