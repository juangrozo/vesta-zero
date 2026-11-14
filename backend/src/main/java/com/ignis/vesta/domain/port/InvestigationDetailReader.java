package com.ignis.vesta.domain.port;

import com.ignis.vesta.domain.model.IngestionWarning;
import com.ignis.vesta.domain.model.Investigation;
import com.ignis.vesta.domain.model.TestCondition;
import com.ignis.vesta.domain.vo.PsiId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Read side for one stored investigation and its experimental data. */
public interface InvestigationDetailReader {

    /** Empty when the investigation is not stored. */
    Mono<Investigation> findById(PsiId psiId);

    /** Test conditions ordered by source file and row. */
    Flux<TestCondition> findTestConditions(PsiId psiId);

    Flux<IngestionWarning> findWarnings(PsiId psiId);
}
