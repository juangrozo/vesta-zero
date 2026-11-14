package com.ignis.vesta.infrastructure.csv;

import java.util.List;

import static com.ignis.vesta.domain.normalization.SourceUnit.ATM;
import static com.ignis.vesta.domain.normalization.SourceUnit.BAR;
import static com.ignis.vesta.domain.normalization.SourceUnit.FRACTION;
import static com.ignis.vesta.domain.normalization.SourceUnit.MMHG;
import static com.ignis.vesta.domain.normalization.SourceUnit.PERCENT;

/**
 * Curated mappings for every experimental table of the in-scope investigations, written from the headers
 * observed on 2026-11-14. Only test conditions are mapped (not post-test results such as final O2 after a burn
 * in BASS-II). Add a new entry when PSI adds a file; unknown files fall back to raw-only storage.
 */
final class PsiTableMappings {

    private PsiTableMappings() {
    }

    static List<TableMapping> all() {
        return List.of(
                // ACME
                TableMapping.file("PSI-10_Experimental table_Flame Design_Normal Flame.csv")
                        .label("Test").fuel("Fuel")
                        .o2("XO2 (init)", FRACTION).o2("XO2 (final)", FRACTION)
                        .pressure("pinit (atm)", ATM).pressure("pfinal (atm)", ATM).build(),
                TableMapping.file("PSI-10_Experimental table_Flame Design_Inverse Flame.csv")
                        .label("Test")
                        .o2("XO2", FRACTION)
                        .pressure("pinit (atm)", ATM).pressure("pfinal (atm)", ATM).build(),
                TableMapping.file("PSI-20_Experimental table_ACME BRE.csv")
                        .label("Date").fuel("Fuel Bottle")
                        .o2("Oxygen Conc (percent)", PERCENT).build(),
                TableMapping.file("PSI-21_Experimental table_CLD Flame.csv")
                        .label("Test Day").fuel("Fuel Bottle").build(),
                TableMapping.file("PSI-22_Experimental table_ACME E-FIELD Flames.csv")
                        .label("Test Day").fuel("Fuel Bottle").build(),
                TableMapping.file("PSI-23_Experimental table_ACME s-Flame.csv")
                        .label("Test Day*").fuel("Fuel Bottle")
                        .o2("Atmosphere*", PERCENT).build(),
                TableMapping.file("PSI-159_Experimental table_CFI-G_Normal Flames.csv")
                        .label("Test").fuel("Fuel")
                        .o2("XO2 before test", FRACTION).o2("XO2 after test", FRACTION)
                        .pressure("approx p (bar)", BAR).build(),
                TableMapping.file("PSI-159_Experimental table_CFI-G_Inverse Flames.csv")
                        .label("Test").fuel("Fuel")
                        .o2("XO2", FRACTION)
                        .pressure("approx p (bar)", BAR).build(),
                // Droplets
                TableMapping.file("PSI-39_Experimental table_CFI.csv")
                        .label("Flex Number").fuel("Fuel")
                        .o2("Oxygen", FRACTION)
                        .pressure("Pressure", ATM).build(),
                TableMapping.file("PSI-68_Experimental table_Flex2.csv")
                        .fuel("Sample (Fuel Type)")
                        .o2("Oxygen Mole Fraction", FRACTION)
                        .pressure("Ambient Pressure (atm)", ATM)
                        .flow("Flow Velocity (cm/s)").build(),
                TableMapping.file("PSI-69_Experimental table_FLEX.csv")
                        .label("FLEX Test #").fuel("Fuel")
                        .o2("O initial ambient composition; mole fraction", FRACTION)
                        .pressure("Ambient pressure; mmHg", MMHG).build(),
                // Solids and gas jets (MSG)
                TableMapping.file("PSI-25_Experimental table_BASS-II.csv")
                        .label("Test #").fuel("Fuel Sample Material")
                        .o2("Calibrated initial O2 % by vol", PERCENT).build(),
                TableMapping.file("PSI-26_Experimental table_BASS.csv")
                        .label("Test #").fuel("Fuel Sample Material").build(),
                TableMapping.file("PSI-106_Experimental table_Slice.csv")
                        .label("Test Date").fuel("Fuel Type").build(),
                TableMapping.file("PSI-107_Experimental table_Spice.csv")
                        .label("GMT ignition").fuel("Fuel")
                        .flow("Air Rate (cm/s)").build(),
                // Saffire (Cygnus)
                TableMapping.file("PSI-98_Experimental table_SAFFIRE-1.csv")
                        .label("Sample Number").fuel("Material")
                        .o2("Percent O2", PERCENT)
                        .flow("Air Flow (cm/s)").build(),
                TableMapping.file("PSI-99_Experimental table_SAFFIRE-2.csv")
                        .label("Sample Number").fuel("Material")
                        .o2("Percent O2 (Note 1)", PERCENT)
                        .flow("Air Flow (cm/s)").build(),
                TableMapping.file("PSI-100_Experimental table_SAFFIRE-3.csv")
                        .label("Sample Number").fuel("Material")
                        .flow("Air Flow").build(),
                // Smoke and dust
                TableMapping.file("PSI-101_Experimental table_SAME-R.csv")
                        .label("SAME Test #").fuel("Sample Type")
                        .flow("Inlet velocity (cm/s)").build(),
                TableMapping.file("PSI-102_Experimental table_SAME.csv")
                        .label("Sample #").fuel("Sample Material")
                        .flow("Air Flow (cm/s)").build(),
                TableMapping.file("PSI-47_Experimental table_DAFT.csv")
                        .label("Session Name").fuel("Sample Material").build(),
                // Ground: index of published figures, not test runs
                TableMapping.file("PSI-142_Experimental table_Data Summary Table.csv")
                        .label("Data ID").build()
        );
    }
}
