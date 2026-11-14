package com.ignis.vesta.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ignis.vesta.application.GetInvestigationUseCase;
import com.ignis.vesta.application.ListInvestigationsUseCase;
import com.ignis.vesta.domain.port.InvestigationCatalogReader;
import com.ignis.vesta.domain.port.InvestigationDetailReader;
import com.ignis.vesta.infrastructure.persistence.R2dbcInvestigationCatalogReader;
import com.ignis.vesta.infrastructure.persistence.R2dbcInvestigationDetailReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;

/** Wires the read side: catalog (F1) and investigation detail (F2). */
@Configuration
public class CatalogConfig {

    @Bean
    InvestigationCatalogReader investigationCatalogReader(DatabaseClient db) {
        return new R2dbcInvestigationCatalogReader(db);
    }

    @Bean
    InvestigationDetailReader investigationDetailReader(DatabaseClient db, ObjectMapper objectMapper) {
        return new R2dbcInvestigationDetailReader(db, objectMapper);
    }

    @Bean
    ListInvestigationsUseCase listInvestigationsUseCase(InvestigationCatalogReader reader) {
        return new ListInvestigationsUseCase(reader);
    }

    @Bean
    GetInvestigationUseCase getInvestigationUseCase(InvestigationDetailReader reader) {
        return new GetInvestigationUseCase(reader);
    }
}
