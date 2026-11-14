package com.ignis.vesta.domain.model;

import com.ignis.vesta.domain.vo.PsiId;

/** A value or file that could not be normalized. Never silently dropped: stored and shown as data quality. */
public record IngestionWarning(
        PsiId investigationId,
        String sourceFile,
        Integer rowIndex,
        String columnName,
        String rawValue,
        String reason) {

    public static IngestionWarning forFile(PsiId id, String file, String reason) {
        return new IngestionWarning(id, file, null, null, null, reason);
    }
}
