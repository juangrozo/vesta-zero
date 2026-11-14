package com.ignis.vesta.domain.ranking;

/**
 * Expected lunar base and pressurized rover atmosphere (challenge resources): at least 30 % O2 at no more than
 * 10.2 psi, versus Earth's 21 % O2 at 14.7 psi (1 atm).
 */
public final class LunarHabitatTarget {

    public static final double EARTH_O2_FRACTION = 0.21;
    public static final double EARTH_PRESSURE_ATM = 1.0;
    public static final double O2_FRACTION = 0.30;
    public static final double PSI_PER_ATM = 14.6959;
    public static final double PRESSURE_ATM = 10.2 / PSI_PER_ATM;

    private LunarHabitatTarget() {
    }
}
