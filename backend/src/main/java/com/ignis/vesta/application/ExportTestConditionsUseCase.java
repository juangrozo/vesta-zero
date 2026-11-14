package com.ignis.vesta.application;

import com.ignis.vesta.domain.export.ExportedCondition;
import com.ignis.vesta.domain.export.TestConditionQuery;
import com.ignis.vesta.domain.port.InvestigationCatalogReader;
import com.ignis.vesta.domain.port.InvestigationDetailReader;
import reactor.core.publisher.Flux;

import java.util.Comparator;
import java.util.Objects;

/**
 * Documented test conditions across investigations, for modeling work outside the dashboard. Rows come out in
 * PSI number order, then by source file and row. At most ~2,000 rows exist, so no paging is needed.
 */
public class ExportTestConditionsUseCase {

    private final InvestigationCatalogReader catalog;
    private final InvestigationDetailReader details;

    public ExportTestConditionsUseCase(InvestigationCatalogReader catalog, InvestigationDetailReader details) {
        this.catalog = Objects.requireNonNull(catalog);
        this.details = Objects.requireNonNull(details);
    }

    public Flux<ExportedCondition> export(TestConditionQuery query) {
        return catalog.findAllSummaries()
                .filter(s -> query.includes(s.psiId()) && s.testCount() > 0)
                .sort(Comparator.comparingInt(s -> s.psiId().number()))
                .concatMap(s -> details.findTestConditions(s.psiId())
                        .filter(query::matches)
                        .map(row -> new ExportedCondition(s, row)));
    }
}
