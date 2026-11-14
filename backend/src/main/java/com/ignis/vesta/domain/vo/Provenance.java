package com.ignis.vesta.domain.vo;

/** Where a displayed value comes from. Shown as a badge next to every value in the UI. */
public enum Provenance {
    /** Taken from a PSI JSON field or experimental-table CSV. */
    DOCUMENTED,
    /** Classified by Vesta Zero from PSI metadata and the challenge resources (versioned in code). */
    CURATED,
    /** Interpreted from PSI text by the LLM; always stored with its source span. */
    AI_EXTRACTED
}
