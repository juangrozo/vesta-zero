package com.ignis.vesta.domain.port;

import com.ignis.vesta.domain.model.InvestigationSummary;
import reactor.core.publisher.Flux;

/** Read side of stored investigations for the catalog (kept apart from the ingestion write port). */
public interface InvestigationCatalogReader {

    Flux<InvestigationSummary> findAllSummaries();
}
