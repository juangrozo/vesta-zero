package com.ignis.vesta.application;

import com.ignis.vesta.domain.extraction.ExtractionSource;
import com.ignis.vesta.domain.extraction.ExtractionVerifier;
import com.ignis.vesta.domain.extraction.InvestigationExtraction;
import com.ignis.vesta.domain.model.ConditionsSummary;
import com.ignis.vesta.domain.model.TestCondition;
import com.ignis.vesta.domain.port.ExtractionRepository;
import com.ignis.vesta.domain.port.FactExtractor;
import com.ignis.vesta.domain.port.InvestigationDetailReader;
import com.ignis.vesta.domain.vo.PsiId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Offline AI extraction for the detail view (spec F2). For each stored investigation: build the source text,
 * ask the model, keep only facts whose source span is verified, and store them. Investigations already extracted
 * with the same model and prompt version are skipped unless {@code force} is set, so a rerun costs nothing.
 * Investigations run one at a time; a failure is reported and the run continues.
 */
public class ExtractInvestigationFactsUseCase {

    private final InvestigationDetailReader details;
    private final FactExtractor extractor;
    private final ExtractionRepository repository;
    private final Clock clock;

    public ExtractInvestigationFactsUseCase(InvestigationDetailReader details, FactExtractor extractor,
                                            ExtractionRepository repository, Clock clock) {
        this.details = Objects.requireNonNull(details);
        this.extractor = Objects.requireNonNull(extractor);
        this.repository = Objects.requireNonNull(repository);
        this.clock = Objects.requireNonNull(clock);
    }

    public record Line(PsiId psiId, String status, int accepted, List<ExtractionVerifier.Rejection> rejected,
                       String error) {
    }

    public Flux<Line> extractAll(List<PsiId> psiIds, boolean force) {
        return Flux.fromIterable(psiIds).concatMap(id -> extractOne(id, force)
                .onErrorResume(e -> Mono.just(new Line(id, "failed", 0, List.of(), e.getMessage()))));
    }

    private Mono<Line> extractOne(PsiId id, boolean force) {
        Mono<Boolean> skip = force ? Mono.just(false)
                : repository.exists(id, extractor.model(), extractor.promptVersion());
        return skip.flatMap(alreadyDone -> alreadyDone
                ? Mono.just(new Line(id, "skipped", 0, List.of(), null))
                : source(id).flatMap(this::run)
                        .switchIfEmpty(Mono.fromSupplier(() -> new Line(id, "failed", 0, List.of(),
                                "investigation is not stored; run the ingestion first"))));
    }

    private Mono<ExtractionSource> source(PsiId id) {
        return details.findById(id).flatMap(investigation -> details.findTestConditions(id).collectList()
                .map(rows -> ExtractionSource.of(investigation, columns(rows),
                        ConditionsSummary.of(rows).fuelsOrMaterials())));
    }

    private Mono<Line> run(ExtractionSource source) {
        return extractor.extract(source).flatMap(facts -> {
            ExtractionVerifier.Result verified = ExtractionVerifier.verify(facts, source);
            InvestigationExtraction extraction = new InvestigationExtraction(source.psiId(), extractor.model(),
                    extractor.promptVersion(), clock.instant(), verified.accepted());
            return repository.replace(extraction).thenReturn(new Line(source.psiId(), "extracted",
                    verified.accepted().size(), verified.rejected(), null));
        });
    }

    private static List<String> columns(List<TestCondition> rows) {
        Set<String> columns = new LinkedHashSet<>();
        rows.forEach(row -> columns.addAll(row.rawRow().keySet()));
        return List.copyOf(columns);
    }
}
