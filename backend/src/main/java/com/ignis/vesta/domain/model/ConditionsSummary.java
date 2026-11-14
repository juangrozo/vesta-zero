package com.ignis.vesta.domain.model;

import com.ignis.vesta.domain.vo.NumericRange;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Function;

/**
 * Aggregate of documented test conditions for one investigation: the tested envelope of O2, pressure and flow,
 * plus the fuels or materials named in the tables. Reused by detail, comparison and ranking.
 */
public record ConditionsSummary(
        int testRows,
        long normalizedRows,
        Optional<NumericRange> o2Fraction,
        Optional<NumericRange> pressureAtm,
        Optional<NumericRange> flowCmPerS,
        List<String> fuelsOrMaterials,
        List<String> sourceFiles) {

    public ConditionsSummary {
        fuelsOrMaterials = List.copyOf(fuelsOrMaterials);
        sourceFiles = List.copyOf(sourceFiles);
    }

    public static ConditionsSummary of(List<TestCondition> conditions) {
        return new ConditionsSummary(
                conditions.size(),
                conditions.stream().filter(TestCondition::hasNormalizedConditions).count(),
                envelope(conditions, TestCondition::o2),
                envelope(conditions, TestCondition::pressure),
                envelope(conditions, TestCondition::flow),
                distinct(conditions, TestCondition::fuelOrMaterial),
                distinct(conditions, TestCondition::sourceFile));
    }

    private static Optional<NumericRange> envelope(List<TestCondition> conditions,
                                                   Function<TestCondition, Optional<NumericRange>> value) {
        return conditions.stream().map(value).flatMap(Optional::stream).reduce(NumericRange::union);
    }

    private static List<String> distinct(List<TestCondition> conditions, Function<TestCondition, String> value) {
        return List.copyOf(conditions.stream().map(value).filter(Objects::nonNull)
                .map(s -> s.trim().replaceAll("\\s+", " "))
                .filter(s -> !s.isEmpty()).collect(java.util.stream.Collectors.toCollection(
                        () -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER))));
    }
}
