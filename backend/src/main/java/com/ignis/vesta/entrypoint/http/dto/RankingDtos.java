package com.ignis.vesta.entrypoint.http.dto;

import com.ignis.vesta.application.RankInvestigationsUseCase;
import com.ignis.vesta.domain.ranking.HabitatEnvironment;
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

    /**
     * {@code environment} defaults to the lunar base; {@code weights} may be omitted or partial, any non-negative
     * scale, normalized server-side.
     */
    public record RankingRequest(String environment, Map<String, Double> weights) {
        public Map<String, Double> weightsOrEmpty() {
            return weights == null ? Map.of() : weights;
        }
    }

    public record CriterionDto(String id, String name, String description, double defaultWeight, String provenance) {
        public static CriterionDto from(RankingCriterion c) {
            return new CriterionDto(c.id(), c.name(), c.description(), c.defaultWeight(), c.provenance().name());
        }
    }

    /** A crew environment and its target atmosphere. {@code assumption} marks targets not given by the challenge. */
    public record EnvironmentDto(String id, String name, double o2Fraction, String o2Bound, double pressurePsi,
                                 double pressureAtm, String pressureBound, String basis, boolean assumption) {
        public static EnvironmentDto from(HabitatEnvironment e) {
            return new EnvironmentDto(e.name(), e.displayName(), e.o2Fraction(), e.o2Bound().name(), e.pressurePsi(),
                    e.pressureAtm(), e.pressureBound().name(), e.basis(), e.assumption());
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

    public record RankingResponse(Map<String, Double> weights, EnvironmentDto environment,
                                  String partialGravityNotice, List<RankedDto> results) {
        public static RankingResponse from(RankInvestigationsUseCase.Result result) {
            return new RankingResponse(result.weights().asMap(), EnvironmentDto.from(result.environment()),
                    PARTIAL_GRAVITY_NOTICE,
                    result.ranked().stream().map(RankedDto::from).toList());
        }
    }
}
