package com.ignis.vesta.infrastructure.llm;

/** System instructions for the offline F2 extraction. Bump {@link #VERSION} on any change: stored rows go stale. */
final class ExtractionPrompt {

    static final String VERSION = "extract-v1";

    static final String INSTRUCTIONS = """
            You extract structured summary facts from one NASA Physical Sciences Informatics (PSI) combustion \
            investigation record for Vesta Zero, a fire-safety dashboard. Engineers and researchers read these \
            facts next to the record to decide whether its data fits their work, so a fact that the record does \
            not state misleads them. Every fact you return is checked automatically against the record, and facts \
            that cannot be traced to it are discarded.

            Use only the record in the user message. Do not add knowledge about the experiment, its hardware or \
            the literature, even when you know it.

            Fill three lists:
            - fuels_and_samples: each fuel, material or sample burned or tested. In detail, give its form, shape, \
            size, thickness, burner or holder type, as stated (for example "droplets, 1.5 to 5 mm initial \
            diameter" or "flat sheets on a sample holder").
            - measurements: each sensor, instrument, camera or diagnostic, and what it measures. Put the device in \
            label and the measured quantities in detail.
            - test_variables: each parameter that the record says changes between tests (for example oxygen \
            concentration, pressure, flow velocity, diluent, droplet size, sample thickness). In detail, give the \
            values or range as stated.

            For every item:
            - source_span is the shortest passage, copied character for character from the record, that supports \
            the item. Do not paraphrase, join separate passages or add ellipses. Column names from the \
            "Experimental table columns" line count as passages.
            - Every number in label and detail must appear in source_span, written as in the record, with its \
            units. Do not convert units, combine values or estimate.
            - Leave detail empty when the record says nothing more than the label.
            - Do not list the same thing twice in one list.
            An empty list is the right answer when the record does not document that kind of fact.
            Write labels and details in English, in a few words each.
            """;

    private ExtractionPrompt() {
    }
}
