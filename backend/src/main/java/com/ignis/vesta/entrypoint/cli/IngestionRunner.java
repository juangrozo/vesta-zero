package com.ignis.vesta.entrypoint.cli;

import com.ignis.vesta.application.IngestInvestigationsUseCase;
import com.ignis.vesta.application.IngestionReport;
import com.ignis.vesta.domain.catalog.FireInvestigationCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Runs the PSI ingestion once and exits. Start with the {@code ingest} profile:
 * {@code ./gradlew bootRun --args='--spring.profiles.active=ingest'}.
 */
@Component
@Profile("ingest")
public class IngestionRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(IngestionRunner.class);

    private final IngestInvestigationsUseCase ingestInvestigations;

    public IngestionRunner(IngestInvestigationsUseCase ingestInvestigations) {
        this.ingestInvestigations = ingestInvestigations;
    }

    @Override
    public void run(ApplicationArguments args) {
        var entries = FireInvestigationCatalog.entries();
        log.info("Ingesting {} investigations from PSI", entries.size());
        IngestionReport report = ingestInvestigations.ingest(entries).block();
        if (report == null) {
            throw new IllegalStateException("Ingestion produced no report");
        }
        log.info(String.format("%-8s %-7s %6s %6s %10s %8s  %s", "PSI", "stored", "tables", "rows", "w/values",
                "warnings", "error"));
        report.lines().forEach(l -> log.info(String.format("%-8s %-7s %6d %6d %10d %8d  %s", l.psiId(), l.stored(),
                l.tables(), l.rows(), l.rowsWithConditions(), l.warnings(), l.error() == null ? "" : l.error())));
        log.info("Ingestion finished: {} stored, {} failed", report.storedCount(), report.failedCount());
    }
}
