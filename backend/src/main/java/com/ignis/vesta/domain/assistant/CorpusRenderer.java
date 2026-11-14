package com.ignis.vesta.domain.assistant;

import com.ignis.vesta.domain.catalog.InvestigationType;
import com.ignis.vesta.domain.model.ConditionsSummary;
import com.ignis.vesta.domain.model.Investigation;
import com.ignis.vesta.domain.model.InvestigationDetail;
import com.ignis.vesta.domain.vo.NumericRange;
import com.ignis.vesta.domain.vo.PsiId;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Renders investigations as tagged plain-text records. Numbers appear exactly as the dashboard shows them
 * (O2 in %, pressure in atm and psi, flow in cm/s), so answers can be checked against this text.
 */
public final class CorpusRenderer {

    private static final int MAX_FUELS = 40;
    private static final double PSI_PER_ATM = 14.6959;

    private CorpusRenderer() {
    }

    public static EvidenceCorpus render(List<InvestigationDetail> details) {
        List<InvestigationDetail> ordered = details.stream()
                .sorted(Comparator.comparingInt(d -> d.investigation().psiId().number()))
                .toList();
        StringBuilder out = new StringBuilder();
        Set<PsiId> ids = new LinkedHashSet<>();
        for (InvestigationDetail detail : ordered) {
            ids.add(detail.investigation().psiId());
            renderOne(detail, out);
        }
        String text = out.toString();
        return new EvidenceCorpus(text, ids, sha256(text));
    }

    private static void renderOne(InvestigationDetail detail, StringBuilder out) {
        Investigation i = detail.investigation();
        ConditionsSummary c = detail.conditions();
        var cls = i.classification();
        out.append("<investigation id=\"").append(i.psiId()).append("\" doi=\"").append(i.doi()).append("\">\n");
        line(out, "Acronym", i.acronym());
        line(out, "Title", i.title());
        line(out, "Type", InvestigationType.fromProjectType(i.projectType()) == InvestigationType.GROUND
                ? "Ground study (models or analyzes PSI flight data)" : "Flight investigation in microgravity");
        line(out, "Research sub-area", i.subArea());
        line(out, "Hardware", i.hardware());
        line(out, "Flight program", i.flightProgram());
        line(out, "Curated classification (Vesta Zero, not PSI)", "hardware family " + cls.hardwareFamily()
                + "; fuel state " + cls.fuelState() + "; safety pillar " + cls.safetyPillar());
        line(out, "Objective", i.objective());
        line(out, "Approach", i.approach());
        line(out, "Hypothesis", i.hypothesis());
        line(out, "Research impacts / Earth benefits", i.researchImpacts());
        if (c.testRows() == 0) {
            line(out, "Experimental table", "none documented in PSI");
        } else {
            line(out, "Experimental table", c.testRows() + " test rows in " + c.sourceFiles().size()
                    + " file(s); " + c.normalizedRows() + " rows with normalized O2, pressure or flow");
            line(out, "Documented O2 range", c.o2Fraction().map(CorpusRenderer::percent).orElse("not documented"));
            line(out, "Documented pressure range", c.pressureAtm().map(CorpusRenderer::pressure)
                    .orElse("not documented"));
            line(out, "Documented flow velocity range", c.flowCmPerS().map(r -> span(r, 1, 2) + " cm/s")
                    .orElse("not documented"));
            List<String> fuels = c.fuelsOrMaterials();
            if (!fuels.isEmpty()) {
                String shown = String.join("; ", fuels.subList(0, Math.min(MAX_FUELS, fuels.size())));
                line(out, "Fuels or materials in the table", fuels.size() > MAX_FUELS
                        ? shown + "; and " + (fuels.size() - MAX_FUELS) + " more" : shown);
            }
        }
        out.append("</investigation>\n\n");
    }

    private static void line(StringBuilder out, String label, String value) {
        out.append(label).append(": ")
                .append(Optional.ofNullable(value).map(String::strip).filter(s -> !s.isEmpty()).orElse("not documented"))
                .append('\n');
    }

    private static String percent(NumericRange r) {
        return span(r, 100, 1) + " % O2";
    }

    private static String pressure(NumericRange r) {
        return span(r, 1, 3) + " atm (" + span(new NumericRange(r.min() * PSI_PER_ATM, r.max() * PSI_PER_ATM), 1, 1)
                + " psi)";
    }

    private static String span(NumericRange r, double factor, int digits) {
        String min = number(r.min() * factor, digits);
        String max = number(r.max() * factor, digits);
        return min.equals(max) ? min : min + " to " + max;
    }

    private static String number(double value, int digits) {
        return new java.math.BigDecimal(String.format(Locale.ROOT, "%." + digits + "f", value))
                .stripTrailingZeros().toPlainString();
    }

    private static String sha256(String text) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
