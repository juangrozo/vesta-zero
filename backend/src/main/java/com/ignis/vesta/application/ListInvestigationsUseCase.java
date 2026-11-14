package com.ignis.vesta.application;

import com.ignis.vesta.domain.catalog.CatalogFilter;
import com.ignis.vesta.domain.model.InvestigationSummary;
import com.ignis.vesta.domain.port.InvestigationCatalogReader;
import reactor.core.publisher.Flux;

import java.util.Comparator;
import java.util.Objects;

/** Lists catalog investigations matching a filter, in natural PSI id order. */
public class ListInvestigationsUseCase {

    private final InvestigationCatalogReader reader;

    public ListInvestigationsUseCase(InvestigationCatalogReader reader) {
        this.reader = Objects.requireNonNull(reader);
    }

    public Flux<InvestigationSummary> list(CatalogFilter filter) {
        return reader.findAllSummaries()
                .filter(filter::matches)
                .sort(Comparator.comparingInt(s -> s.psiId().number()));
    }
}
