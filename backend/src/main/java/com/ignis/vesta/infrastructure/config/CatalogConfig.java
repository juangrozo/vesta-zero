package com.ignis.vesta.infrastructure.config;

import com.ignis.vesta.application.ListInvestigationsUseCase;
import com.ignis.vesta.domain.port.InvestigationCatalogReader;
import com.ignis.vesta.infrastructure.persistence.R2dbcInvestigationCatalogReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;

/** Wires the catalog read side (spec F1). */
@Configuration
public class CatalogConfig {

    @Bean
    InvestigationCatalogReader investigationCatalogReader(DatabaseClient db) {
        return new R2dbcInvestigationCatalogReader(db);
    }

    @Bean
    ListInvestigationsUseCase listInvestigationsUseCase(InvestigationCatalogReader reader) {
        return new ListInvestigationsUseCase(reader);
    }
}
