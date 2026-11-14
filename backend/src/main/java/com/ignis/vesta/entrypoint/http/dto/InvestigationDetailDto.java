package com.ignis.vesta.entrypoint.http.dto;

import com.ignis.vesta.domain.catalog.InvestigationType;
import com.ignis.vesta.domain.model.ConditionsSummary;
import com.ignis.vesta.domain.model.IngestionWarning;
import com.ignis.vesta.domain.model.Investigation;
import com.ignis.vesta.domain.model.InvestigationDetail;
import com.ignis.vesta.domain.model.Publication;

import java.time.LocalDate;
import java.util.List;

/**
 * Investigation detail (spec F2). Text fields and dates are documented by PSI; {@code classification} is curated;
 * {@code conditions} is derived from documented experimental tables.
 */
public record InvestigationDetailDto(
        String psiId,
        String acronym,
        String title,
        String doi,
        String doiUrl,
        String citation,
        String type,
        String projectType,
        String researchArea,
        String subArea,
        String flightProgram,
        String hardware,
        String objective,
        String approach,
        String hypothesis,
        String researchImpacts,
        LocalDate startDate,
        LocalDate endDate,
        String license,
        List<PublicationDto> publications,
        InvestigationSummaryDto.Classification classification,
        Conditions conditions,
        List<WarningDto> dataQualityWarnings) {

    public record PublicationDto(String title, String authors, String doi, String doiUrl) {

        static PublicationDto from(Publication p) {
            return new PublicationDto(p.title(), p.authors(), blankToNull(p.doi()), resolveDoiUrl(p.doi()));
        }
    }

    public record Conditions(int testRows, long normalizedRows, RangeDto o2Fraction, RangeDto pressureAtm,
                             RangeDto flowCmPerS, List<String> fuelsOrMaterials, List<String> sourceFiles) {

        static Conditions from(ConditionsSummary s) {
            return new Conditions(s.testRows(), s.normalizedRows(), RangeDto.from(s.o2Fraction()),
                    RangeDto.from(s.pressureAtm()), RangeDto.from(s.flowCmPerS()), s.fuelsOrMaterials(),
                    s.sourceFiles());
        }
    }

    public record WarningDto(String sourceFile, Integer rowIndex, String column, String rawValue, String reason) {

        static WarningDto from(IngestionWarning w) {
            return new WarningDto(w.sourceFile(), w.rowIndex(), w.columnName(), w.rawValue(), w.reason());
        }
    }

    public static InvestigationDetailDto from(InvestigationDetail detail) {
        Investigation i = detail.investigation();
        var c = i.classification();
        return new InvestigationDetailDto(
                i.psiId().value(),
                i.acronym(),
                i.title(),
                i.doi().value(),
                i.doi().url(),
                citation(i),
                InvestigationType.fromProjectType(i.projectType()).name(),
                i.projectType(),
                i.researchArea(),
                i.subArea(),
                i.flightProgram(),
                i.hardware(),
                i.objective(),
                i.approach(),
                i.hypothesis(),
                i.researchImpacts(),
                i.startDate(),
                i.endDate(),
                i.license(),
                i.publications().stream().map(PublicationDto::from).toList(),
                new InvestigationSummaryDto.Classification(c.hardwareFamily().name(), c.fuelState().name(),
                        c.safetyPillar().name(), "CURATED"),
                Conditions.from(detail.conditions()),
                detail.warnings().stream().map(WarningDto::from).toList());
    }

    /** Uses only documented fields; no authors are invented. */
    private static String citation(Investigation i) {
        String name = i.acronym() == null ? i.title() : i.title() + " (" + i.acronym() + ")";
        return name + ". NASA Physical Sciences Informatics (PSI), " + i.psiId().value() + ". " + i.doi().url();
    }

    private static String resolveDoiUrl(String doi) {
        if (doi == null || doi.isBlank()) {
            return null;
        }
        String value = doi.trim();
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        return value.startsWith("10.") ? "https://doi.org/" + value : null;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
