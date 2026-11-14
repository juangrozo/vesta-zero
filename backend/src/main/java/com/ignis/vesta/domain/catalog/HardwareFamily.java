package com.ignis.vesta.domain.catalog;

public enum HardwareFamily {
    /** Combustion Integrated Rack with the ACME insert (gaseous fuels). */
    CIR_ACME,
    /** Multi-user Droplet Combustion Apparatus inside the CIR (liquid droplets). */
    CIR_MDCA,
    /** Microgravity Science Glovebox (solids and gas jets). */
    MSG,
    /** Saffire experiments flown in a Cygnus vehicle after leaving the station. */
    CYGNUS,
    /** Other flight hardware (e.g. smoke and dust diagnostics). */
    OTHER_FLIGHT,
    /** Ground-based modeling or laboratory work using PSI flight data. */
    GROUND
}
