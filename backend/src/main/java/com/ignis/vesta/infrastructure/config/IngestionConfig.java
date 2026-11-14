package com.ignis.vesta.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ignis.vesta.application.IngestInvestigationsUseCase;
import com.ignis.vesta.domain.port.ExperimentalTableNormalizer;
import com.ignis.vesta.domain.port.InvestigationRepository;
import com.ignis.vesta.domain.port.PsiGateway;
import com.ignis.vesta.infrastructure.csv.CsvExperimentalTableNormalizer;
import com.ignis.vesta.infrastructure.persistence.R2dbcInvestigationRepository;
import com.ignis.vesta.infrastructure.psi.HttpPsiGateway;
import com.ignis.vesta.infrastructure.psi.ThrottledPsiGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/** Wires ingestion ports to their adapters. The only place that knows concrete classes. */
@Configuration
public class IngestionConfig {

    /** PSI detail JSON reaches ~8 MB (file lists); the default 256 KB codec buffer is too small. */
    private static final int MAX_IN_MEMORY_BYTES = 32 * 1024 * 1024;

    @Bean
    PsiGateway psiGateway(WebClient.Builder builder, ObjectMapper objectMapper, VestaProperties properties) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_BYTES))
                .build();
        WebClient psiClient = builder.clone()
                .baseUrl(properties.psi().baseUrl())
                .exchangeStrategies(strategies)
                .defaultHeader("User-Agent", "vesta-zero-ingestion")
                .build();
        WebClient downloadClient = builder.clone().exchangeStrategies(strategies).build();
        HttpPsiGateway http = new HttpPsiGateway(psiClient, downloadClient, objectMapper, properties.psi().timeout());
        return new ThrottledPsiGateway(http, properties.psi().requestDelay());
    }

    @Bean
    ExperimentalTableNormalizer experimentalTableNormalizer() {
        return new CsvExperimentalTableNormalizer();
    }

    @Bean
    InvestigationRepository investigationRepository(DatabaseClient db, TransactionalOperator transactions,
                                                    ObjectMapper objectMapper) {
        return new R2dbcInvestigationRepository(db, transactions, objectMapper);
    }

    @Bean
    IngestInvestigationsUseCase ingestInvestigationsUseCase(PsiGateway psiGateway,
                                                            ExperimentalTableNormalizer normalizer,
                                                            InvestigationRepository repository) {
        return new IngestInvestigationsUseCase(psiGateway, normalizer, repository);
    }
}
