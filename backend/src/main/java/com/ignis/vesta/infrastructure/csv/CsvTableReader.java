package com.ignis.vesta.infrastructure.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Decodes PSI CSVs: UTF-8 (with or without BOM) first, cp1252 as fallback (seen in PSI-69). */
final class CsvTableReader {

    private static final Charset CP1252 = Charset.forName("windows-1252");

    private CsvTableReader() {
    }

    static CsvTable read(byte[] content) {
        Decoded decoded = decode(content);
        List<CSVRecord> records;
        try (CSVParser parser = CSVParser.parse(new StringReader(decoded.text()), CSVFormat.RFC4180)) {
            records = parser.getRecords();
        } catch (IOException e) {
            throw new UncheckedIOException("CSV could not be parsed", e);
        }
        List<CSVRecord> nonBlank = records.stream().filter(CsvTableReader::hasContent).toList();
        if (nonBlank.isEmpty()) {
            return new CsvTable(List.of(), List.of(), decoded.encoding());
        }
        List<String> headers = uniqueHeaders(nonBlank.getFirst());
        List<Map<String, String>> rows = new ArrayList<>();
        for (CSVRecord record : nonBlank.subList(1, nonBlank.size())) {
            Map<String, String> row = new LinkedHashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                row.put(headers.get(i), i < record.size() ? record.get(i).trim() : "");
            }
            rows.add(row);
        }
        return new CsvTable(headers, rows, decoded.encoding());
    }

    private static boolean hasContent(CSVRecord record) {
        for (String value : record) {
            if (!value.isBlank()) {
                return true;
            }
        }
        return false;
    }

    private static List<String> uniqueHeaders(CSVRecord headerRecord) {
        List<String> headers = new ArrayList<>();
        Map<String, Integer> seen = new HashMap<>();
        for (int i = 0; i < headerRecord.size(); i++) {
            String header = headerRecord.get(i).trim();
            if (header.isEmpty()) {
                header = "column " + (i + 1);
            }
            int count = seen.merge(header, 1, Integer::sum);
            headers.add(count == 1 ? header : header + " (" + count + ")");
        }
        return headers;
    }

    private static Decoded decode(byte[] content) {
        try {
            String text = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(content))
                    .toString();
            return new Decoded(stripBom(text), "UTF-8");
        } catch (CharacterCodingException e) {
            return new Decoded(stripBom(new String(content, CP1252)), "windows-1252");
        }
    }

    private static String stripBom(String text) {
        return text.startsWith("﻿") ? text.substring(1) : text;
    }

    private record Decoded(String text, String encoding) {
    }
}
