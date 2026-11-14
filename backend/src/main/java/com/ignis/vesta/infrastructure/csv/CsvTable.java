package com.ignis.vesta.infrastructure.csv;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/** A decoded CSV: trimmed headers and rows keyed by header, in file order. */
record CsvTable(List<String> headers, List<Map<String, String>> rows, String encoding) {

    CsvTable {
        headers = List.copyOf(headers);
        rows = List.copyOf(rows);
    }

    /** Finds a header ignoring case and repeated whitespace (PSI headers contain double spaces). */
    java.util.Optional<String> findHeader(String wanted) {
        String key = normalize(wanted);
        return headers.stream().filter(h -> normalize(h).equals(key)).findFirst();
    }

    static String normalize(String header) {
        return header.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
