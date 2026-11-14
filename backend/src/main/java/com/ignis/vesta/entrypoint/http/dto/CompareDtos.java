package com.ignis.vesta.entrypoint.http.dto;

import com.ignis.vesta.domain.catalog.InvestigationType;
import com.ignis.vesta.domain.model.Investigation;
import com.ignis.vesta.domain.model.InvestigationComparison;
import com.ignis.vesta.domain.model.InvestigationDetail;

import java.time.LocalDate;
import java.util.List;

/** Request and response bodies for the comparator (spec F4). */
public final class CompareDtos {

    private CompareDtos() {
    }

    public record CompareRequest(List<String> psiIds) {
    }

    public record ItemDto(String psiId, String acronym, String title, String doi, String doiUrl, String type,
                          String subArea, String hardware, String flightProgram, String objective,
                          LocalDate startDate, LocalDate endDate,
                          InvestigationSummaryDto.Classification classification,
                          InvestigationDetailDto.Conditions conditions) {

        static ItemDto from(InvestigationDetail d) {
            Investigation i = d.investigation();
            var c = i.classification();
            return new ItemDto(i.psiId().value(), i.acronym(), i.title(), i.doi().value(), i.doi().url(),
                    InvestigationType.fromProjectType(i.projectType()).name(), i.subArea(), i.hardware(),
                    i.flightProgram(), i.objective(), i.startDate(), i.endDate(),
                    new InvestigationSummaryDto.Classification(c.hardwareFamily().name(), c.fuelState().name(),
                            c.safetyPillar().name(), "CURATED"),
                    InvestigationDetailDto.Conditions.from(d.conditions()));
        }
    }

    public record FactDto(String dimension, boolean same, String detail) {
    }

    public record CompareResponse(List<ItemDto> investigations, List<FactDto> facts) {

        public static CompareResponse from(InvestigationComparison comparison) {
            return new CompareResponse(
                    comparison.investigations().stream().map(ItemDto::from).toList(),
                    comparison.facts().stream().map(f -> new FactDto(f.dimension(), f.same(), f.detail())).toList());
        }
    }
}
