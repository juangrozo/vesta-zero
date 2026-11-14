package com.ignis.vesta.domain.model;

/** A publication listed in a PSI record. {@code doi} may be empty when PSI does not provide one. */
public record Publication(String title, String authors, String doi) {
}
