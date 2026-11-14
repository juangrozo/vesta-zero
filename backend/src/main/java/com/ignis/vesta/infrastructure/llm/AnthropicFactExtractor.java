package com.ignis.vesta.infrastructure.llm;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.JsonSchemaLocalValidation;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.StructuredOutputConfig;
import com.ignis.vesta.domain.extraction.ExtractedFact;
import com.ignis.vesta.domain.extraction.ExtractionField;
import com.ignis.vesta.domain.extraction.ExtractionSource;
import com.ignis.vesta.domain.port.FactExtractor;
import com.ignis.vesta.domain.port.LlmGateway.LlmUnavailableException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

/** Claude adapter for the offline F2 extraction: one structured-output call per investigation. */
public final class AnthropicFactExtractor implements FactExtractor {

    private static final long MAX_TOKENS = 8_000L;

    private final AnthropicClient client;
    private final String model;

    public AnthropicFactExtractor(AnthropicClient client, String model) {
        this.client = client;
        this.model = model;
    }

    @Override
    public Mono<List<ExtractedFact>> extract(ExtractionSource source) {
        // The SDK call is blocking; keep it off the event loop.
        return Mono.fromCallable(() -> call(source)).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public String model() {
        return model;
    }

    @Override
    public String promptVersion() {
        return ExtractionPrompt.VERSION;
    }

    private List<ExtractedFact> call(ExtractionSource source) {
        StructuredMessageCreateParams<ExtractionPayload> params =
                StructuredMessageCreateParams.<ExtractionPayload>builder()
                        .model(model)
                        .maxTokens(MAX_TOKENS)
                        .outputConfig(StructuredOutputConfig.<ExtractionPayload>builder()
                                .effort(OutputConfig.Effort.MEDIUM)
                                .format(ExtractionPayload.class, JsonSchemaLocalValidation.YES)
                                .build())
                        .system(ExtractionPrompt.INSTRUCTIONS)
                        .addUserMessage("<record id=\"" + source.psiId() + "\">\n" + source.text() + "\n</record>")
                        .putAdditionalHeader("anthropic-beta", "server-side-fallback-2026-07-01")
                        .putAdditionalBodyProperty("fallbacks", JsonValue.from("default"))
                        .build();
        StructuredMessage<ExtractionPayload> response =
                ClaudeCalls.create(client, params, "extraction " + source.psiId());
        StopReason stop = response.stopReason().orElse(null);
        if (StopReason.REFUSAL.equals(stop)) {
            throw new LlmUnavailableException("The model declined to extract facts for " + source.psiId());
        }
        if (StopReason.MAX_TOKENS.equals(stop)) {
            throw new LlmUnavailableException("The extraction for " + source.psiId() + " was cut off");
        }
        ExtractionPayload payload = response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(typed -> typed.text())
                .findFirst()
                .orElseThrow(() -> new LlmUnavailableException("Claude returned no extraction"));
        List<ExtractedFact> facts = new ArrayList<>();
        add(facts, ExtractionField.FUELS_AND_SAMPLES, payload.fuelsAndSamples());
        add(facts, ExtractionField.MEASUREMENTS, payload.measurements());
        add(facts, ExtractionField.TEST_VARIABLES, payload.testVariables());
        add(facts, ExtractionField.TEST_CONSTANTS, payload.testConstants());
        add(facts, ExtractionField.EARTH_APPLICATIONS, payload.earthApplications());
        return facts;
    }

    private static void add(List<ExtractedFact> facts, ExtractionField field, List<ExtractionPayload.Item> items) {
        if (items != null) {
            items.forEach(i -> facts.add(new ExtractedFact(field, i.label(), i.detail(), i.sourceSpan())));
        }
    }
}
