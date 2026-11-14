package com.ignis.vesta.domain.normalization;

import com.ignis.vesta.domain.vo.NumericRange;

import java.util.Optional;

/** Outcome of parsing one cell: a range, nothing documented, or a rejection with its reason. */
public sealed interface ParsedValue {

    record Value(NumericRange range) implements ParsedValue {
    }

    record NotDocumented() implements ParsedValue {
    }

    record Rejected(String reason) implements ParsedValue {
    }

    default Optional<NumericRange> asRange() {
        return this instanceof Value v ? Optional.of(v.range()) : Optional.empty();
    }
}
