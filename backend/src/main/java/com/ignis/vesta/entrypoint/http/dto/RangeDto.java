package com.ignis.vesta.entrypoint.http.dto;

import com.ignis.vesta.domain.vo.NumericRange;

import java.util.Optional;

/** Numeric range in canonical units; {@code null} in JSON means "not documented". */
public record RangeDto(double min, double max) {

    public static RangeDto from(NumericRange range) {
        return range == null ? null : new RangeDto(range.min(), range.max());
    }

    public static RangeDto from(Optional<NumericRange> range) {
        return range.map(RangeDto::from).orElse(null);
    }
}
