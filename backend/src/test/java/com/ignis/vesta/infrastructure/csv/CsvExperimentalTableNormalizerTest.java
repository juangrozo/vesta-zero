package com.ignis.vesta.infrastructure.csv;

import com.ignis.vesta.domain.model.TableNormalization;
import com.ignis.vesta.domain.model.TestCondition;
import com.ignis.vesta.domain.vo.PsiId;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/** Uses real PSI experimental tables (CC0-1.0) copied into test resources. */
class CsvExperimentalTableNormalizerTest {

    private final CsvExperimentalTableNormalizer normalizer = new CsvExperimentalTableNormalizer();

    @Test
    void flexCp1252FileIsDecodedAndPressureConvertedFromMmHg() throws IOException {
        String file = "PSI-69_Experimental table_FLEX.csv";
        TableNormalization result = normalizer.normalize(PsiId.of("PSI-69"), file, fixture(file));

        assertThat(result.parserName()).startsWith("mapped:");
        assertThat(result.conditions()).hasSize(274);
        TestCondition first = result.conditions().getFirst();
        assertThat(first.testLabel()).isEqualTo("1");
        assertThat(first.fuelOrMaterial()).isEqualTo("Methanol");
        assertThat(first.o2().orElseThrow().min()).isCloseTo(0.21, within(1e-9));
        assertThat(first.pressure().orElseThrow().min()).isCloseTo(757.7 / 760.0, within(1e-9));
    }

    @Test
    void flexZeroPressureIsRejectedWithWarningNotStored() throws IOException {
        String file = "PSI-69_Experimental table_FLEX.csv";
        TableNormalization result = normalizer.normalize(PsiId.of("PSI-69"), file, fixture(file));

        assertThat(result.conditions().get(113).pressure()).isEmpty();
        assertThat(result.warnings())
                .anySatisfy(w -> {
                    assertThat(w.rowIndex()).isEqualTo(114);
                    assertThat(w.columnName()).isEqualTo("Ambient pressure; mmHg");
                    assertThat(w.reason()).contains("out of bounds");
                });
    }

    @Test
    void sFlameAtmosphereTextIgnoresChemicalFormulas() throws IOException {
        String file = "PSI-23_Experimental table_ACME s-Flame.csv";
        TableNormalization result = normalizer.normalize(PsiId.of("PSI-23"), file, fixture(file));

        assertThat(result.conditions()).isNotEmpty();
        assertThat(result.conditions())
                .allSatisfy(c -> assertThat(c.o2()).hasValueSatisfying(r -> {
                    assertThat(r.min()).isGreaterThanOrEqualTo(0.21);
                    assertThat(r.max()).isLessThanOrEqualTo(0.30);
                }));
    }

    @Test
    void saffireUnitsInsideCellsAreParsed() throws IOException {
        String file = "PSI-99_Experimental table_SAFFIRE-2.csv";
        TableNormalization result = normalizer.normalize(PsiId.of("PSI-99"), file, fixture(file));

        TestCondition first = result.conditions().getFirst();
        assertThat(first.fuelOrMaterial()).isEqualTo("Silicone");
        assertThat(first.flow().orElseThrow().max()).isCloseTo(20.0, within(1e-9));
        assertThat(first.rawRow()).containsEntry("Air Flow (cm/s)", "20 cm/s");
    }

    @Test
    void unknownFileFallsBackToRawOnlyStorage() {
        byte[] csv = "a,b\n1,2\n".getBytes(StandardCharsets.UTF_8);
        TableNormalization result = normalizer.normalize(PsiId.of("PSI-1"), "unknown.csv", csv);

        assertThat(result.parserName()).isEqualTo("raw-only");
        assertThat(result.conditions()).singleElement()
                .satisfies(c -> assertThat(c.hasNormalizedConditions()).isFalse());
        assertThat(result.warnings()).singleElement()
                .satisfies(w -> assertThat(w.reason()).contains("no curated mapping"));
    }

    private static byte[] fixture(String name) throws IOException {
        try (InputStream in = CsvExperimentalTableNormalizerTest.class.getResourceAsStream("/fixtures/" + name)) {
            assertThat(in).as("fixture %s", name).isNotNull();
            return in.readAllBytes();
        }
    }
}
