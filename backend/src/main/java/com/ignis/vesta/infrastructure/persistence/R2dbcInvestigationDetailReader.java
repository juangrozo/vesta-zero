package com.ignis.vesta.infrastructure.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ignis.vesta.domain.catalog.CatalogEntry;
import com.ignis.vesta.domain.catalog.FuelState;
import com.ignis.vesta.domain.catalog.HardwareFamily;
import com.ignis.vesta.domain.catalog.SafetyPillar;
import com.ignis.vesta.domain.model.ExperimentalTableRef;
import com.ignis.vesta.domain.model.IngestionWarning;
import com.ignis.vesta.domain.model.Investigation;
import com.ignis.vesta.domain.model.Publication;
import com.ignis.vesta.domain.model.TestCondition;
import com.ignis.vesta.domain.port.InvestigationDetailReader;
import com.ignis.vesta.domain.vo.Doi;
import com.ignis.vesta.domain.vo.NumericRange;
import com.ignis.vesta.domain.vo.PsiId;
import io.r2dbc.postgresql.codec.Json;
import io.r2dbc.spi.Readable;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** R2DBC adapter for the investigation detail read side. */
public final class R2dbcInvestigationDetailReader implements InvestigationDetailReader {

    private static final String SELECT_INVESTIGATION = """
            SELECT psi_id, acronym, title, doi, project_type, research_area, sub_area, flight_program, hardware,
                   hardware_family, fuel_state, safety_pillar, objective, approach, hypothesis, research_impacts,
                   start_date, end_date, publications, raw_json,
                   NULLIF(raw_json ->> 'licenseIdentifier', '') AS license
            FROM investigation WHERE psi_id = :id
            """;

    private static final String SELECT_CONDITIONS = """
            SELECT source_file, row_index, test_label, fuel_or_material, o2_min, o2_max,
                   pressure_atm_min, pressure_atm_max, flow_cm_s_min, flow_cm_s_max, raw_row
            FROM test_condition WHERE investigation_id = :id
            ORDER BY source_file, row_index
            """;

    private static final String SELECT_WARNINGS = """
            SELECT source_file, row_index, column_name, raw_value, reason
            FROM ingestion_warning WHERE investigation_id = :id
            ORDER BY source_file NULLS FIRST, row_index NULLS FIRST, id
            """;

    private static final TypeReference<LinkedHashMap<String, String>> ROW_TYPE = new TypeReference<>() {
    };

    private final DatabaseClient db;
    private final ObjectMapper objectMapper;

    public R2dbcInvestigationDetailReader(DatabaseClient db, ObjectMapper objectMapper) {
        this.db = db;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Investigation> findById(PsiId psiId) {
        return db.sql(SELECT_INVESTIGATION).bind("id", psiId.value()).map(this::toInvestigation).one();
    }

    @Override
    public Flux<TestCondition> findTestConditions(PsiId psiId) {
        return db.sql(SELECT_CONDITIONS).bind("id", psiId.value()).map(row -> toCondition(psiId, row)).all();
    }

    @Override
    public Flux<IngestionWarning> findWarnings(PsiId psiId) {
        return db.sql(SELECT_WARNINGS).bind("id", psiId.value())
                .map(row -> new IngestionWarning(psiId,
                        row.get("source_file", String.class),
                        row.get("row_index", Integer.class),
                        row.get("column_name", String.class),
                        row.get("raw_value", String.class),
                        row.get("reason", String.class)))
                .all();
    }

    private Investigation toInvestigation(Readable row) {
        PsiId id = PsiId.of(row.get("psi_id", String.class));
        String rawJson = json(row, "raw_json");
        return new Investigation(
                id,
                row.get("acronym", String.class),
                row.get("title", String.class),
                new Doi(row.get("doi", String.class)),
                row.get("project_type", String.class),
                row.get("research_area", String.class),
                row.get("sub_area", String.class),
                row.get("flight_program", String.class),
                row.get("hardware", String.class),
                row.get("objective", String.class),
                row.get("approach", String.class),
                row.get("hypothesis", String.class),
                row.get("research_impacts", String.class),
                row.get("start_date", LocalDate.class),
                row.get("end_date", LocalDate.class),
                row.get("license", String.class),
                publications(json(row, "publications")),
                experimentalTables(rawJson),
                new CatalogEntry(id,
                        HardwareFamily.valueOf(row.get("hardware_family", String.class)),
                        FuelState.valueOf(row.get("fuel_state", String.class)),
                        SafetyPillar.valueOf(row.get("safety_pillar", String.class))),
                rawJson);
    }

    private TestCondition toCondition(PsiId psiId, Readable row) {
        return new TestCondition(
                psiId,
                row.get("source_file", String.class),
                row.get("row_index", Integer.class),
                row.get("test_label", String.class),
                row.get("fuel_or_material", String.class),
                range(row, "o2_min", "o2_max"),
                range(row, "pressure_atm_min", "pressure_atm_max"),
                range(row, "flow_cm_s_min", "flow_cm_s_max"),
                parse(json(row, "raw_row"), ROW_TYPE));
    }

    private List<Publication> publications(String json) {
        List<Publication> publications = new ArrayList<>();
        tree(json).forEach(node -> publications.add(new Publication(
                node.path("title").asText(""), node.path("authors").asText(""), node.path("doi").asText(""))));
        return publications;
    }

    private List<ExperimentalTableRef> experimentalTables(String rawJson) {
        List<ExperimentalTableRef> tables = new ArrayList<>();
        tree(rawJson).path("experimentalTable").forEach(node -> tables.add(
                new ExperimentalTableRef(node.path("fileName").asText(), node.path("version").asInt(1))));
        return tables;
    }

    private static NumericRange range(Readable row, String minColumn, String maxColumn) {
        BigDecimal min = row.get(minColumn, BigDecimal.class);
        BigDecimal max = row.get(maxColumn, BigDecimal.class);
        return min == null || max == null ? null : new NumericRange(min.doubleValue(), max.doubleValue());
    }

    private static String json(Readable row, String column) {
        Json value = row.get(column, Json.class);
        return value == null ? "null" : value.asString();
    }

    private JsonNode tree(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (IOException e) {
            throw new UncheckedIOException("Stored JSON is invalid", e);
        }
    }

    private <T> T parse(String json, TypeReference<T> type) {
        try {
            T value = objectMapper.readValue(json, type);
            return value == null ? objectMapper.readValue("{}", type) : value;
        } catch (IOException e) {
            throw new UncheckedIOException("Stored JSON is invalid", e);
        }
    }
}
