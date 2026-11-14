package com.ignis.vesta.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.ignis.vesta.domain.extraction.ExtractedFact;
import com.ignis.vesta.domain.extraction.ExtractionField;
import com.ignis.vesta.domain.extraction.InvestigationExtraction;
import com.ignis.vesta.domain.port.ExtractionRepository;
import com.ignis.vesta.domain.vo.PsiId;
import io.r2dbc.postgresql.codec.Json;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ai_extraction adapter. One row per field holds the JSON array of its facts (label, detail, source_span), so a
 * field the record does not document is stored as an empty array and stays distinguishable from "not extracted".
 */
public final class R2dbcExtractionRepository implements ExtractionRepository {

    private final DatabaseClient db;
    private final TransactionalOperator transactions;
    private final ObjectMapper objectMapper;

    public R2dbcExtractionRepository(DatabaseClient db, TransactionalOperator transactions,
                                     ObjectMapper objectMapper) {
        this.db = db;
        this.transactions = transactions;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> replace(InvestigationExtraction extraction) {
        Mono<Void> delete = db.sql("DELETE FROM ai_extraction WHERE investigation_id = :id")
                .bind("id", extraction.psiId().value())
                .then();
        Flux<Long> inserts = Flux.fromArray(ExtractionField.values()).concatMap(field -> db.sql("""
                        INSERT INTO ai_extraction (investigation_id, field, value, model, prompt_version, created_at)
                        VALUES (:id, :field, CAST(:value AS jsonb), :model, :version, :createdAt)
                        """)
                .bind("id", extraction.psiId().value())
                .bind("field", field.name())
                .bind("value", toJson(extraction.facts(field)))
                .bind("model", extraction.model())
                .bind("version", extraction.promptVersion())
                .bind("createdAt", extraction.extractedAt().atOffset(java.time.ZoneOffset.UTC))
                .fetch().rowsUpdated());
        return delete.thenMany(inserts).then().as(transactions::transactional);
    }

    @Override
    public Mono<Boolean> exists(PsiId psiId, String model, String promptVersion) {
        return db.sql("""
                        SELECT count(*) AS n FROM ai_extraction
                        WHERE investigation_id = :id AND model = :model AND prompt_version = :version
                        """)
                .bind("id", psiId.value())
                .bind("model", model)
                .bind("version", promptVersion)
                .map(row -> row.get("n", Long.class))
                .one()
                .map(n -> n != null && n >= ExtractionField.values().length);
    }

    @Override
    public Mono<InvestigationExtraction> find(PsiId psiId) {
        return db.sql("""
                        SELECT field, value, model, prompt_version, created_at FROM ai_extraction
                        WHERE investigation_id = :id ORDER BY id
                        """)
                .bind("id", psiId.value())
                .map(row -> new StoredRow(
                        ExtractionField.valueOf(row.get("field", String.class)),
                        row.get("value", Json.class).asString(),
                        row.get("model", String.class),
                        row.get("prompt_version", String.class),
                        row.get("created_at", OffsetDateTime.class).toInstant()))
                .all()
                .collectList()
                .flatMap(rows -> rows.isEmpty() ? Mono.empty() : Mono.just(toExtraction(psiId, rows)));
    }

    private record StoredRow(ExtractionField field, String value, String model, String promptVersion,
                             Instant createdAt) {
    }

    private InvestigationExtraction toExtraction(PsiId psiId, List<StoredRow> rows) {
        List<ExtractedFact> facts = new ArrayList<>();
        for (StoredRow row : rows) {
            readTree(row.value()).forEach(node -> facts.add(new ExtractedFact(row.field(),
                    node.path("label").asText(""), node.path("detail").asText(""),
                    node.path("source_span").asText(""))));
        }
        StoredRow first = rows.get(0);
        return new InvestigationExtraction(psiId, first.model(), first.promptVersion(), first.createdAt(), facts);
    }

    private String toJson(List<ExtractedFact> facts) {
        ArrayNode array = objectMapper.createArrayNode();
        facts.forEach(f -> array.addObject()
                .put("label", f.label())
                .put("detail", f.detail())
                .put("source_span", f.sourceSpan()));
        try {
            return objectMapper.writeValueAsString(array);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private JsonNode readTree(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
