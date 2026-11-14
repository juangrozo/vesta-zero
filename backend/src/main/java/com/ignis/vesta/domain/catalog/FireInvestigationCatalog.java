package com.ignis.vesta.domain.catalog;

import com.ignis.vesta.domain.vo.PsiId;

import java.util.List;
import java.util.Optional;

import static com.ignis.vesta.domain.catalog.FuelState.AEROSOL;
import static com.ignis.vesta.domain.catalog.FuelState.GAS;
import static com.ignis.vesta.domain.catalog.FuelState.LIQUID_DROPLET;
import static com.ignis.vesta.domain.catalog.FuelState.SOLID;
import static com.ignis.vesta.domain.catalog.HardwareFamily.CIR_ACME;
import static com.ignis.vesta.domain.catalog.HardwareFamily.CIR_MDCA;
import static com.ignis.vesta.domain.catalog.HardwareFamily.CYGNUS;
import static com.ignis.vesta.domain.catalog.HardwareFamily.GROUND;
import static com.ignis.vesta.domain.catalog.HardwareFamily.MSG;
import static com.ignis.vesta.domain.catalog.HardwareFamily.OTHER_FLIGHT;
import static com.ignis.vesta.domain.catalog.SafetyPillar.DETECTION;
import static com.ignis.vesta.domain.catalog.SafetyPillar.FUNDAMENTAL;
import static com.ignis.vesta.domain.catalog.SafetyPillar.PREVENTION;
import static com.ignis.vesta.domain.catalog.SafetyPillar.SUPPRESSION;

/**
 * The 24 fire-related PSI investigations in scope, verified against the PSI search on 2026-11-14.
 * Excluded on purpose: PSI-77 and PSI-181 (soldering), PSI-108 (STDCE-1 convection), PSI-179 (aerosol sampling).
 * The classification is curated from the challenge resources and PSI metadata; version it when it changes.
 */
public final class FireInvestigationCatalog {

    public static final String CLASSIFICATION_VERSION = "2026-11-14";

    private static final List<CatalogEntry> ENTRIES = List.of(
            // ACME (gaseous fuels, CIR)
            entry("PSI-10", CIR_ACME, GAS, FUNDAMENTAL),     // Flame Design
            entry("PSI-20", CIR_ACME, GAS, PREVENTION),      // BRE: gas burner emulating burning solids
            entry("PSI-21", CIR_ACME, GAS, FUNDAMENTAL),     // CLD Flame
            entry("PSI-22", CIR_ACME, GAS, FUNDAMENTAL),     // E-FIELD Flames
            entry("PSI-23", CIR_ACME, GAS, FUNDAMENTAL),     // s-Flame
            entry("PSI-159", CIR_ACME, GAS, FUNDAMENTAL),    // CFI-G, up to 85 % O2
            // Droplets (MDCA in CIR)
            entry("PSI-39", CIR_MDCA, LIQUID_DROPLET, FUNDAMENTAL),  // CFI
            entry("PSI-68", CIR_MDCA, LIQUID_DROPLET, FUNDAMENTAL),  // FLEX-2
            entry("PSI-69", CIR_MDCA, LIQUID_DROPLET, SUPPRESSION),  // FLEX: CO2 / He dilution
            // Solids and gas jets (MSG)
            entry("PSI-25", MSG, SOLID, PREVENTION),         // BASS-II
            entry("PSI-26", MSG, SOLID, PREVENTION),         // BASS
            entry("PSI-106", MSG, GAS, FUNDAMENTAL),         // SLICE
            entry("PSI-107", MSG, GAS, FUNDAMENTAL),         // SPICE
            // Large-scale solids (Cygnus)
            entry("PSI-98", CYGNUS, SOLID, PREVENTION),      // SAFFIRE-I
            entry("PSI-99", CYGNUS, SOLID, PREVENTION),      // SAFFIRE-II
            entry("PSI-100", CYGNUS, SOLID, PREVENTION),     // SAFFIRE-III
            // Smoke and dust detection
            entry("PSI-101", OTHER_FLIGHT, SOLID, DETECTION),   // SAME-R
            entry("PSI-102", OTHER_FLIGHT, SOLID, DETECTION),   // SAME
            entry("PSI-47", OTHER_FLIGHT, AEROSOL, DETECTION),  // DAFT / DAFT-2
            // Ground investigations modeling PSI flight data
            entry("PSI-117", GROUND, LIQUID_DROPLET, FUNDAMENTAL),  // cool flame dynamics (CFD)
            entry("PSI-62", GROUND, SOLID, PREVENTION),             // concurrent flame spread
            entry("PSI-60", GROUND, GAS, FUNDAMENTAL),              // SPICE data
            entry("PSI-142", GROUND, LIQUID_DROPLET, FUNDAMENTAL),  // cool flame transitions (FLEX data)
            entry("PSI-115", GROUND, AEROSOL, DETECTION)            // SAME data
    );

    private FireInvestigationCatalog() {
    }

    public static List<CatalogEntry> entries() {
        return ENTRIES;
    }

    public static Optional<CatalogEntry> find(PsiId psiId) {
        return ENTRIES.stream().filter(e -> e.psiId().equals(psiId)).findFirst();
    }

    private static CatalogEntry entry(String id, HardwareFamily family, FuelState fuel, SafetyPillar pillar) {
        return new CatalogEntry(PsiId.of(id), family, fuel, pillar);
    }
}
