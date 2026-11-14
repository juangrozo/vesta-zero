package com.ignis.vesta.infrastructure.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** Structured output schema for the assistant; the SDK derives the JSON schema from this record. */
record AnswerPayload(
        @JsonProperty("answer")
        @JsonPropertyDescription("The answer in plain text with inline citations such as [PSI-25].")
        String answer,

        @JsonProperty("cited_psi_ids")
        @JsonPropertyDescription("Every investigation ID cited inline in the answer, for example PSI-25.")
        List<String> citedPsiIds,

        @JsonProperty("insufficient_data")
        @JsonPropertyDescription("True when the records do not contain what the question needs.")
        boolean insufficientData) {
}
