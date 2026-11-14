package com.ignis.vesta.infrastructure.psi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ignis.vesta.domain.catalog.CatalogEntry;
import com.ignis.vesta.domain.model.ExperimentalTableRef;
import com.ignis.vesta.domain.model.Investigation;
import com.ignis.vesta.domain.model.Publication;
import com.ignis.vesta.domain.vo.Doi;
import com.ignis.vesta.domain.vo.PsiId;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Maps the (undocumented) PSI investigation JSON to the domain model and sanitizes the stored copy. */
final class PsiRecordMapper {

    private static final DateTimeFormatter PSI_DATE = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    /** File lists are large (up to MBs) and not needed; experimentalTable is reduced to name and version. */
    private static final Set<String> DROPPED_FIELDS = Set.of("added", "removed", "images", "modified");
    /** Only these contact fields are kept: no emails, phones, fax or postal addresses. */
    private static final Set<String> CONTACT_FIELDS = Set.of("firstName", "midInitials", "lastName", "affiliation", "roles");

    private final ObjectMapper objectMapper;

    PsiRecordMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    Investigation toInvestigation(JsonNode json, CatalogEntry entry) {
        PsiId accession = PsiId.of(text(json, "accession"));
        if (!accession.equals(entry.psiId())) {
            throw new PsiAccessException("PSI returned " + accession + " for " + entry.psiId());
        }
        List<ExperimentalTableRef> tables = tables(json);
        return new Investigation(
                accession,
                blankToNull(text(json, "investigationAcronym")),
                text(json, "title"),
                new Doi(text(json, "doi")),
                blankToNull(text(json, "projectType")),
                blankToNull(text(json, "researchArea")),
                blankToNull(text(json, "subResearchArea")),
                blankToNull(text(json, "flightProgram")),
                blankToNull(text(json, "experimentHardware")),
                blankToNull(text(json, "objective")),
                blankToNull(text(json, "approach")),
                blankToNull(text(json, "hypothesis")),
                blankToNull(text(json, "researchImpacts")),
                date(text(json, "investigationStartDate")),
                date(text(json, "investigationEndDate")),
                blankToNull(text(json, "licenseIdentifier")),
                publications(json),
                tables,
                entry,
                sanitize(json, tables));
    }

    private String sanitize(JsonNode json, List<ExperimentalTableRef> tables) {
        ObjectNode copy = json.deepCopy();
        DROPPED_FIELDS.forEach(copy::remove);
        ArrayNode contacts = objectMapper.createArrayNode();
        json.path("contacts").forEach(contact -> {
            ObjectNode kept = objectMapper.createObjectNode();
            CONTACT_FIELDS.forEach(field -> {
                if (contact.has(field)) {
                    kept.set(field, contact.get(field));
                }
            });
            contacts.add(kept);
        });
        copy.set("contacts", contacts);
        copy.set("experimentalTable", objectMapper.valueToTree(tables));
        return copy.toString();
    }

    private static List<ExperimentalTableRef> tables(JsonNode json) {
        List<ExperimentalTableRef> tables = new ArrayList<>();
        json.path("experimentalTable").forEach(node -> {
            String fileName = text(node, "file_name");
            if (!fileName.isBlank()) {
                tables.add(new ExperimentalTableRef(fileName, node.path("version").asInt(1)));
            }
        });
        return tables;
    }

    private static List<Publication> publications(JsonNode json) {
        List<Publication> publications = new ArrayList<>();
        json.path("publications").forEach(node -> publications.add(new Publication(
                text(node, "title").trim(), text(node, "authorList").trim(), text(node, "doi").trim())));
        return publications;
    }

    private static LocalDate date(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), PSI_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isValueNode() ? value.asText() : "";
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
