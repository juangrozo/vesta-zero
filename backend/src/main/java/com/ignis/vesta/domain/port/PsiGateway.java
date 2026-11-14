package com.ignis.vesta.domain.port;

import com.ignis.vesta.domain.catalog.CatalogEntry;
import com.ignis.vesta.domain.model.ExperimentalTableRef;
import com.ignis.vesta.domain.model.Investigation;
import com.ignis.vesta.domain.vo.PsiId;
import reactor.core.publisher.Mono;

/** Read-only access to the NASA PSI repository. */
public interface PsiGateway {

    /** Fetches and maps the PSI record for a catalog entry; the entry's curated classification is attached. */
    Mono<Investigation> fetchInvestigation(CatalogEntry entry);

    /** Downloads one experimental-table file. Implementations must never expose the signed download URL. */
    Mono<byte[]> downloadExperimentalTable(PsiId psiId, ExperimentalTableRef table);
}
