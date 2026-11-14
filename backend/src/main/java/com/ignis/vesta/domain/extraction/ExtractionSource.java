package com.ignis.vesta.domain.extraction;

import com.ignis.vesta.domain.model.Investigation;
import com.ignis.vesta.domain.vo.PsiId;

import java.util.List;
import java.util.Objects;

/**
 * The PSI text the model reads for one investigation: documented text fields, experimental table column names
 * and the fuels listed in the tables. Every extracted source span must be a passage of {@link #text()}.
 */
public record ExtractionSource(PsiId psiId, String text) {

    /** Long material lists add little and cost tokens; the full list stays visible as documented data. */
    static final int MAX_FUELS = 40;

    public ExtractionSource {
        Objects.requireNonNull(psiId);
        Objects.requireNonNull(text);
    }

    public static ExtractionSource of(Investigation investigation, List<String> tableColumns, List<String> fuels) {
        StringBuilder text = new StringBuilder();
        line(text, "Title", investigation.title());
        line(text, "Hardware", investigation.hardware());
        block(text, "Objective", investigation.objective());
        block(text, "Approach", investigation.approach());
        block(text, "Hypothesis", investigation.hypothesis());
        block(text, "Research impacts", investigation.researchImpacts());
        if (!tableColumns.isEmpty()) {
            line(text, "Experimental table columns", String.join(" | ", tableColumns));
        }
        if (!fuels.isEmpty()) {
            List<String> shown = fuels.stream().limit(MAX_FUELS).toList();
            line(text, "Fuels and materials listed in the experimental tables", String.join("; ", shown));
        }
        return new ExtractionSource(investigation.psiId(), text.toString().strip());
    }

    private static void line(StringBuilder text, String label, String value) {
        if (value != null && !value.isBlank()) {
            text.append(label).append(": ").append(value.strip()).append("\n\n");
        }
    }

    private static void block(StringBuilder text, String label, String value) {
        if (value != null && !value.isBlank()) {
            text.append(label).append(":\n").append(value.strip()).append("\n\n");
        }
    }
}
