package com.ignis.vesta.domain.normalization;

import com.ignis.vesta.domain.vo.NumericRange;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses documented cell text into a canonical numeric range. Handles single values ({@code 0.21}),
 * units inside cells ({@code 20 cm/s}, {@code 1 atm}), approximations ({@code ~ 21.5}), ranges
 * ({@code 21.5 - 21.7}, {@code 30%-40%}) and lists ({@code 0.17; 0.21; 0.25}). Never guesses: values outside
 * the quantity's bounds reject the whole cell.
 */
public final class QuantityParser {

    // Unsigned on purpose: every normalized quantity is non-negative, and "-" is used as a range separator.
    private static final Pattern NUMBER = Pattern.compile("\\d*\\.?\\d+");
    // Chemical formulas carry digits that are not measurements: "~21% O2 with N2", "50% C2H4", "CO2".
    private static final Pattern CHEMICAL_FORMULA = Pattern.compile("[A-Za-z]+\\d[A-Za-z0-9]*");
    private static final Set<String> EMPTY_MARKERS = Set.of("", "-", "n/a", "na", "none", "?", "insignificant");

    private QuantityParser() {
    }

    public static ParsedValue parse(String cell, SourceUnit unit) {
        if (cell == null || EMPTY_MARKERS.contains(cell.trim().toLowerCase(Locale.ROOT))) {
            return new ParsedValue.NotDocumented();
        }
        List<Double> values = new ArrayList<>();
        Matcher matcher = NUMBER.matcher(CHEMICAL_FORMULA.matcher(cell).replaceAll(" "));
        while (matcher.find()) {
            values.add(unit.toCanonical(Double.parseDouble(matcher.group())));
        }
        if (values.isEmpty()) {
            return new ParsedValue.Rejected("no numeric value");
        }
        NumericRange range = NumericRange.spanning(values).orElseThrow();
        Quantity quantity = unit.quantity();
        if (!range.within(quantity.lower(), quantity.upper())) {
            return new ParsedValue.Rejected(String.format(Locale.ROOT,
                    "out of bounds for %s: %s..%s (allowed %s..%s)",
                    quantity, round(range.min()), round(range.max()), quantity.lower(), quantity.upper()));
        }
        return new ParsedValue.Value(range);
    }

    private static String round(double value) {
        return String.format(Locale.ROOT, "%.4g", value);
    }
}
