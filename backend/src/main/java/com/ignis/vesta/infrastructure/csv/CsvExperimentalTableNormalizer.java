package com.ignis.vesta.infrastructure.csv;

import com.ignis.vesta.domain.model.IngestionWarning;
import com.ignis.vesta.domain.model.TableNormalization;
import com.ignis.vesta.domain.port.ExperimentalTableNormalizer;
import com.ignis.vesta.domain.vo.PsiId;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry of table parsers: picks the curated parser for a file, or the raw-only fallback.
 * New schemas are added as new mappings without touching this class.
 */
public final class CsvExperimentalTableNormalizer implements ExperimentalTableNormalizer {

    private final List<TableParser> parsers;
    private final TableParser fallback = new RawOnlyTableParser();

    public CsvExperimentalTableNormalizer() {
        this(PsiTableMappings.all());
    }

    CsvExperimentalTableNormalizer(List<TableMapping> mappings) {
        this.parsers = mappings.stream().<TableParser>map(MappedTableParser::new).toList();
    }

    @Override
    public TableNormalization normalize(PsiId psiId, String fileName, byte[] content) {
        CsvTable table;
        try {
            table = CsvTableReader.read(content);
        } catch (RuntimeException e) {
            return new TableNormalization(fileName, "none", List.of(),
                    List.of(IngestionWarning.forFile(psiId, fileName, "unreadable CSV: " + e.getMessage())));
        }
        TableParser parser = parsers.stream().filter(p -> p.supports(fileName)).findFirst().orElse(fallback);
        TableNormalization result = parser.parse(psiId, fileName, table);
        if (table.rows().isEmpty()) {
            List<IngestionWarning> warnings = new ArrayList<>(result.warnings());
            warnings.add(IngestionWarning.forFile(psiId, fileName, "table has no data rows"));
            return new TableNormalization(fileName, result.parserName(), result.conditions(), warnings);
        }
        return result;
    }
}
