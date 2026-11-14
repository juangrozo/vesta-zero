package com.ignis.vesta.domain.assistant;

import com.ignis.vesta.domain.vo.PsiId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks a generated answer against the corpus it was given.
 * <ul>
 *   <li>Citations: inline {@code [PSI-n]} markers plus the declared list; IDs not in the corpus are dropped.</li>
 *   <li>Numbers: every number in the answer must appear in the corpus text (percent and fraction forms of the same
 *       value both count). Small integers below 10 are ignored (counts such as "three investigations").</li>
 * </ul>
 */
public final class AnswerGrounding {

    private static final Pattern INLINE_CITATION = Pattern.compile("\\[(PSI-\\d{1,5})]");
    private static final Pattern PSI_REFERENCE = Pattern.compile("PSI-\\d{1,5}");
    // Identifiers with digits are names, not measurements: FLEX-2, SAFFIRE-III, CO2, C2H4, 10.60555/xyz (DOIs).
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z]+[-/]?\\d[A-Za-z0-9./-]*|10\\.\\d{4,}/\\S+");
    private static final Pattern NUMBER = Pattern.compile("\\d+(?:\\.\\d+)?");

    private AnswerGrounding() {
    }

    public static AssistantAnswer ground(GeneratedAnswer generated, EvidenceCorpus corpus, String question) {
        Set<String> candidates = new LinkedHashSet<>(generated.citedPsiIds());
        Matcher inline = INLINE_CITATION.matcher(generated.text());
        while (inline.find()) {
            candidates.add(inline.group(1));
        }
        List<PsiId> citations = new ArrayList<>();
        List<String> dropped = new ArrayList<>();
        for (String candidate : candidates) {
            PsiId id = parse(candidate);
            if (id != null && corpus.psiIds().contains(id)) {
                citations.add(id);
            } else {
                dropped.add(candidate);
            }
        }
        citations.sort(java.util.Comparator.comparingInt(PsiId::number));
        boolean partialGravity = PartialGravityTopic.mentionedIn(question);
        return new AssistantAnswer(generated.text(), citations, dropped,
                generated.declined() ? List.of() : unverifiedNumbers(generated.text(), corpus.text()),
                generated.insufficientData(), generated.declined(), partialGravity, generated.model(),
                generated.fromCache());
    }

    static List<String> unverifiedNumbers(String answer, String corpusText) {
        Set<BigDecimal> known = numbersIn(corpusText, true);
        List<String> unverified = new ArrayList<>();
        Matcher m = NUMBER.matcher(strip(answer));
        Set<String> seen = new HashSet<>();
        while (m.find()) {
            String token = m.group();
            BigDecimal value = new BigDecimal(token).stripTrailingZeros();
            boolean smallCount = value.scale() <= 0 && value.compareTo(BigDecimal.TEN) < 0;
            if (!smallCount && !known.contains(value) && seen.add(token)) {
                unverified.add(token);
            }
        }
        return unverified;
    }

    private static Set<BigDecimal> numbersIn(String text, boolean withPercentForms) {
        Set<BigDecimal> values = new HashSet<>();
        Matcher m = NUMBER.matcher(strip(text));
        while (m.find()) {
            BigDecimal value = new BigDecimal(m.group()).stripTrailingZeros();
            values.add(value);
            if (withPercentForms) {
                values.add(value.movePointLeft(2).stripTrailingZeros());
                values.add(value.movePointRight(2).stripTrailingZeros());
            }
        }
        return values;
    }

    private static String strip(String text) {
        String withoutIds = PSI_REFERENCE.matcher(text).replaceAll(" ");
        return IDENTIFIER.matcher(withoutIds).replaceAll(" ");
    }

    private static PsiId parse(String candidate) {
        try {
            return PsiId.of(candidate.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
