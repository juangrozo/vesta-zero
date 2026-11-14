package com.ignis.vesta.domain.ranking;

/** How a test value is compared with a target atmosphere value. */
public enum TargetBound {
    /** The target is a minimum (for example "30 % O2 or more"): reaching or passing it scores 1. */
    AT_LEAST,
    /** The target is a maximum (for example "10.2 psi or less"): reaching or going below it scores 1. */
    AT_MOST,
    /** The target is a nominal value: a tested range that contains it scores 1. */
    AROUND
}
