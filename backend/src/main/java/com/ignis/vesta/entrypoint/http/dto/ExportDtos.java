package com.ignis.vesta.entrypoint.http.dto;

import com.ignis.vesta.domain.export.ExportedCondition;
import com.ignis.vesta.domain.export.TestConditionQuery;
import com.ignis.vesta.domain.ranking.HabitatEnvironment;
import com.ignis.vesta.domain.vo.NumericRange;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** JSON and CSV forms of the test-condition export. Values are documented; units are fixed per column. */
public final class ExportDtos {

    public static final String CITATION_NOTE = "Data from NASA Physical Sciences Informatics (PSI). Cite each "
            + "investigation by its DOI. Normalized values are documented values converted to O2 mole fraction, "
            + "atm and cm/s; original_row keeps the cells as published.";

    static final List<String> CSV_HEADER = List.of("psi_id", "acronym", "doi", "doi_url", "source_file",
            "row_index", "test_label", "fuel_or_material", "o2_fraction_min", "o2_fraction_max",
            "pressure_atm_min", "pressure_atm_max", "pressure_psi_min", "pressure_psi_max", "flow_cm_s_min",
            "flow_cm_s_max", "original_row", "provenance");

    private ExportDtos() {
    }

    public record FiltersDto(List<String> psiIds, Double minO2Percent, Double maxPressurePsi) {
        public static FiltersDto from(TestConditionQuery q) {
            return new FiltersDto(q.psiIds().stream().map(Object::toString).sorted().toList(),
                    q.minO2Fraction().map(v -> v * 100).orElse(null),
                    q.maxPressureAtm().map(v -> v * HabitatEnvironment.PSI_PER_ATM).orElse(null));
        }
    }

    public record RowDto(String psiId, String acronym, String doi, String doiUrl, String sourceFile, int rowIndex,
                         String testLabel, String fuelOrMaterial, RangeDto o2Fraction, RangeDto pressureAtm,
                         RangeDto flowCmPerS, Map<String, String> originalRow, String provenance) {
        public static RowDto from(ExportedCondition e) {
            var s = e.investigation();
            var t = e.condition();
            return new RowDto(s.psiId().value(), s.acronym(), s.doi().value(), s.doi().url(), t.sourceFile(),
                    t.rowIndex(), t.testLabel(), t.fuelOrMaterial(), RangeDto.from(t.o2()),
                    RangeDto.from(t.pressure()), RangeDto.from(t.flow()), t.rawRow(), "DOCUMENTED");
        }
    }

    public record ExportDto(String note, FiltersDto filters, int count, List<RowDto> rows) {
    }

    /** RFC 4180 CSV with a header row; original_row is a JSON object string. */
    public static String toCsv(List<ExportedCondition> rows, java.util.function.Function<Object, String> toJson) {
        StringWriter out = new StringWriter();
        try (CSVPrinter csv = new CSVPrinter(out, CSVFormat.RFC4180.builder()
                .setHeader(CSV_HEADER.toArray(String[]::new)).get())) {
            for (ExportedCondition e : rows) {
                var s = e.investigation();
                var t = e.condition();
                csv.printRecord(s.psiId().value(), s.acronym(), s.doi().value(), s.doi().url(), t.sourceFile(),
                        t.rowIndex(), t.testLabel(), t.fuelOrMaterial(),
                        min(t.o2()), max(t.o2()), min(t.pressure()), max(t.pressure()),
                        psi(min(t.pressure())), psi(max(t.pressure())), min(t.flow()), max(t.flow()),
                        toJson.apply(t.rawRow()), "DOCUMENTED");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toString();
    }

    private static Double min(Optional<NumericRange> r) {
        return r.map(NumericRange::min).orElse(null);
    }

    private static Double max(Optional<NumericRange> r) {
        return r.map(NumericRange::max).orElse(null);
    }

    private static Double psi(Double atm) {
        return atm == null ? null : Math.round(atm * HabitatEnvironment.PSI_PER_ATM * 1000.0) / 1000.0;
    }
}
