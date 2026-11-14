package com.ignis.vesta.domain.normalization;

/** Physical quantities normalized from experimental tables, with their canonical unit and sanity bounds. */
public enum Quantity {
    /** Oxygen as mole fraction. */
    O2_FRACTION(0.0, 1.0),
    /** Pressure in atm. Zero is rejected: no combustion test runs in vacuum. */
    PRESSURE_ATM(1e-6, 10.0),
    /** Gas flow velocity in cm/s. */
    FLOW_CM_PER_S(0.0, 100.0);

    private final double lower;
    private final double upper;

    Quantity(double lower, double upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public double lower() {
        return lower;
    }

    public double upper() {
        return upper;
    }
}
