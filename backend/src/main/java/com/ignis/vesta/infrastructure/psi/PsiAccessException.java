package com.ignis.vesta.infrastructure.psi;

/** PSI request failure. Messages never contain signed download URLs. */
public class PsiAccessException extends RuntimeException {

    public PsiAccessException(String message) {
        super(message);
    }
}
