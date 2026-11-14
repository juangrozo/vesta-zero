package com.ignis.vesta.application;

import com.ignis.vesta.domain.model.ConditionsSummary;
import com.ignis.vesta.domain.model.InvestigationDetail;
import com.ignis.vesta.domain.model.TestCondition;
import com.ignis.vesta.domain.port.InvestigationDetailReader;
import com.ignis.vesta.domain.vo.PsiId;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

/** Detail of one investigation and its test conditions (spec F2). Empty result means "not found". */
public class GetInvestigationUseCase {

    private final InvestigationDetailReader reader;

    public GetInvestigationUseCase(InvestigationDetailReader reader) {
        this.reader = Objects.requireNonNull(reader);
    }

    public Mono<InvestigationDetail> detail(PsiId psiId) {
        return reader.findById(psiId).flatMap(investigation -> Mono.zip(
                        reader.findTestConditions(psiId).collectList(),
                        reader.findWarnings(psiId).collectList())
                .map(t -> new InvestigationDetail(investigation, ConditionsSummary.of(t.getT1()), t.getT2())));
    }

    /** Empty when the investigation is unknown; otherwise its rows (possibly none, at most a few hundred). */
    public Mono<List<TestCondition>> testConditions(PsiId psiId) {
        return reader.findById(psiId).flatMap(found -> reader.findTestConditions(psiId).collectList());
    }
}
