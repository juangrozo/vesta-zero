package com.ignis.vesta.domain.normalization;

import com.ignis.vesta.domain.vo.NumericRange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/** Cases are real cell values observed in PSI experimental tables. */
class QuantityParserTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "0.21                          | FRACTION | 0.21   | 0.21",
            "0.17; 0.21;0.25; 0.3          | FRACTION | 0.17   | 0.30",
            "21; 26.5; 34; 40              | PERCENT  | 0.21   | 0.40",
            "21.5 - 21.7                   | PERCENT  | 0.215  | 0.217",
            "~ 21.5                        | PERCENT  | 0.215  | 0.215",
            "22.0 to 21.9                  | PERCENT  | 0.219  | 0.220",
            "~21% O2 with N2               | PERCENT  | 0.21   | 0.21",
            "~30% and ~21% O2 with He      | PERCENT  | 0.21   | 0.30",
            "1 atm                         | ATM      | 1.0    | 1.0",
            "0.5; 1.0; 1.5; 2.0; or 3.0    | ATM      | 0.5    | 3.0",
            "760                           | MMHG     | 1.0    | 1.0",
            "1.01325                       | BAR      | 1.0    | 1.0",
            "20 cm/s                       | CM_PER_S | 20     | 20",
            "0.1 - 5                       | CM_PER_S | 0.1    | 5",
            ".33                           | CM_PER_S | 0.33   | 0.33"
    })
    void parsesDocumentedCells(String cell, SourceUnit unit, double min, double max) {
        NumericRange range = QuantityParser.parse(cell, unit).asRange().orElseThrow();
        assertThat(range.min()).isCloseTo(min, within(1e-9));
        assertThat(range.max()).isCloseTo(max, within(1e-9));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "-", "n/a", "Insignificant"})
    void emptyMarkersAreNotDocumented(String cell) {
        assertThat(QuantityParser.parse(cell, SourceUnit.PERCENT)).isInstanceOf(ParsedValue.NotDocumented.class);
    }

    @Test
    void nullIsNotDocumented() {
        assertThat(QuantityParser.parse(null, SourceUnit.ATM)).isInstanceOf(ParsedValue.NotDocumented.class);
    }

    @Test
    void rejectsOxygenFractionAboveOne() {
        assertThat(QuantityParser.parse("2", SourceUnit.FRACTION)).isInstanceOf(ParsedValue.Rejected.class);
    }

    @Test
    void rejectsZeroPressure() {
        assertThat(QuantityParser.parse("0", SourceUnit.MMHG)).isInstanceOf(ParsedValue.Rejected.class);
    }

    @Test
    void rejectsTextWithoutNumbers() {
        assertThat(QuantityParser.parse("Concurrent", SourceUnit.CM_PER_S)).isInstanceOf(ParsedValue.Rejected.class);
    }
}
