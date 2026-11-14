package com.ignis.vesta.application;

import java.util.List;

/** Summary of one ingestion run, one line per catalog entry. */
public record IngestionReport(List<Line> lines) {

    public record Line(String psiId, boolean stored, int tables, int rows, long rowsWithConditions, int warnings,
                       String error) {

        public static Line failed(String psiId, String error) {
            return new Line(psiId, false, 0, 0, 0, 0, error);
        }
    }

    public IngestionReport {
        lines = List.copyOf(lines);
    }

    public long storedCount() {
        return lines.stream().filter(Line::stored).count();
    }

    public long failedCount() {
        return lines.size() - storedCount();
    }
}
