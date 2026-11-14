package com.ignis.vesta.domain.extraction;

/** The summary fields the AI extracts from PSI text for the detail view (spec F2). */
public enum ExtractionField {
    /** Fuels, materials and samples, with form, shape and size when the record states them. */
    FUELS_AND_SAMPLES,
    /** Sensors, instruments and diagnostics, and the quantities they measure. */
    MEASUREMENTS,
    /** Parameters that change between the tests of the investigation. */
    TEST_VARIABLES
}
