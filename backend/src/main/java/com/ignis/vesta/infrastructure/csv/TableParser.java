package com.ignis.vesta.infrastructure.csv;

import com.ignis.vesta.domain.model.TableNormalization;
import com.ignis.vesta.domain.vo.PsiId;

/** Strategy for one experimental-table schema. */
interface TableParser {

    String name();

    boolean supports(String fileName);

    TableNormalization parse(PsiId psiId, String fileName, CsvTable table);
}
