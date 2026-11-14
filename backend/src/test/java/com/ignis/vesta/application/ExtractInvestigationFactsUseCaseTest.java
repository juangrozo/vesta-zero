package com.ignis.vesta.application;

import com.ignis.vesta.domain.catalog.CatalogEntry;
import com.ignis.vesta.domain.catalog.FuelState;
import com.ignis.vesta.domain.catalog.HardwareFamily;
import com.ignis.vesta.domain.catalog.SafetyPillar;
import com.ignis.vesta.domain.extraction.ExtractedFact;
import com.ignis.vesta.domain.extraction.ExtractionField;
import com.ignis.vesta.domain.extraction.ExtractionSource;
import com.ignis.vesta.domain.extraction.InvestigationExtraction;
import com.ignis.vesta.domain.model.IngestionWarning;
import com.ignis.vesta.domain.model.Investigation;
import com.ignis.vesta.domain.model.TestCondition;
import com.ignis.vesta.domain.port.ExtractionRepository;
import com.ignis.vesta.domain.port.FactExtractor;
import com.ignis.vesta.domain.port.InvestigationDetailReader;
import com.ignis.vesta.domain.port.LlmGateway.LlmUnavailableException;
import com.ignis.vesta.domain.vo.Doi;
import com.ignis.vesta.domain.vo.PsiId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ExtractInvestigationFactsUseCaseTest {

    private static final PsiId FLEX = PsiId.of("PSI-69");
    private static final PsiId MISSING = PsiId.of("PSI-999");

    private final Map<PsiId, InvestigationExtraction> stored = new HashMap<>();
    private final AtomicInteger calls = new AtomicInteger();
    private final List<ExtractionSource> sources = new ArrayList<>();

    private ExtractInvestigationFactsUseCase useCase(boolean failing) {
        FactExtractor extractor = new FactExtractor() {
            @Override
            public Mono<List<ExtractedFact>> extract(ExtractionSource source) {
                calls.incrementAndGet();
                sources.add(source);
                if (failing) {
                    return Mono.error(new LlmUnavailableException("Claude could not be reached"));
                }
                return Mono.just(List.of(
                        new ExtractedFact(ExtractionField.FUELS_AND_SAMPLES, "Heptane droplets", "", "heptane droplets"),
                        new ExtractedFact(ExtractionField.MEASUREMENTS, "Thermocouple", "", "thermocouples")));
            }

            @Override
            public String model() {
                return "test-model";
            }

            @Override
            public String promptVersion() {
                return "extract-test";
            }
        };
        ExtractionRepository repository = new ExtractionRepository() {
            @Override
            public Mono<Void> replace(InvestigationExtraction extraction) {
                return Mono.fromRunnable(() -> stored.put(extraction.psiId(), extraction));
            }

            @Override
            public Mono<Boolean> exists(PsiId psiId, String model, String promptVersion) {
                InvestigationExtraction e = stored.get(psiId);
                return Mono.just(e != null && e.model().equals(model) && e.promptVersion().equals(promptVersion));
            }

            @Override
            public Mono<InvestigationExtraction> find(PsiId psiId) {
                return Mono.justOrEmpty(stored.get(psiId));
            }
        };
        return new ExtractInvestigationFactsUseCase(new Details(), extractor, repository,
                Clock.fixed(Instant.parse("2026-11-14T20:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void storesOnlyVerifiedFactsAndReportsRejections() {
        var lines = useCase(false).extractAll(List.of(FLEX), false).collectList().block();

        assertThat(lines).singleElement().satisfies(line -> {
            assertThat(line.status()).isEqualTo("extracted");
            assertThat(line.accepted()).isEqualTo(1);
            assertThat(line.rejected()).singleElement()
                    .satisfies(r -> assertThat(r.fact().label()).isEqualTo("Thermocouple"));
        });
        assertThat(stored.get(FLEX).facts()).extracting(ExtractedFact::label).containsExactly("Heptane droplets");
        assertThat(sources.get(0).text()).contains("Experimental table columns: Test # | Fuel");
        assertThat(sources.get(0).text()).contains("Fuels and materials listed in the experimental tables: heptane");
    }

    @Test
    void secondRunSkipsWithoutCallingTheModelUnlessForced() {
        var useCase = useCase(false);
        useCase.extractAll(List.of(FLEX), false).blockLast();
        var again = useCase.extractAll(List.of(FLEX), false).blockLast();
        assertThat(again.status()).isEqualTo("skipped");
        assertThat(calls.get()).isEqualTo(1);

        useCase.extractAll(List.of(FLEX), true).blockLast();
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void failuresAreReportedAndTheRunContinues() {
        var lines = useCase(true).extractAll(List.of(MISSING, FLEX), false).collectList().block();
        assertThat(lines).extracting(ExtractInvestigationFactsUseCase.Line::status)
                .containsExactly("failed", "failed");
        assertThat(lines.get(0).error()).contains("not stored");
        assertThat(lines.get(1).error()).isEqualTo("Claude could not be reached");
        assertThat(stored).isEmpty();
    }

    private static final class Details implements InvestigationDetailReader {
        @Override
        public Mono<Investigation> findById(PsiId psiId) {
            if (!FLEX.equals(psiId)) {
                return Mono.empty();
            }
            return Mono.just(new Investigation(FLEX, "FLEX", "Flame Extinguishment Experiment",
                    new Doi("10.60555/flex"), "Microgravity Investigation", "Combustion Science", "Fire",
                    null, "MDCA", "Study heptane droplets burning in microgravity.", null, null, null, null, null,
                    "CC0-1.0", List.of(), List.of(),
                    new CatalogEntry(FLEX, HardwareFamily.CIR_MDCA, FuelState.LIQUID_DROPLET,
                            SafetyPillar.SUPPRESSION), "{}"));
        }

        @Override
        public Flux<TestCondition> findTestConditions(PsiId psiId) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("Test #", "1");
            row.put("Fuel", "heptane");
            return Flux.just(new TestCondition(psiId, "flex.csv", 0, "1", "heptane", null, null, null, row));
        }

        @Override
        public Flux<IngestionWarning> findWarnings(PsiId psiId) {
            return Flux.empty();
        }
    }
}
