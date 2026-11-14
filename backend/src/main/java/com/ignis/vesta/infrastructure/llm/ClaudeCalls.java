package com.ignis.vesta.infrastructure.llm;

import com.anthropic.client.AnthropicClient;
import com.anthropic.errors.AnthropicException;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.ignis.vesta.domain.port.LlmGateway.LlmUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shared blocking call to Claude: translates SDK errors without leaking details and logs token usage. */
final class ClaudeCalls {

    private static final Logger log = LoggerFactory.getLogger(ClaudeCalls.class);

    private ClaudeCalls() {
    }

    static <T> StructuredMessage<T> create(AnthropicClient client, StructuredMessageCreateParams<T> params,
                                           String purpose) {
        StructuredMessage<T> response;
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
        log.info("Claude {}: input={} cacheRead={} cacheWrite={} output={} stop={}", purpose,
                response.usage().inputTokens(), response.usage().cacheReadInputTokens().orElse(0L),
                response.usage().cacheCreationInputTokens().orElse(0L), response.usage().outputTokens(),
                response.stopReason().map(Object::toString).orElse("none"));
        return response;
    }
}
