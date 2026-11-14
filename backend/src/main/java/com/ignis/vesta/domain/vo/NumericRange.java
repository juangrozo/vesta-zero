package com.ignis.vesta.domain.vo;

import java.util.Collection;
import java.util.Optional;

/** Closed numeric interval. A single documented value is a range with {@code min == max}. */
public record NumericRange(double min, double max) {

    public NumericRange {
        if (Double.isNaN(min) || Double.isNaN(max)) {
            throw new IllegalArgumentException("Range bounds must be numbers");
        }
        if (min > max) {
            throw new IllegalArgumentException("Range min " + min + " is greater than max " + max);
        }
    }

    public static NumericRange of(double value) {
        return new NumericRange(value, value);
    }

    public static Optional<NumericRange> spanning(Collection<Double> values) {
        if (values.isEmpty()) {
            return Optional.empty();
        }
        double min = values.stream().mapToDouble(Double::doubleValue).min().orElseThrow();
        double max = values.stream().mapToDouble(Double::doubleValue).max().orElseThrow();
        return Optional.of(new NumericRange(min, max));
    }

    public NumericRange union(NumericRange other) {
        return new NumericRange(Math.min(min, other.min), Math.max(max, other.max));
    }

    public boolean within(double lower, double upper) {
        return min >= lower && max <= upper;
    }
}
