package com.ignis.vesta.application;

import com.ignis.vesta.domain.model.InvestigationSummary;
import com.ignis.vesta.domain.model.TestCondition;
import com.ignis.vesta.domain.port.InvestigationCatalogReader;
import com.ignis.vesta.domain.ranking.RankedInvestigation;
import com.ignis.vesta.domain.ranking.RankingCriterion;
import com.ignis.vesta.domain.ranking.RankingEngine;
import com.ignis.vesta.domain.ranking.RankingInput;
import com.ignis.vesta.domain.ranking.RankingWeights;
import com.ignis.vesta.domain.vo.PsiId;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** Explainable ranking of all catalog investigations by lunar habitat relevance (spec F3). */
public class RankInvestigationsUseCase {

    public record Result(RankingWeights weights, List<RankedInvestigation> ranked) {
    }

    private final InvestigationCatalogReader reader;
    private final RankingEngine engine;

    public RankInvestigationsUseCase(InvestigationCatalogReader reader, RankingEngine engine) {
        this.reader = Objects.requireNonNull(reader);
        this.engine = Objects.requireNonNull(engine);
    }

    public List<RankingCriterion> criteria() {
        return engine.criteria();
    }

    /** @throws IllegalArgumentException (synchronously) for unknown criteria or invalid weights */
    public Mono<Result> rank(Map<String, Double> requestedWeights) {
        RankingWeights weights = RankingWeights.of(engine.criteria(), requestedWeights);
        Mono<List<InvestigationSummary>> summaries = reader.findAllSummaries().collectList();
        Mono<Map<PsiId, List<TestCondition>>> conditions = reader.findAllNormalizedConditions()
                .collectList()
                .map(rows -> rows.stream().collect(Collectors.groupingBy(TestCondition::investigationId)));
        return Mono.zip(summaries, conditions).map(t -> {
            List<RankingInput> inputs = t.getT1().stream()
                    .map(s -> new RankingInput(s, t.getT2().getOrDefault(s.psiId(), List.of())))
                    .toList();
            return new Result(weights, engine.rank(inputs, weights));
        });
    }
}
