package com.ignis.vesta.entrypoint.http.dto;

import com.ignis.vesta.application.RankInvestigationsUseCase;
import com.ignis.vesta.domain.ranking.LunarHabitatTarget;
import com.ignis.vesta.domain.ranking.RankedInvestigation;
import com.ignis.vesta.domain.ranking.RankingCriterion;

import java.util.List;
import java.util.Map;

/** Request and response bodies for the ranking API (spec F3). */
public final class RankingDtos {

    public static final String PARTIAL_GRAVITY_NOTICE = "PSI has no partial-gravity data yet (expected from LUCI "
            + "and FM2). E-FIELD only mimics partial gravity with electric fields. Microgravity and 1-g results "
            + "bound the intermediate range.";

    private RankingDtos() {
    }

    /** {@code weights} may be omitted or partial; any non-negative scale, normalized server-side. */
    public record RankingRequest(Map<String, Double> weights) {
        public Map<String, Double> weightsOrEmpty() {
            return weights == null ? Map.of() : weights;
        }
    }

    public record CriterionDto(String id, String name, String description, double defaultWeight, String provenance) {
        public static CriterionDto from(RankingCriterion c) {
            return new CriterionDto(c.id(), c.name(), c.description(), c.defaultWeight(), c.provenance().name());
        }
    }

    public record TargetDto(double o2Fraction, double pressureAtm, double pressurePsi) {
        static TargetDto lunar() {
            return new TargetDto(LunarHabitatTarget.O2_FRACTION, LunarHabitatTarget.PRESSURE_ATM, 10.2);
        }
    }

    public record ContributionDto(String criterionId, String criterionName, String provenance, double weight,
                                  double score, double contribution, String reason) {
        static ContributionDto from(RankedInvestigation.Contribution c) {
            return new ContributionDto(c.criterion().id(), c.criterion().name(), c.criterion().provenance().name(),
                    c.weight(), c.score(), c.contribution(), c.reason());
        }
    }

    public record RankedDto(int position, String psiId, String acronym, String title, String doiUrl, String type,
                            double score, List<ContributionDto> breakdown) {
        static RankedDto from(RankedInvestigation r) {
            var s = r.summary();
            return new RankedDto(r.position(), s.psiId().value(), s.acronym(), s.title(), s.doi().url(),
                    s.type().name(), r.score(), r.breakdown().stream().map(ContributionDto::from).toList());
        }
    }

    public record RankingResponse(Map<String, Double> weights, TargetDto lunarTarget, String partialGravityNotice,
                                  List<RankedDto> results) {
        public static RankingResponse from(RankInvestigationsUseCase.Result result) {
            return new RankingResponse(result.weights().asMap(), TargetDto.lunar(), PARTIAL_GRAVITY_NOTICE,
                    result.ranked().stream().map(RankedDto::from).toList());
        }
    }
}
