package com.ignis.vesta.application;

import com.ignis.vesta.domain.assistant.AnswerGrounding;
import com.ignis.vesta.domain.assistant.AssistantAnswer;
import com.ignis.vesta.domain.assistant.CorpusRenderer;
import com.ignis.vesta.domain.assistant.EvidenceCorpus;
import com.ignis.vesta.domain.model.ConditionsSummary;
import com.ignis.vesta.domain.model.InvestigationDetail;
import com.ignis.vesta.domain.model.InvestigationSummary;
import com.ignis.vesta.domain.port.InvestigationCatalogReader;
import com.ignis.vesta.domain.port.InvestigationDetailReader;
import com.ignis.vesta.domain.port.LlmGateway;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Grounded Q&A over the stored investigations (spec F5). The whole catalog fits in one prompt, so there is no
 * retrieval step: the model sees every record, and the answer is checked against that same corpus.
 */
public class AskAssistantUseCase {

    public static final int MIN_QUESTION_LENGTH = 5;
    public static final int MAX_QUESTION_LENGTH = 1000;
    private static final Duration CORPUS_TTL = Duration.ofMinutes(10);

    public record Result(AssistantAnswer answer, Map<String, InvestigationSummary> cited) {
    }

    private final InvestigationCatalogReader catalog;
    private final LlmGateway llm;
    private final Mono<EvidenceCorpus> corpus;

    public AskAssistantUseCase(InvestigationCatalogReader catalog, InvestigationDetailReader details, LlmGateway llm) {
        this.catalog = Objects.requireNonNull(catalog);
        this.llm = Objects.requireNonNull(llm);
        this.corpus = catalog.findAllSummaries()
                .concatMap(s -> loadDetail(details, s))
                .collectList()
                .map(CorpusRenderer::render)
                .cache(CORPUS_TTL);
    }

    /** @throws IllegalArgumentException (synchronously) when the question is empty or too long */
    public Mono<Result> ask(String question) {
        String q = question == null ? "" : question.strip();
        if (q.length() < MIN_QUESTION_LENGTH || q.length() > MAX_QUESTION_LENGTH) {
            throw new IllegalArgumentException("Question must be between " + MIN_QUESTION_LENGTH + " and "
                    + MAX_QUESTION_LENGTH + " characters");
        }
        return corpus.flatMap(c -> llm.answer(q, c).map(generated -> AnswerGrounding.ground(generated, c, q)))
                .zipWith(catalog.findAllSummaries().collectMap(s -> s.psiId().value()))
                .map(t -> new Result(t.getT1(), t.getT1().citations().stream()
                        .filter(id -> t.getT2().containsKey(id.value()))
                        .collect(Collectors.toMap(id -> id.value(), id -> t.getT2().get(id.value()),
                                (a, b) -> a, java.util.LinkedHashMap::new))));
    }

    private static Mono<InvestigationDetail> loadDetail(InvestigationDetailReader details, InvestigationSummary s) {
        return details.findById(s.psiId()).flatMap(investigation -> details.findTestConditions(s.psiId())
                .collectList()
                .map(rows -> new InvestigationDetail(investigation, ConditionsSummary.of(rows), List.of())));
    }
}
