package com.ignis.vesta.infrastructure.config;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ignis.vesta.application.AskAssistantUseCase;
import com.ignis.vesta.application.CompareInvestigationsUseCase;
import com.ignis.vesta.application.ExtractInvestigationFactsUseCase;
import com.ignis.vesta.application.GetExtractionUseCase;
import com.ignis.vesta.domain.port.ExtractionRepository;
import com.ignis.vesta.domain.port.FactExtractor;
import com.ignis.vesta.infrastructure.llm.AnthropicFactExtractor;
import com.ignis.vesta.infrastructure.llm.UnconfiguredFactExtractor;
import com.ignis.vesta.infrastructure.persistence.R2dbcExtractionRepository;
import org.springframework.transaction.reactive.TransactionalOperator;
import java.time.Clock;
import com.ignis.vesta.domain.port.InvestigationCatalogReader;
import com.ignis.vesta.domain.port.InvestigationDetailReader;
import com.ignis.vesta.domain.port.LlmGateway;
import com.ignis.vesta.infrastructure.llm.AnthropicLlmGateway;
import com.ignis.vesta.infrastructure.llm.CachingLlmGateway;
import com.ignis.vesta.infrastructure.llm.UnconfiguredLlmGateway;
import com.ignis.vesta.infrastructure.persistence.R2dbcLlmCacheStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;

/**
 * Wires Claude: the assistant (Anthropic adapter wrapped by the answer cache) and the offline F2 extractor.
 * Each falls back to a null object when no key is configured.
 */
@Configuration
public class LlmConfig {

    private static final Logger log = LoggerFactory.getLogger(LlmConfig.class);

    @Bean
    LlmGateway llmGateway(VestaProperties properties, DatabaseClient db, ObjectMapper objectMapper) {
        VestaProperties.Llm llm = properties.llm();
        if (llm.apiKey() == null || llm.apiKey().isBlank()) {
            log.warn("ANTHROPIC_API_KEY is not set: AI features are disabled");
            return new UnconfiguredLlmGateway();
        }
        AnthropicClient client = AnthropicOkHttpClient.builder().apiKey(llm.apiKey()).build();
        log.info("AI features enabled with model {}", llm.model());
        return new CachingLlmGateway(new AnthropicLlmGateway(client, llm.model()), new R2dbcLlmCacheStore(db),
                objectMapper, llm.model());
    }

    @Bean
    FactExtractor factExtractor(VestaProperties properties) {
        VestaProperties.Llm llm = properties.llm();
        if (llm.apiKey() == null || llm.apiKey().isBlank()) {
            return new UnconfiguredFactExtractor(llm.model());
        }
        return new AnthropicFactExtractor(AnthropicOkHttpClient.builder().apiKey(llm.apiKey()).build(), llm.model());
    }

    @Bean
    ExtractionRepository extractionRepository(DatabaseClient db, TransactionalOperator transactions,
                                              ObjectMapper objectMapper) {
        return new R2dbcExtractionRepository(db, transactions, objectMapper);
    }

    @Bean
    ExtractInvestigationFactsUseCase extractInvestigationFactsUseCase(InvestigationDetailReader details,
                                                                      FactExtractor extractor,
                                                                      ExtractionRepository repository) {
        return new ExtractInvestigationFactsUseCase(details, extractor, repository, Clock.systemUTC());
    }

    @Bean
    GetExtractionUseCase getExtractionUseCase(InvestigationDetailReader details, ExtractionRepository repository) {
        return new GetExtractionUseCase(details, repository);
    }

    @Bean
    AskAssistantUseCase askAssistantUseCase(InvestigationCatalogReader catalog, InvestigationDetailReader details,
                                            LlmGateway llm) {
        return new AskAssistantUseCase(catalog, details, llm);
    }

    @Bean
    CompareInvestigationsUseCase compareInvestigationsUseCase(InvestigationDetailReader details,
                                                              AskAssistantUseCase assistant) {
        return new CompareInvestigationsUseCase(details, assistant);
    }
}
