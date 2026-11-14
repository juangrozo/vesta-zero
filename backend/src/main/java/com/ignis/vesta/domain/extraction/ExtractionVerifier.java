package com.ignis.vesta.domain.extraction;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Keeps only extracted facts that can be traced to the record. A fact is accepted when
 * <ul>
 *   <li>its label and source span are not blank,</li>
 *   <li>its source span is a passage of the source text (ignoring case, whitespace, quote and dash styles), and</li>
 *   <li>every number in its label and detail also appears in its source span.</li>
 * </ul>
 * Items repeating the label and detail of another item in the same field are dropped. Rejected facts are reported, never stored.
 */
public final class ExtractionVerifier {

    // Names with digits are identifiers, not measurements: CO2, C2H4, FLEX-2, SAFFIRE-III, PSI-25.
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z]+[-/]?\\d[A-Za-z0-9]*");
    private static final Pattern NUMBER = Pattern.compile("\\d*\\.?\\d+");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private ExtractionVerifier() {
    }

    public record Rejection(ExtractedFact fact, String reason) {
    }

    public record Result(List<ExtractedFact> accepted, List<Rejection> rejected) {
        public Result {
            accepted = List.copyOf(accepted);
            rejected = List.copyOf(rejected);
        }
    }

    public static Result verify(List<ExtractedFact> facts, ExtractionSource source) {
        String haystack = normalize(source.text());
        List<ExtractedFact> accepted = new ArrayList<>();
        List<Rejection> rejected = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (ExtractedFact fact : facts) {
            String reason = rejectionReason(fact, haystack);
            if (reason == null && !seen.add(fact.field() + "|" + normalize(fact.label()) + "|" + normalize(fact.detail()))) {
                reason = "duplicate item";
            }
            if (reason == null) {
                accepted.add(fact);
            } else {
                rejected.add(new Rejection(fact, reason));
            }
        }
        return new Result(accepted, rejected);
    }

    private static String rejectionReason(ExtractedFact fact, String haystack) {
        if (fact.label().isEmpty()) {
            return "empty label";
        }
        if (fact.sourceSpan().isEmpty()) {
            return "no source span";
        }
        if (!haystack.contains(normalize(fact.sourceSpan()))) {
            return "source span not found in the record";
        }
        Set<BigDecimal> spanNumbers = numbers(fact.sourceSpan());
        for (BigDecimal n : numbers(fact.label() + " " + fact.detail())) {
            if (!spanNumbers.contains(n)) {
                return "number " + n.toPlainString() + " is not in the source span";
            }
        }
        return null;
    }

    static String normalize(String text) {
        String s = Normalizer.normalize(text, Normalizer.Form.NFKC)
                .replace('‘', '\'').replace('’', '\'')
                .replace('“', '"').replace('”', '"')
                .replace('–', '-').replace('—', '-').replace('−', '-');
        return WHITESPACE.matcher(s).replaceAll(" ").strip().toLowerCase(Locale.ROOT);
    }

    private static Set<BigDecimal> numbers(String text) {
        Set<BigDecimal> values = new HashSet<>();
        Matcher m = NUMBER.matcher(IDENTIFIER.matcher(normalize(text)).replaceAll(" "));
        while (m.find()) {
            values.add(new BigDecimal(m.group()).stripTrailingZeros());
        }
        return values;
    }
}
