package com.ignis.vesta.entrypoint.http.dto;

import com.ignis.vesta.domain.model.TestCondition;

import java.util.Map;

/** One experimental-table row: normalized values (O2 fraction, atm, cm/s) plus the original cells. */
public record TestConditionDto(
        String sourceFile,
        int rowIndex,
        String testLabel,
        String fuelOrMaterial,
        RangeDto o2Fraction,
        RangeDto pressureAtm,
        RangeDto flowCmPerS,
        Map<String, String> raw) {

    public static TestConditionDto from(TestCondition t) {
        return new TestConditionDto(t.sourceFile(), t.rowIndex(), t.testLabel(), t.fuelOrMaterial(),
                RangeDto.from(t.o2()), RangeDto.from(t.pressure()), RangeDto.from(t.flow()), t.rawRow());
    }
}
