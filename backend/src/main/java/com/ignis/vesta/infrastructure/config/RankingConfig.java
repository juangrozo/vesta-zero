package com.ignis.vesta.infrastructure.config;

import com.ignis.vesta.application.RankInvestigationsUseCase;
import com.ignis.vesta.domain.port.InvestigationCatalogReader;
import com.ignis.vesta.domain.ranking.DefaultRankingCriteria;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the explainable ranking (spec F3). */
@Configuration
public class RankingConfig {

    @Bean
    RankInvestigationsUseCase rankInvestigationsUseCase(InvestigationCatalogReader reader) {
        return new RankInvestigationsUseCase(reader, DefaultRankingCriteria::forEnvironment);
    }
}
