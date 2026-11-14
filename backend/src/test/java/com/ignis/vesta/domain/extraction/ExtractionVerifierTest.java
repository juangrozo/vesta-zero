package com.ignis.vesta.domain.extraction;

import com.ignis.vesta.domain.vo.PsiId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExtractionVerifierTest {

    private static final ExtractionSource SOURCE = new ExtractionSource(PsiId.of("PSI-69"), """
            Approach:
            Free droplets of n-heptane with initial diameters
            from 1.5 to 5 mm burn in O2/N2 mixtures; the CO2 diluent fraction is varied.

            Experimental table columns: Test # | Initial O2 (% vol) | Radiometer gain""");

    private static ExtractedFact fact(ExtractionField field, String label, String detail, String span) {
        return new ExtractedFact(field, label, detail, span);
    }

    @Test
    void acceptsSpansThatDifferOnlyInWhitespaceCaseAndDashes() {
        var result = ExtractionVerifier.verify(List.of(fact(ExtractionField.FUELS_AND_SAMPLES, "n-Heptane droplets",
                "1.5 to 5 mm initial diameter",
                "Free droplets of N-heptane with initial diameters from 1.5 to 5 mm")), SOURCE);
        assertThat(result.accepted()).hasSize(1);
        assertThat(result.rejected()).isEmpty();
    }

    @Test
    void acceptsColumnNamesAsSpansAndIgnoresChemicalFormulas() {
        var result = ExtractionVerifier.verify(List.of(
                fact(ExtractionField.MEASUREMENTS, "Radiometer", "", "Radiometer gain"),
                fact(ExtractionField.TEST_VARIABLES, "CO2 diluent fraction", "", "the CO2 diluent fraction is varied")),
                SOURCE);
        assertThat(result.accepted()).hasSize(2);
    }

    @Test
    void rejectsParaphrasedSpans() {
        var result = ExtractionVerifier.verify(List.of(fact(ExtractionField.FUELS_AND_SAMPLES, "Heptane", "",
                "heptane droplets of several sizes")), SOURCE);
        assertThat(result.accepted()).isEmpty();
        assertThat(result.rejected().get(0).reason()).isEqualTo("source span not found in the record");
    }

    @Test
    void rejectsNumbersThatAreNotInTheSpan() {
        var result = ExtractionVerifier.verify(List.of(fact(ExtractionField.FUELS_AND_SAMPLES, "n-heptane droplets",
                "1.5 to 6 mm", "Free droplets of n-heptane with initial diameters from 1.5 to 5 mm")), SOURCE);
        assertThat(result.accepted()).isEmpty();
        assertThat(result.rejected().get(0).reason()).isEqualTo("number 6 is not in the source span");
    }

    @Test
    void rejectsEmptySpansAndDuplicateItemsButKeepsSameLabelWithOtherDetail() {
        var result = ExtractionVerifier.verify(List.of(
                fact(ExtractionField.MEASUREMENTS, "Radiometer", "", ""),
                fact(ExtractionField.MEASUREMENTS, "Radiometer", "", "Radiometer gain"),
                fact(ExtractionField.MEASUREMENTS, "radiometer ", "", "Radiometer gain"),
                fact(ExtractionField.MEASUREMENTS, "Radiometer", "gain", "Radiometer gain")), SOURCE);
        assertThat(result.accepted()).hasSize(2);
        assertThat(result.rejected()).extracting(ExtractionVerifier.Rejection::reason)
                .containsExactly("no source span", "duplicate item");
    }
}
