package com.ignis.vesta.infrastructure.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ignis.vesta.domain.assistant.EvidenceCorpus;
import com.ignis.vesta.domain.assistant.GeneratedAnswer;
import com.ignis.vesta.domain.port.LlmGateway;
import com.ignis.vesta.domain.vo.PsiId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class CachingLlmGatewayTest {

    private final EvidenceCorpus corpus = new EvidenceCorpus("records", Set.of(PsiId.of("PSI-25")), "fp-1");
    private final InMemoryStore store = new InMemoryStore();
    private final AtomicInteger calls = new AtomicInteger();

    @Test
    void secondIdenticalQuestionIsServedFromCache() {
        CachingLlmGateway gateway = gateway(answering(false));

        GeneratedAnswer first = gateway.answer("Which tests used PMMA?", corpus).block();
        GeneratedAnswer second = gateway.answer("  which tests   used PMMA? ", corpus).block();

        assertThat(calls).hasValue(1);
        assertThat(first.fromCache()).isFalse();
        assertThat(second.fromCache()).isTrue();
        assertThat(second.text()).isEqualTo(first.text());
        assertThat(second.citedPsiIds()).containsExactly("PSI-25");
    }

    @Test
    void changedCorpusMissesTheCache() {
        CachingLlmGateway gateway = gateway(answering(false));
        gateway.answer("q?", corpus).block();
        gateway.answer("q?", new EvidenceCorpus("records v2", corpus.psiIds(), "fp-2")).block();
        assertThat(calls).hasValue(2);
    }

    @Test
    void declinedAnswersAreNotCached() {
        CachingLlmGateway gateway = gateway(answering(true));
        gateway.answer("q?", corpus).block();
        gateway.answer("q?", corpus).block();
        assertThat(calls).hasValue(2);
        assertThat(store.rows).isEmpty();
    }

    @Test
    void cacheFailuresDoNotBlockAnswers() {
        LlmCacheStore broken = new LlmCacheStore() {
            @Override
            public Mono<String> find(String hash) {
                return Mono.error(new IllegalStateException("db down"));
            }

            @Override
            public Mono<Void> save(String hash, String requestJson, String responseJson, String model) {
                return Mono.error(new IllegalStateException("db down"));
            }
        };
        CachingLlmGateway gateway = new CachingLlmGateway(answering(false), broken, new ObjectMapper(), "m");
        assertThat(gateway.answer("q?", corpus).block().text()).isEqualTo("Answer [PSI-25]");
    }

    private CachingLlmGateway gateway(LlmGateway delegate) {
        return new CachingLlmGateway(delegate, store, new ObjectMapper(), "claude-opus-5-5");
    }

    private LlmGateway answering(boolean declined) {
        return (question, c) -> {
            calls.incrementAndGet();
            return Mono.just(new GeneratedAnswer("Answer [PSI-25]", List.of("PSI-25"), false, declined,
                    "claude-opus-5-5", false));
        };
    }

    private static final class InMemoryStore implements LlmCacheStore {
        final Map<String, String> rows = new ConcurrentHashMap<>();

        @Override
        public Mono<String> find(String hash) {
            return Mono.justOrEmpty(rows.get(hash));
        }

        @Override
        public Mono<Void> save(String hash, String requestJson, String responseJson, String model) {
            rows.putIfAbsent(hash, responseJson);
            return Mono.empty();
        }
    }
}
