package com.ignis.vesta.domain.port;

import com.ignis.vesta.domain.extraction.InvestigationExtraction;
import com.ignis.vesta.domain.vo.PsiId;
import reactor.core.publisher.Mono;

/** Stored AI extractions. Stored rows are the cache: the demo never calls the model for F2. */
public interface ExtractionRepository {

    /** Replaces every stored fact of the investigation in one transaction. */
    Mono<Void> replace(InvestigationExtraction extraction);

    /** True when facts from this model and prompt version are already stored. */
    Mono<Boolean> exists(PsiId psiId, String model, String promptVersion);

    /** Empty when nothing was extracted for the investigation. */
    Mono<InvestigationExtraction> find(PsiId psiId);
}
