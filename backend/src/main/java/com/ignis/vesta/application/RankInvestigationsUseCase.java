package com.ignis.vesta.application;

import com.ignis.vesta.domain.model.InvestigationSummary;
import com.ignis.vesta.domain.model.TestCondition;
import com.ignis.vesta.domain.port.InvestigationCatalogReader;
import com.ignis.vesta.domain.ranking.HabitatEnvironment;
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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Explainable ranking of all catalog investigations by relevance to a crew environment (spec F3). The criteria
 * depend on the environment (its target atmosphere), so an engine is built per request; building one is cheap.
 */
public class RankInvestigationsUseCase {

    public record Result(HabitatEnvironment environment, RankingWeights weights, List<RankedInvestigation> ranked) {
    }

    private final InvestigationCatalogReader reader;
    private final Function<HabitatEnvironment, List<RankingCriterion>> criteriaFactory;

    public RankInvestigationsUseCase(InvestigationCatalogReader reader,
                                     Function<HabitatEnvironment, List<RankingCriterion>> criteriaFactory) {
        this.reader = Objects.requireNonNull(reader);
        this.criteriaFactory = Objects.requireNonNull(criteriaFactory);
    }

    public List<RankingCriterion> criteria(HabitatEnvironment environment) {
        return criteriaFactory.apply(environment);
    }

    /** @throws IllegalArgumentException (synchronously) for unknown criteria or invalid weights */
    public Mono<Result> rank(HabitatEnvironment environment, Map<String, Double> requestedWeights) {
        RankingEngine engine = new RankingEngine(criteriaFactory.apply(Objects.requireNonNull(environment)));
        RankingWeights weights = RankingWeights.of(engine.criteria(), requestedWeights);
        Mono<List<InvestigationSummary>> summaries = reader.findAllSummaries().collectList();
        Mono<Map<PsiId, List<TestCondition>>> conditions = reader.findAllNormalizedConditions()
                .collectList()
                .map(rows -> rows.stream().collect(Collectors.groupingBy(TestCondition::investigationId)));
        return Mono.zip(summaries, conditions).map(t -> {
            List<RankingInput> inputs = t.getT1().stream()
                    .map(s -> new RankingInput(s, t.getT2().getOrDefault(s.psiId(), List.of())))
                    .toList();
            return new Result(environment, weights, engine.rank(inputs, weights));
        });
    }
}
