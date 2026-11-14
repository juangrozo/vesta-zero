package com.ignis.vesta.infrastructure.persistence;

import com.ignis.vesta.domain.catalog.CatalogEntry;
import com.ignis.vesta.domain.catalog.FuelState;
import com.ignis.vesta.domain.catalog.HardwareFamily;
import com.ignis.vesta.domain.catalog.SafetyPillar;
import com.ignis.vesta.domain.model.InvestigationSummary;
import com.ignis.vesta.domain.port.InvestigationCatalogReader;
import com.ignis.vesta.domain.vo.Doi;
import com.ignis.vesta.domain.vo.PsiId;
import io.r2dbc.spi.Readable;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;

/** One aggregate query: investigations with experimental-table and test-row counts. */
public final class R2dbcInvestigationCatalogReader implements InvestigationCatalogReader {

    private static final String SELECT_SUMMARIES = """
            SELECT i.psi_id, i.acronym, i.title, i.doi, i.project_type, i.sub_area, i.hardware,
                   i.hardware_family, i.fuel_state, i.safety_pillar,
                   jsonb_array_length(COALESCE(i.raw_json -> 'experimentalTable', '[]'::jsonb)) AS table_count,
                   COUNT(t.id) AS test_count,
                   COUNT(t.id) FILTER (WHERE t.o2_min IS NOT NULL OR t.pressure_atm_min IS NOT NULL
                                          OR t.flow_cm_s_min IS NOT NULL) AS normalized_count
            FROM investigation i
            LEFT JOIN test_condition t ON t.investigation_id = i.psi_id
            GROUP BY i.psi_id
            """;

    private final DatabaseClient db;

    public R2dbcInvestigationCatalogReader(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Flux<InvestigationSummary> findAllSummaries() {
        return db.sql(SELECT_SUMMARIES).map(R2dbcInvestigationCatalogReader::toSummary).all();
    }

    private static InvestigationSummary toSummary(Readable row) {
        PsiId id = PsiId.of(row.get("psi_id", String.class));
        CatalogEntry classification = new CatalogEntry(id,
                HardwareFamily.valueOf(row.get("hardware_family", String.class)),
                FuelState.valueOf(row.get("fuel_state", String.class)),
                SafetyPillar.valueOf(row.get("safety_pillar", String.class)));
        return new InvestigationSummary(
                id,
                row.get("acronym", String.class),
                row.get("title", String.class),
                new Doi(row.get("doi", String.class)),
                row.get("project_type", String.class),
                row.get("sub_area", String.class),
                row.get("hardware", String.class),
                classification,
                number(row.get("table_count", Number.class)).intValue(),
                number(row.get("test_count", Number.class)).longValue(),
                number(row.get("normalized_count", Number.class)).longValue());
    }

    private static Number number(Number value) {
        return value == null ? 0 : value;
    }
}
