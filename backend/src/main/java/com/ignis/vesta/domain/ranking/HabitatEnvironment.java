package com.ignis.vesta.domain.ranking;

/**
 * Crew environments the ranking can target, each with the atmosphere tests are compared against.
 * Values come from the challenge resources; where they do not give one, the preset is marked as an assumption
 * and says so in {@link #basis()}.
 */
public enum HabitatEnvironment {

    SPACECRAFT_STATION("Spacecraft and space station cabin", 0.21, TargetBound.AROUND, 14.7, TargetBound.AROUND,
            "Assumed Earth sea-level air: 21 % O2 at 14.7 psi. The challenge gives these values for Earth, not for "
                    + "a specific vehicle.", true),

    LUNAR_BASE_ROVER("Lunar base and pressurized rover", 0.30, TargetBound.AT_LEAST, 10.2, TargetBound.AT_MOST,
            "Expected 30 % O2 or more at 10.2 psi or less (challenge resources): more than 40 % more oxygen "
                    + "than Earth's atmosphere.", false),

    MARS_BASE("Martian base", 0.30, TargetBound.AT_LEAST, 10.2, TargetBound.AT_MOST,
            "The challenge gives no Martian habitat atmosphere. Assumed the same as the lunar base: 30 % O2 or "
                    + "more at 10.2 psi or less.", true),

    SPACESUIT("Spacesuit (reference)", 1.0, TargetBound.AT_LEAST, 4.3, TargetBound.AT_MOST,
            "100 % O2 at 4.3 psi (challenge resources). Not a habitat: the most oxygen-rich atmosphere a crew "
                    + "member breathes, useful as a worst case.", false);

    public static final HabitatEnvironment DEFAULT = LUNAR_BASE_ROVER;
    public static final double PSI_PER_ATM = 14.6959;

    private final String displayName;
    private final double o2Fraction;
    private final TargetBound o2Bound;
    private final double pressurePsi;
    private final TargetBound pressureBound;
    private final String basis;
    private final boolean assumption;

    HabitatEnvironment(String displayName, double o2Fraction, TargetBound o2Bound, double pressurePsi,
                       TargetBound pressureBound, String basis, boolean assumption) {
        this.displayName = displayName;
        this.o2Fraction = o2Fraction;
        this.o2Bound = o2Bound;
        this.pressurePsi = pressurePsi;
        this.pressureBound = pressureBound;
        this.basis = basis;
        this.assumption = assumption;
    }

    public String displayName() {
        return displayName;
    }

    public double o2Fraction() {
        return o2Fraction;
    }

    public TargetBound o2Bound() {
        return o2Bound;
    }

    public double pressurePsi() {
        return pressurePsi;
    }

    public double pressureAtm() {
        return pressurePsi / PSI_PER_ATM;
    }

    public TargetBound pressureBound() {
        return pressureBound;
    }

    /** Where the target comes from, shown next to the ranking. */
    public String basis() {
        return basis;
    }

    /** True when the target is a Vesta Zero assumption rather than a value from the challenge resources. */
    public boolean assumption() {
        return assumption;
    }

    /** Case-insensitive lookup; {@link #DEFAULT} for null or blank. */
    public static HabitatEnvironment parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT;
        }
        try {
            return valueOf(raw.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown environment '" + raw + "'. Allowed: "
                    + java.util.Arrays.toString(values()));
        }
    }
}
