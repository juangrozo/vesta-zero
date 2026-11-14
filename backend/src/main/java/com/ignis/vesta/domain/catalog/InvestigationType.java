package com.ignis.vesta.domain.catalog;

/** Flight (microgravity) investigation or ground study, derived from the documented PSI project type. */
public enum InvestigationType {
    FLIGHT,
    GROUND;

    public static InvestigationType fromProjectType(String projectType) {
        return projectType != null && projectType.toLowerCase(java.util.Locale.ROOT).contains("ground") ? GROUND : FLIGHT;
    }
}
