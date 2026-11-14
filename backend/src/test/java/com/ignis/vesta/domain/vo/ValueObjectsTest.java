package com.ignis.vesta.domain.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValueObjectsTest {

    @Test
    void psiIdAcceptsValidAccession() {
        assertThat(PsiId.of("PSI-159").value()).isEqualTo("PSI-159");
    }

    @Test
    void psiIdRejectsInvalidAccession() {
        assertThatThrownBy(() -> PsiId.of("psi-25")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PsiId.of("PSI-")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void doiBuildsResolverUrl() {
        assertThat(new Doi("10.60555/4qc4-de67").url()).isEqualTo("https://doi.org/10.60555/4qc4-de67");
    }

    @Test
    void doiRejectsMalformedValue() {
        assertThatThrownBy(() -> new Doi("4qc4-de67")).isInstanceOf(IllegalArgumentException.class);
    }
}
