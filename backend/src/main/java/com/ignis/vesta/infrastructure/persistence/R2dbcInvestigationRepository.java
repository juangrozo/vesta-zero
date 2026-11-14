package com.ignis.vesta.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ignis.vesta.domain.model.IngestedInvestigation;
import com.ignis.vesta.domain.model.IngestionWarning;
import com.ignis.vesta.domain.model.Investigation;
import com.ignis.vesta.domain.model.TestCondition;
import com.ignis.vesta.domain.port.InvestigationRepository;
import com.ignis.vesta.domain.vo.NumericRange;
import io.r2dbc.postgresql.codec.Json;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;

/** R2DBC adapter. Ingestion is idempotent: each investigation is upserted and its child rows replaced. */
public final class R2dbcInvestigationRepository implements InvestigationRepository {

    private static final String UPSERT_INVESTIGATION = """
            INSERT INTO investigation (psi_id, acronym, title, doi, project_type, research_area, sub_area,
                flight_program, hardware, hardware_family, fuel_state, safety_pillar, objective, approach,
                hypothesis, research_impacts, start_date, end_date, publications, raw_json, ingested_at)
            VALUES (:psiId, :acronym, :title, :doi, :projectType, :researchArea, :subArea, :flightProgram,
                :hardware, :hardwareFamily, :fuelState, :safetyPillar, :objective, :approach, :hypothesis,
                :researchImpacts, :startDate, :endDate, :publications, :rawJson, now())
            ON CONFLICT (psi_id) DO UPDATE SET
                acronym = EXCLUDED.acronym, title = EXCLUDED.title, doi = EXCLUDED.doi,
                project_type = EXCLUDED.project_type, research_area = EXCLUDED.research_area,
                sub_area = EXCLUDED.sub_area, flight_program = EXCLUDED.flight_program,
                hardware = EXCLUDED.hardware, hardware_family = EXCLUDED.hardware_family,
                fuel_state = EXCLUDED.fuel_state, safety_pillar = EXCLUDED.safety_pillar,
                objective = EXCLUDED.objective, approach = EXCLUDED.approach, hypothesis = EXCLUDED.hypothesis,
                research_impacts = EXCLUDED.research_impacts, start_date = EXCLUDED.start_date,
                end_date = EXCLUDED.end_date, publications = EXCLUDED.publications,
                raw_json = EXCLUDED.raw_json, ingested_at = now()
            """;

    private static final String INSERT_CONDITION = """
            INSERT INTO test_condition (investigation_id, source_file, row_index, test_label, fuel_or_material,
                o2_min, o2_max, pressure_atm_min, pressure_atm_max, flow_cm_s_min, flow_cm_s_max, raw_row)
            VALUES (:investigationId, :sourceFile, :rowIndex, :testLabel, :fuel, :o2Min, :o2Max,
                :pMin, :pMax, :flowMin, :flowMax, CAST(:rawRow AS json))
            """;

    private static final String INSERT_WARNING = """
            INSERT INTO ingestion_warning (investigation_id, source_file, row_index, column_name, raw_value, reason)
            VALUES (:investigationId, :sourceFile, :rowIndex, :columnName, :rawValue, :reason)
            """;

    private final DatabaseClient db;
    private final TransactionalOperator transactions;
    private final ObjectMapper objectMapper;

    public R2dbcInvestigationRepository(DatabaseClient db, TransactionalOperator transactions, ObjectMapper objectMapper) {
        this.db = db;
        this.transactions = transactions;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> replace(IngestedInvestigation ingested) {
        String id = ingested.investigation().psiId().value();
        Mono<Void> work = upsert(ingested.investigation())
                .then(db.sql("DELETE FROM test_condition WHERE investigation_id = :id").bind("id", id).then())
                .then(db.sql("DELETE FROM ingestion_warning WHERE investigation_id = :id").bind("id", id).then())
                .thenMany(Flux.fromIterable(ingested.conditions()).concatMap(this::insertCondition))
                .thenMany(Flux.fromIterable(ingested.warnings()).concatMap(this::insertWarning))
                .then();
        return transactions.transactional(work);
    }

    private Mono<Void> upsert(Investigation inv) {
        var c = inv.classification();
        return new Binder(db.sql(UPSERT_INVESTIGATION))
                .bind("psiId", inv.psiId().value(), String.class)
                .bind("acronym", inv.acronym(), String.class)
                .bind("title", inv.title(), String.class)
                .bind("doi", inv.doi().value(), String.class)
                .bind("projectType", inv.projectType(), String.class)
                .bind("researchArea", inv.researchArea(), String.class)
                .bind("subArea", inv.subArea(), String.class)
                .bind("flightProgram", inv.flightProgram(), String.class)
                .bind("hardware", inv.hardware(), String.class)
                .bind("hardwareFamily", c.hardwareFamily().name(), String.class)
                .bind("fuelState", c.fuelState().name(), String.class)
                .bind("safetyPillar", c.safetyPillar().name(), String.class)
                .bind("objective", inv.objective(), String.class)
                .bind("approach", inv.approach(), String.class)
                .bind("hypothesis", inv.hypothesis(), String.class)
                .bind("researchImpacts", inv.researchImpacts(), String.class)
                .bind("startDate", inv.startDate(), java.time.LocalDate.class)
                .bind("endDate", inv.endDate(), java.time.LocalDate.class)
                .bind("publications", Json.of(toJson(inv.publications())), Json.class)
                .bind("rawJson", Json.of(inv.rawJson()), Json.class)
                .spec().then();
    }

    private Mono<Void> insertCondition(TestCondition t) {
        return new Binder(db.sql(INSERT_CONDITION))
                .bind("investigationId", t.investigationId().value(), String.class)
                .bind("sourceFile", t.sourceFile(), String.class)
                .bind("rowIndex", t.rowIndex(), Integer.class)
                .bind("testLabel", t.testLabel(), String.class)
                .bind("fuel", t.fuelOrMaterial(), String.class)
                .bind("o2Min", bound(t.o2(), NumericRange::min), BigDecimal.class)
                .bind("o2Max", bound(t.o2(), NumericRange::max), BigDecimal.class)
                .bind("pMin", bound(t.pressure(), NumericRange::min), BigDecimal.class)
                .bind("pMax", bound(t.pressure(), NumericRange::max), BigDecimal.class)
                .bind("flowMin", bound(t.flow(), NumericRange::min), BigDecimal.class)
                .bind("flowMax", bound(t.flow(), NumericRange::max), BigDecimal.class)
                // Sent as text and cast to json (not jsonb) so the CSV column order survives.
                .bind("rawRow", toJson(t.rawRow()), String.class)
                .spec().then();
    }

    private Mono<Void> insertWarning(IngestionWarning w) {
        return new Binder(db.sql(INSERT_WARNING))
                .bind("investigationId", w.investigationId().value(), String.class)
                .bind("sourceFile", w.sourceFile(), String.class)
                .bind("rowIndex", w.rowIndex(), Integer.class)
                .bind("columnName", w.columnName(), String.class)
                .bind("rawValue", w.rawValue(), String.class)
                .bind("reason", w.reason(), String.class)
                .spec().then();
    }

    private static BigDecimal bound(Optional<NumericRange> range, Function<NumericRange, Double> side) {
        return range.map(side).map(BigDecimal::valueOf).orElse(null);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize value to JSON", e);
        }
    }

    /** Small helper: binds a value or a typed null, keeping the statement code readable. */
    private record Binder(DatabaseClient.GenericExecuteSpec spec) {
        <T> Binder bind(String name, T value, Class<T> type) {
            return new Binder(value == null ? spec.bindNull(name, type) : spec.bind(name, value));
        }
    }
}
