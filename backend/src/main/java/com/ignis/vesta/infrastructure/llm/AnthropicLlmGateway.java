package com.ignis.vesta.infrastructure.llm;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.JsonSchemaLocalValidation;
import com.anthropic.core.JsonValue;
import com.anthropic.errors.AnthropicException;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.StructuredOutputConfig;
import com.anthropic.models.messages.TextBlockParam;
import com.ignis.vesta.domain.assistant.EvidenceCorpus;
import com.ignis.vesta.domain.assistant.GeneratedAnswer;
import com.ignis.vesta.domain.port.LlmGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * Claude adapter. The instructions and the corpus form a stable system prefix cached for one hour, so repeated
 * questions only pay for the question itself. Structured output gives a validated JSON answer.
 * Server-side refusal fallback is enabled; a refusal that still happens is returned as a declined answer.
 */
public final class AnthropicLlmGateway implements LlmGateway {

    private static final Logger log = LoggerFactory.getLogger(AnthropicLlmGateway.class);
    private static final long MAX_TOKENS = 16_000L;

    private final AnthropicClient client;
    private final String model;

    public AnthropicLlmGateway(AnthropicClient client, String model) {
        this.client = client;
        this.model = model;
    }

    @Override
    public Mono<GeneratedAnswer> answer(String question, EvidenceCorpus corpus) {
        // The SDK call is blocking; keep it off the event loop.
        return Mono.fromCallable(() -> call(question, corpus)).subscribeOn(Schedulers.boundedElastic());
    }

    private GeneratedAnswer call(String question, EvidenceCorpus corpus) {
        StructuredMessageCreateParams<AnswerPayload> params = StructuredMessageCreateParams.<AnswerPayload>builder()
                .model(model)
                .maxTokens(MAX_TOKENS)
                .outputConfig(StructuredOutputConfig.<AnswerPayload>builder()
                        .effort(OutputConfig.Effort.MEDIUM)
                        .format(AnswerPayload.class, JsonSchemaLocalValidation.YES)
                        .build())
                .systemOfTextBlockParams(List.of(
                        TextBlockParam.builder().text(AssistantPrompt.INSTRUCTIONS).build(),
                        TextBlockParam.builder()
                                .text("Investigation records:\n\n" + corpus.text())
                                .cacheControl(CacheControlEphemeral.builder()
                                        .ttl(CacheControlEphemeral.Ttl.TTL_1H)
                                        .build())
                                .build()))
                .addUserMessage(question)
                .putAdditionalHeader("anthropic-beta", "server-side-fallback-2026-07-01")
                .putAdditionalBodyProperty("fallbacks", JsonValue.from("default"))
                .build();
        StructuredMessage<AnswerPayload> response;
        try {
            response = client.messages().create(params);
        } catch (AnthropicServiceException e) {
            throw new LlmUnavailableException("Claude request failed with HTTP " + e.statusCode());
        } catch (AnthropicException e) {
            Throwable root = e;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            log.warn("Claude request failed: {} (root cause {}: {})", e.getClass().getSimpleName(),
                    root.getClass().getName(), root.getMessage());
            throw new LlmUnavailableException("Claude could not be reached (" + e.getClass().getSimpleName() + ")");
        }
        log.info("Claude answer: input={} cacheRead={} cacheWrite={} output={} stop={}",
                response.usage().inputTokens(), response.usage().cacheReadInputTokens().orElse(0L),
                response.usage().cacheCreationInputTokens().orElse(0L), response.usage().outputTokens(),
                response.stopReason().map(Object::toString).orElse("none"));

        StopReason stop = response.stopReason().orElse(null);
        if (StopReason.REFUSAL.equals(stop)) {
            return new GeneratedAnswer("The model declined to answer this question.", List.of(), false, true,
                    model, false);
        }
        if (StopReason.MAX_TOKENS.equals(stop)) {
            throw new LlmUnavailableException("The answer was cut off before it was complete");
        }
        AnswerPayload payload = response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(typed -> typed.text())
                .findFirst()
                .orElseThrow(() -> new LlmUnavailableException("Claude returned no answer"));
        return new GeneratedAnswer(payload.answer(),
                payload.citedPsiIds() == null ? List.of() : payload.citedPsiIds(),
                payload.insufficientData(), false, model, false);
    }
}
