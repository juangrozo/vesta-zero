package com.ignis.vesta.application;

import com.ignis.vesta.domain.model.ConditionsSummary;
import com.ignis.vesta.domain.model.InvestigationComparison;
import com.ignis.vesta.domain.model.InvestigationDetail;
import com.ignis.vesta.domain.port.InvestigationDetailReader;
import com.ignis.vesta.domain.vo.PsiId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Side-by-side comparison of 2–3 investigations (spec F4). The deterministic part never depends on the language
 * model; the narrative is a separate, grounded assistant answer limited to the selected investigations.
 */
public class CompareInvestigationsUseCase {

    /** One or more requested investigations are not in the catalog. */
    public static final class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }

    private final InvestigationDetailReader details;
    private final AskAssistantUseCase assistant;

    public CompareInvestigationsUseCase(InvestigationDetailReader details, AskAssistantUseCase assistant) {
        this.details = Objects.requireNonNull(details);
        this.assistant = Objects.requireNonNull(assistant);
    }

    /** @throws IllegalArgumentException (synchronously) for fewer than 2, more than 3, or malformed IDs */
    public Mono<InvestigationComparison> compare(List<String> rawIds) {
        List<PsiId> ids = validate(rawIds);
        return Flux.fromIterable(ids)
                .concatMap(id -> details.findById(id).flatMap(inv -> details.findTestConditions(id).collectList()
                        .map(rows -> new InvestigationDetail(inv, ConditionsSummary.of(rows), List.of()))))
                .collectList()
                .flatMap(found -> {
                    if (found.size() != ids.size()) {
                        List<String> present = found.stream().map(d -> d.investigation().psiId().value()).toList();
                        String missing = ids.stream().map(PsiId::value).filter(id -> !present.contains(id))
                                .collect(Collectors.joining(", "));
                        return Mono.error(new NotFoundException("Not in the Vesta Zero catalog: " + missing));
                    }
                    return Mono.just(InvestigationComparison.of(found));
                });
    }

    /** AI comparison paragraph, grounded and cached like any assistant answer. */
    public Mono<AskAssistantUseCase.Result> narrative(List<String> rawIds) {
        return compare(rawIds).flatMap(comparison -> assistant.ask(questionFor(comparison)));
    }

    static String questionFor(InvestigationComparison comparison) {
        String names = comparison.investigations().stream()
                .map(d -> d.investigation().psiId() + (d.investigation().acronym() == null ? ""
                        : " (" + d.investigation().acronym() + ")"))
                .collect(Collectors.joining(", "));
        return "Compare these investigations: " + names + ". Cover their objectives; fuels and sample shapes or "
                + "sizes; test conditions (oxygen concentration, pressure, flow velocity); hardware; sensors and "
                + "measurements; and what is similar and what is different between them. Cite only these "
                + "investigations, and say which of these aspects are not documented.";
    }

    private static List<PsiId> validate(List<String> rawIds) {
        if (rawIds == null) {
            throw new IllegalArgumentException("psiIds is required");
        }
        LinkedHashSet<String> unique = rawIds.stream().filter(Objects::nonNull)
                .map(s -> s.strip().toUpperCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (unique.size() < InvestigationComparison.MIN_ITEMS || unique.size() > InvestigationComparison.MAX_ITEMS) {
            throw new IllegalArgumentException("Select between " + InvestigationComparison.MIN_ITEMS + " and "
                    + InvestigationComparison.MAX_ITEMS + " different investigations");
        }
        List<PsiId> ids = new ArrayList<>();
        for (String raw : unique) {
            try {
                ids.add(PsiId.of(raw));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid PSI id '" + raw + "'");
            }
        }
        return ids;
    }
}
