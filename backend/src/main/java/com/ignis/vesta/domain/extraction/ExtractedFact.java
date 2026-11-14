package com.ignis.vesta.domain.extraction;

import java.util.Objects;

/**
 * One AI-extracted item. {@code sourceSpan} is the verbatim passage of the PSI record it comes from;
 * {@code detail} is empty when the record states nothing beyond the label.
 */
public record ExtractedFact(ExtractionField field, String label, String detail, String sourceSpan) {

    public ExtractedFact {
        Objects.requireNonNull(field);
        label = label == null ? "" : label.strip();
        detail = detail == null ? "" : detail.strip();
        sourceSpan = sourceSpan == null ? "" : sourceSpan.strip();
    }
}
