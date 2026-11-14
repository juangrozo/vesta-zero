package com.ignis.vesta.infrastructure.psi;

import com.ignis.vesta.domain.catalog.CatalogEntry;
import com.ignis.vesta.domain.model.ExperimentalTableRef;
import com.ignis.vesta.domain.model.Investigation;
import com.ignis.vesta.domain.port.PsiGateway;
import com.ignis.vesta.domain.vo.PsiId;
import reactor.core.publisher.Mono;

import java.time.Duration;

/** Decorator: waits a fixed delay before every PSI call so ingestion never hammers the repository. */
public final class ThrottledPsiGateway implements PsiGateway {

    private final PsiGateway delegate;
    private final Duration delay;

    public ThrottledPsiGateway(PsiGateway delegate, Duration delay) {
        this.delegate = delegate;
        this.delay = delay;
    }

    @Override
    public Mono<Investigation> fetchInvestigation(CatalogEntry entry) {
        return Mono.delay(delay).then(delegate.fetchInvestigation(entry));
    }

    @Override
    public Mono<byte[]> downloadExperimentalTable(PsiId psiId, ExperimentalTableRef table) {
        return Mono.delay(delay).then(delegate.downloadExperimentalTable(psiId, table));
    }
}
