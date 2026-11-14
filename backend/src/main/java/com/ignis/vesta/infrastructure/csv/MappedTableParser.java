package com.ignis.vesta.infrastructure.csv;

import com.ignis.vesta.domain.model.IngestionWarning;
import com.ignis.vesta.domain.model.TableNormalization;
import com.ignis.vesta.domain.model.TestCondition;
import com.ignis.vesta.domain.normalization.ParsedValue;
import com.ignis.vesta.domain.normalization.Quantity;
import com.ignis.vesta.domain.normalization.QuantityParser;
import com.ignis.vesta.domain.vo.NumericRange;
import com.ignis.vesta.domain.vo.PsiId;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Parses a table using a curated {@link TableMapping}. Unmapped columns stay available in the raw row. */
final class MappedTableParser implements TableParser {

    private static final int MAX_LABEL_LENGTH = 128;

    private final TableMapping mapping;

    MappedTableParser(TableMapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public String name() {
        return "mapped:" + mapping.fileName();
    }

    @Override
    public boolean supports(String fileName) {
        return mapping.fileName().equals(fileName);
    }

    @Override
    public TableNormalization parse(PsiId psiId, String fileName, CsvTable table) {
        List<IngestionWarning> warnings = new ArrayList<>();
        Optional<String> labelHeader = resolve(table, mapping.labelColumn(), psiId, fileName, warnings);
        Optional<String> fuelHeader = resolve(table, mapping.fuelColumn(), psiId, fileName, warnings);
        List<ResolvedColumn> quantityColumns = new ArrayList<>();
        for (TableMapping.QuantityColumn column : mapping.quantities()) {
            resolve(table, column.header(), psiId, fileName, warnings)
                    .ifPresent(header -> quantityColumns.add(new ResolvedColumn(header, column)));
        }

        List<TestCondition> conditions = new ArrayList<>();
        for (int i = 0; i < table.rows().size(); i++) {
            Map<String, String> row = table.rows().get(i);
            int rowIndex = i + 1;
            Map<Quantity, NumericRange> values = new EnumMap<>(Quantity.class);
            for (ResolvedColumn column : quantityColumns) {
                String cell = row.get(column.header());
                ParsedValue parsed = QuantityParser.parse(cell, column.mapping().unit());
                switch (parsed) {
                    case ParsedValue.Value v -> values.merge(column.mapping().quantity(), v.range(), NumericRange::union);
                    case ParsedValue.Rejected r -> warnings.add(new IngestionWarning(
                            psiId, fileName, rowIndex, column.header(), cell, r.reason()));
                    case ParsedValue.NotDocumented ignored -> {
                    }
                }
            }
            conditions.add(new TestCondition(
                    psiId, fileName, rowIndex,
                    labelHeader.map(row::get).map(MappedTableParser::truncateLabel).orElse(null),
                    fuelHeader.map(row::get).filter(s -> !s.isBlank()).orElse(null),
                    values.get(Quantity.O2_FRACTION),
                    values.get(Quantity.PRESSURE_ATM),
                    values.get(Quantity.FLOW_CM_PER_S),
                    row));
        }
        return new TableNormalization(fileName, name(), conditions, warnings);
    }

    private static Optional<String> resolve(CsvTable table, String wanted, PsiId psiId, String fileName,
                                            List<IngestionWarning> warnings) {
        if (wanted == null) {
            return Optional.empty();
        }
        Optional<String> header = table.findHeader(wanted);
        if (header.isEmpty()) {
            warnings.add(new IngestionWarning(psiId, fileName, null, wanted, null,
                    "mapped column not found in file; mapping may be outdated"));
        }
        return header;
    }

    private static String truncateLabel(String label) {
        if (label == null || label.isBlank()) {
            return null;
        }
        return label.length() <= MAX_LABEL_LENGTH ? label : label.substring(0, MAX_LABEL_LENGTH);
    }

    private record ResolvedColumn(String header, TableMapping.QuantityColumn mapping) {
    }
}
