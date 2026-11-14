package com.ignis.vesta.domain.model;

/** Reference to an experimental-table CSV attached to a PSI investigation. */
public record ExperimentalTableRef(String fileName, int version) {
}
