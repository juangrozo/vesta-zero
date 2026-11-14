package com.ignis.vesta.entrypoint.cli;

import com.ignis.vesta.application.ExtractInvestigationFactsUseCase;
import com.ignis.vesta.domain.catalog.CatalogEntry;
import com.ignis.vesta.domain.catalog.FireInvestigationCatalog;
import com.ignis.vesta.domain.vo.PsiId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Runs the AI extraction for the detail view once and exits. Requires a completed ingestion. Start with
 * {@code ./gradlew bootRun --args='--spring.profiles.active=extract'}; add {@code --force} to redo
 * investigations already extracted with the current model and prompt version, and {@code --only=PSI-69,PSI-25}
 * to limit the run.
 */
@Component
@Profile("extract")
public class ExtractionRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ExtractionRunner.class);

    private final ExtractInvestigationFactsUseCase extract;

    public ExtractionRunner(ExtractInvestigationFactsUseCase extract) {
        this.extract = extract;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean force = args.containsOption("force");
        var only = args.containsOption("only")
                ? args.getOptionValues("only").stream()
                        .flatMap(v -> java.util.Arrays.stream(v.split(",")))
                        .map(v -> PsiId.of(v.trim().toUpperCase(java.util.Locale.ROOT)))
                        .toList()
                : null;
        var ids = FireInvestigationCatalog.entries().stream().map(CatalogEntry::psiId)
                .filter(id -> only == null || only.contains(id))
                .toList();
        log.info("Extracting summary facts for {} investigations (force={})", ids.size(), force);
        var lines = extract.extractAll(ids, force).collectList().block();
        if (lines == null) {
            throw new IllegalStateException("Extraction produced no report");
        }
        for (var line : lines) {
            log.info(String.format("%-8s %-9s accepted=%-3d rejected=%-3d %s", line.psiId(), line.status(),
                    line.accepted(), line.rejected().size(), line.error() == null ? "" : line.error()));
            line.rejected().forEach(r -> log.info("    rejected {} '{}': {}", r.fact().field(), r.fact().label(),
                    r.reason()));
        }
        long failed = lines.stream().filter(l -> "failed".equals(l.status())).count();
        log.info("Extraction finished: {} extracted, {} skipped, {} failed",
                lines.stream().filter(l -> "extracted".equals(l.status())).count(),
                lines.stream().filter(l -> "skipped".equals(l.status())).count(), failed);
    }
}
