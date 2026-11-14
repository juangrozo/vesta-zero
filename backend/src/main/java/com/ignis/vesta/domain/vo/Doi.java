package com.ignis.vesta.domain.vo;

import java.util.Objects;

/** Digital Object Identifier, e.g. {@code 10.60555/4qc4-de67}. */
public record Doi(String value) {

    public Doi {
        Objects.requireNonNull(value, "DOI must not be null");
        if (!value.startsWith("10.") || !value.contains("/")) {
            throw new IllegalArgumentException("Invalid DOI: " + value);
        }
    }

    public String url() {
        return "https://doi.org/" + value;
    }

    @Override
    public String toString() {
        return value;
    }
}
