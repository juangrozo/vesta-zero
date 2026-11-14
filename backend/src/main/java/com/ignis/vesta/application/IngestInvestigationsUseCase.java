package com.ignis.vesta.application;

import com.ignis.vesta.domain.catalog.CatalogEntry;
import com.ignis.vesta.domain.model.ExperimentalTableRef;
import com.ignis.vesta.domain.model.IngestedInvestigation;
import com.ignis.vesta.domain.model.IngestionWarning;
import com.ignis.vesta.domain.model.Investigation;
import com.ignis.vesta.domain.model.TableNormalization;
import com.ignis.vesta.domain.model.TestCondition;
import com.ignis.vesta.domain.port.ExperimentalTableNormalizer;
import com.ignis.vesta.domain.port.InvestigationRepository;
import com.ignis.vesta.domain.port.PsiGateway;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Ingests catalog investigations from PSI: metadata, then each experimental table, then one atomic save.
 * Runs sequentially so the PSI gateway can throttle requests. A failure in one investigation or one table
 * is recorded and never aborts the run.
 */
public class IngestInvestigationsUseCase {

    private final PsiGateway psiGateway;
    private final ExperimentalTableNormalizer normalizer;
    private final InvestigationRepository repository;

    public IngestInvestigationsUseCase(PsiGateway psiGateway, ExperimentalTableNormalizer normalizer,
                                       InvestigationRepository repository) {
        this.psiGateway = Objects.requireNonNull(psiGateway);
        this.normalizer = Objects.requireNonNull(normalizer);
        this.repository = Objects.requireNonNull(repository);
    }

    public Mono<IngestionReport> ingest(List<CatalogEntry> entries) {
        return Flux.fromIterable(entries)
                .concatMap(this::ingestOne)
                .collectList()
                .map(IngestionReport::new);
    }

    private Mono<IngestionReport.Line> ingestOne(CatalogEntry entry) {
        String id = entry.psiId().value();
        return psiGateway.fetchInvestigation(entry)
                .flatMap(this::collectTables)
                .flatMap(ingested -> repository.replace(ingested).thenReturn(toLine(ingested)))
                .onErrorResume(e -> Mono.just(IngestionReport.Line.failed(id, describe(e))));
    }

    private Mono<IngestedInvestigation> collectTables(Investigation investigation) {
        return Flux.fromIterable(investigation.experimentalTables())
                .concatMap(table -> normalizeTable(investigation, table))
                .collectList()
                .map(results -> {
                    List<TestCondition> conditions = new ArrayList<>();
                    List<IngestionWarning> warnings = new ArrayList<>();
                    results.forEach(r -> {
                        conditions.addAll(r.conditions());
                        warnings.addAll(r.warnings());
                    });
                    return new IngestedInvestigation(investigation, conditions, warnings);
                });
    }

    private Mono<TableNormalization> normalizeTable(Investigation investigation, ExperimentalTableRef table) {
        return psiGateway.downloadExperimentalTable(investigation.psiId(), table)
                .map(content -> normalizer.normalize(investigation.psiId(), table.fileName(), content))
                .onErrorResume(e -> Mono.just(new TableNormalization(table.fileName(), "none", List.of(),
                        List.of(IngestionWarning.forFile(investigation.psiId(), table.fileName(),
                                "table not ingested: " + describe(e))))));
    }

    private static IngestionReport.Line toLine(IngestedInvestigation ingested) {
        return new IngestionReport.Line(
                ingested.investigation().psiId().value(),
                true,
                ingested.investigation().experimentalTables().size(),
                ingested.conditions().size(),
                ingested.conditionsWithNormalizedValues(),
                ingested.warnings().size(),
                null);
    }

    private static String describe(Throwable e) {
        return e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
    }
}
