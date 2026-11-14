package com.ignis.vesta.entrypoint.http.dto;

import com.ignis.vesta.application.AskAssistantUseCase;
import com.ignis.vesta.domain.assistant.AssistantAnswer;
import com.ignis.vesta.domain.model.InvestigationSummary;

import java.util.List;

/** Request and response bodies for the assistant (spec F5). Citation DOIs come from the database, never the model. */
public final class AssistantDtos {

    private AssistantDtos() {
    }

    public record AskRequest(String question) {
    }

    public record CitationDto(String psiId, String acronym, String title, String doi, String doiUrl) {
        static CitationDto from(InvestigationSummary s) {
            return new CitationDto(s.psiId().value(), s.acronym(), s.title(), s.doi().value(), s.doi().url());
        }
    }

    public record AskResponse(
            String answer,
            String provenance,
            List<CitationDto> citations,
            List<String> droppedCitations,
            List<String> unverifiedNumbers,
            boolean insufficientData,
            boolean declined,
            String partialGravityNotice,
            String model,
            boolean cached) {

        public static AskResponse from(AskAssistantUseCase.Result result) {
            AssistantAnswer a = result.answer();
            return new AskResponse(
                    a.text(),
                    "AI_EXTRACTED",
                    result.cited().values().stream().map(CitationDto::from).toList(),
                    a.droppedCitations(),
                    a.unverifiedNumbers(),
                    a.insufficientData(),
                    a.declined(),
                    a.partialGravityNotice() ? RankingDtos.PARTIAL_GRAVITY_NOTICE : null,
                    a.model(),
                    a.fromCache());
        }
    }
}
