package com.ignis.vesta.domain.port;

import com.ignis.vesta.domain.model.TableNormalization;
import com.ignis.vesta.domain.vo.PsiId;

/** Turns a raw experimental-table file into normalized test conditions plus warnings. */
public interface ExperimentalTableNormalizer {

    TableNormalization normalize(PsiId psiId, String fileName, byte[] content);
}
