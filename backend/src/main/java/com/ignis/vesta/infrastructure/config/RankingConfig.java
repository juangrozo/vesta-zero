package com.ignis.vesta.infrastructure.config;

import com.ignis.vesta.application.RankInvestigationsUseCase;
import com.ignis.vesta.domain.port.InvestigationCatalogReader;
import com.ignis.vesta.domain.ranking.DefaultRankingCriteria;
import com.ignis.vesta.domain.ranking.RankingEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the explainable ranking (spec F3). */
@Configuration
public class RankingConfig {

    @Bean
    RankingEngine rankingEngine() {
        return new RankingEngine(DefaultRankingCriteria.all());
    }

    @Bean
    RankInvestigationsUseCase rankInvestigationsUseCase(InvestigationCatalogReader reader, RankingEngine engine) {
        return new RankInvestigationsUseCase(reader, engine);
    }
}
