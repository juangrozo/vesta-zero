package com.ignis.vesta.domain.model;

import com.ignis.vesta.domain.vo.NumericRange;
import com.ignis.vesta.domain.vo.PsiId;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * One row of an experimental table. Normalized values are documented values converted to common units
 * (O2 as mole fraction, pressure in atm, flow in cm/s). {@code rawRow} keeps the original cells.
 */
public record TestCondition(
        PsiId investigationId,
        String sourceFile,
        int rowIndex,
        String testLabel,
        String fuelOrMaterial,
        NumericRange o2Fraction,
        NumericRange pressureAtm,
        NumericRange flowCmPerS,
        Map<String, String> rawRow) {

    public TestCondition {
        Objects.requireNonNull(investigationId);
        Objects.requireNonNull(sourceFile);
        rawRow = Collections.unmodifiableMap(new LinkedHashMap<>(rawRow));
    }

    public Optional<NumericRange> o2() {
        return Optional.ofNullable(o2Fraction);
    }

    public Optional<NumericRange> pressure() {
        return Optional.ofNullable(pressureAtm);
    }

    public Optional<NumericRange> flow() {
        return Optional.ofNullable(flowCmPerS);
    }

    public boolean hasNormalizedConditions() {
        return o2Fraction != null || pressureAtm != null || flowCmPerS != null;
    }
}
