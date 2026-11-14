package com.ignis.vesta.domain.port;

import com.ignis.vesta.domain.model.IngestedInvestigation;
import reactor.core.publisher.Mono;

/** Persistence of ingested investigations. */
public interface InvestigationRepository {

    /** Replaces the stored investigation, its test conditions and its warnings in one transaction. */
    Mono<Void> replace(IngestedInvestigation ingested);
}
