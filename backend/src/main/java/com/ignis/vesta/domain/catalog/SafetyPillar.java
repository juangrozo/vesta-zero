package com.ignis.vesta.domain.catalog;

/** Fire safety pillars from the challenge resources, plus fundamental flame physics. */
public enum SafetyPillar {
    /** Material flammability and limits (e.g. BASS, BASS-II, SAFFIRE). */
    PREVENTION,
    /** Smoke and aerosol detection (e.g. SAME, SAME-R, DAFT). */
    DETECTION,
    /** Extinguishment (e.g. FLEX: dilution with CO2 and helium). */
    SUPPRESSION,
    /** Fundamental combustion physics without a direct pillar. */
    FUNDAMENTAL
}
