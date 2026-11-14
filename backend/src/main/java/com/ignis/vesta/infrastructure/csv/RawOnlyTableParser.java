package com.ignis.vesta.infrastructure.csv;

import com.ignis.vesta.domain.model.IngestionWarning;
import com.ignis.vesta.domain.model.TableNormalization;
import com.ignis.vesta.domain.model.TestCondition;
import com.ignis.vesta.domain.vo.PsiId;

import java.util.ArrayList;
import java.util.List;

/** Fallback for files without a curated mapping: keeps every row raw and normalizes nothing. */
final class RawOnlyTableParser implements TableParser {

    @Override
    public String name() {
        return "raw-only";
    }

    @Override
    public boolean supports(String fileName) {
        return true;
    }

    @Override
    public TableNormalization parse(PsiId psiId, String fileName, CsvTable table) {
        List<TestCondition> conditions = new ArrayList<>();
        for (int i = 0; i < table.rows().size(); i++) {
            conditions.add(new TestCondition(psiId, fileName, i + 1, null, null, null, null, null, table.rows().get(i)));
        }
        return new TableNormalization(fileName, name(), conditions, List.of(
                IngestionWarning.forFile(psiId, fileName, "no curated mapping; rows stored raw only")));
    }
}
