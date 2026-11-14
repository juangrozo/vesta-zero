package com.ignis.vesta.domain.normalization;

import java.util.function.DoubleUnaryOperator;

/** Unit a column is documented in, with the conversion to the canonical unit of its {@link Quantity}. */
public enum SourceUnit {
    FRACTION(Quantity.O2_FRACTION, v -> v),
    PERCENT(Quantity.O2_FRACTION, v -> v / 100.0),
    ATM(Quantity.PRESSURE_ATM, v -> v),
    BAR(Quantity.PRESSURE_ATM, v -> v / 1.01325),
    MMHG(Quantity.PRESSURE_ATM, v -> v / 760.0),
    CM_PER_S(Quantity.FLOW_CM_PER_S, v -> v);

    private final Quantity quantity;
    private final DoubleUnaryOperator toCanonical;

    SourceUnit(Quantity quantity, DoubleUnaryOperator toCanonical) {
        this.quantity = quantity;
        this.toCanonical = toCanonical;
    }

    public Quantity quantity() {
        return quantity;
    }

    public double toCanonical(double value) {
        return toCanonical.applyAsDouble(value);
    }
}
