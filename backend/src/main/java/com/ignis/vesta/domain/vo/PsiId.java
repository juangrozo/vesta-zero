package com.ignis.vesta.domain.vo;

import java.util.Objects;
import java.util.regex.Pattern;

/** PSI accession identifier, e.g. {@code PSI-25}. Validated on construction. */
public record PsiId(String value) {

    private static final Pattern FORMAT = Pattern.compile("^PSI-\\d{1,5}$");

    public PsiId {
        Objects.requireNonNull(value, "PSI id must not be null");
        if (!FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid PSI id: " + value);
        }
    }

    public static PsiId of(String value) {
        return new PsiId(value);
    }

    /** Numeric part, for natural ordering (PSI-10 before PSI-100). */
    public int number() {
        return Integer.parseInt(value.substring("PSI-".length()));
    }

    @Override
    public String toString() {
        return value;
    }
}
