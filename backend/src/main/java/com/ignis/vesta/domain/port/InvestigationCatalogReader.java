package com.ignis.vesta.domain.port;

import com.ignis.vesta.domain.model.InvestigationSummary;
import com.ignis.vesta.domain.model.TestCondition;
import reactor.core.publisher.Flux;

/** Read side of stored investigations for the catalog and ranking (kept apart from the ingestion write port). */
public interface InvestigationCatalogReader {

    Flux<InvestigationSummary> findAllSummaries();

    /** Test conditions with at least one normalized value, for all investigations; raw rows are not loaded. */
    Flux<TestCondition> findAllNormalizedConditions();
}
