package com.ignis.vesta.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Typed configuration for external adapters (PSI API and Anthropic). */
@ConfigurationProperties(prefix = "vesta")
public record VestaProperties(Psi psi, Llm llm) {

    public record Psi(String baseUrl, Duration requestDelay, Duration timeout) {
    }

    /** {@code apiKey} comes from the ANTHROPIC_API_KEY environment variable; never log it. */
    public record Llm(String apiKey, String model) {

        @Override
        public String toString() {
            return "Llm[model=" + model + ", apiKey=" + (apiKey == null || apiKey.isBlank() ? "<unset>" : "<set>") + "]";
        }
    }
}
