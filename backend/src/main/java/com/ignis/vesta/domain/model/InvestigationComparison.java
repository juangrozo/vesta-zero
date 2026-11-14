package com.ignis.vesta.domain.model;

import com.ignis.vesta.domain.vo.NumericRange;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Deterministic comparison of 2–3 investigations (spec F4): which documented or curated aspects they share and
 * where their tested envelopes overlap. Facts are computed only from stored data, never by the language model.
 */
public record InvestigationComparison(List<InvestigationDetail> investigations, List<Fact> facts) {

    public static final int MIN_ITEMS = 2;
    public static final int MAX_ITEMS = 3;

    /** One comparison dimension: whether all investigations agree, and a human-readable detail. */
    public record Fact(String dimension, boolean same, String detail) {
    }

    public InvestigationComparison {
        if (investigations.size() < MIN_ITEMS || investigations.size() > MAX_ITEMS) {
            throw new IllegalArgumentException("Compare between " + MIN_ITEMS + " and " + MAX_ITEMS + " investigations");
        }
        investigations = List.copyOf(investigations);
        facts = List.copyOf(facts);
    }

    public static InvestigationComparison of(List<InvestigationDetail> details) {
        List<Fact> facts = new ArrayList<>();
        facts.add(same(details, "Investigation type", d -> d.investigation().projectType()));
        facts.add(same(details, "Hardware family (curated)", d -> d.investigation().classification().hardwareFamily().name()));
        facts.add(same(details, "Fuel state (curated)", d -> d.investigation().classification().fuelState().name()));
        facts.add(same(details, "Safety pillar (curated)", d -> d.investigation().classification().safetyPillar().name()));
        facts.add(overlap(details, "Oxygen range", d -> d.conditions().o2Fraction(), 100, "% O2", 1));
        facts.add(overlap(details, "Pressure range", d -> d.conditions().pressureAtm(), 1, "atm", 3));
        facts.add(overlap(details, "Flow velocity range", d -> d.conditions().flowCmPerS(), 1, "cm/s", 2));
        facts.add(commonFuels(details));
        return new InvestigationComparison(details, facts);
    }

    private static Fact same(List<InvestigationDetail> details, String dimension,
                             Function<InvestigationDetail, String> value) {
        Set<String> values = details.stream().map(value).map(v -> v == null ? "not documented" : v)
                .collect(Collectors.toCollection(TreeSet::new));
        return values.size() == 1
                ? new Fact(dimension, true, "All: " + values.iterator().next())
                : new Fact(dimension, false, "Differs: " + String.join(" / ", values));
    }

    private static Fact overlap(List<InvestigationDetail> details, String dimension,
                                Function<InvestigationDetail, Optional<NumericRange>> range, double factor,
                                String unit, int digits) {
        List<Optional<NumericRange>> ranges = details.stream().map(range).toList();
        long documented = ranges.stream().filter(Optional::isPresent).count();
        if (documented < details.size()) {
            return new Fact(dimension, false, documented == 0 ? "Not documented for any of them"
                    : "Documented for " + documented + " of " + details.size() + " only; no common range");
        }
        double low = ranges.stream().mapToDouble(r -> r.orElseThrow().min()).max().orElseThrow();
        double high = ranges.stream().mapToDouble(r -> r.orElseThrow().max()).min().orElseThrow();
        if (low > high) {
            return new Fact(dimension, false, "Tested ranges do not overlap");
        }
        return new Fact(dimension, true, "All tested within " + format(low * factor, digits) + " to "
                + format(high * factor, digits) + " " + unit);
    }

    private static Fact commonFuels(List<InvestigationDetail> details) {
        List<Set<String>> sets = details.stream()
                .map(d -> d.conditions().fuelsOrMaterials().stream().map(f -> f.toLowerCase(Locale.ROOT))
                        .collect(Collectors.toCollection(TreeSet::new)))
                .map(s -> (Set<String>) s)
                .toList();
        if (sets.stream().anyMatch(Set::isEmpty)) {
            return new Fact("Fuels or materials in common", false, "Not documented in every experimental table");
        }
        Set<String> common = new TreeSet<>(sets.getFirst());
        sets.forEach(common::retainAll);
        return common.isEmpty()
                ? new Fact("Fuels or materials in common", false, "None named identically in the tables")
                : new Fact("Fuels or materials in common", true, String.join("; ", common));
    }

    private static String format(double value, int digits) {
        return new java.math.BigDecimal(String.format(Locale.ROOT, "%." + digits + "f", value))
                .stripTrailingZeros().toPlainString();
    }
}
