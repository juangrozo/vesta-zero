package com.ignis.vesta.application;

import com.ignis.vesta.domain.extraction.InvestigationExtraction;
import com.ignis.vesta.domain.port.ExtractionRepository;
import com.ignis.vesta.domain.port.InvestigationDetailReader;
import com.ignis.vesta.domain.vo.PsiId;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

/** Stored AI extraction of one investigation. Empty Mono: unknown investigation; empty Optional: not extracted. */
public class GetExtractionUseCase {

    private final InvestigationDetailReader details;
    private final ExtractionRepository repository;

    public GetExtractionUseCase(InvestigationDetailReader details, ExtractionRepository repository) {
        this.details = Objects.requireNonNull(details);
        this.repository = Objects.requireNonNull(repository);
    }

    public Mono<Optional<InvestigationExtraction>> find(PsiId psiId) {
        return details.findById(psiId).flatMap(found -> repository.find(psiId)
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty()));
    }
}
