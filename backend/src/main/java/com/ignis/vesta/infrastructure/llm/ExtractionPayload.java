package com.ignis.vesta.infrastructure.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** Structured output schema for the F2 extraction; the SDK derives the JSON schema from these records. */
record ExtractionPayload(
        @JsonProperty("fuels_and_samples")
        @JsonPropertyDescription("Fuels, materials and samples tested, with form, shape and size when stated.")
        List<Item> fuelsAndSamples,

        @JsonProperty("measurements")
        @JsonPropertyDescription("Sensors, instruments and diagnostics, with the quantities they measure.")
        List<Item> measurements,

        @JsonProperty("test_variables")
        @JsonPropertyDescription("Parameters that change between tests, with their stated values or range.")
        List<Item> testVariables,

        @JsonProperty("test_constants")
        @JsonPropertyDescription("Conditions the record says stay the same across tests, with their stated value.")
        List<Item> testConstants,

        @JsonProperty("earth_applications")
        @JsonPropertyDescription("Uses of the research on Earth that the record states.")
        List<Item> earthApplications) {

    record Item(
            @JsonProperty("label")
            @JsonPropertyDescription("A few words naming the item.")
            String label,

            @JsonProperty("detail")
            @JsonPropertyDescription("What the record states about it; empty when nothing more is stated.")
            String detail,

            @JsonProperty("source_span")
            @JsonPropertyDescription("The supporting passage, copied character for character from the record.")
            String sourceSpan) {
    }
}
