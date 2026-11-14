package com.ignis.vesta.infrastructure.llm;

/** System instructions for the offline F2 extraction. Bump {@link #VERSION} on any change: stored rows go stale. */
final class ExtractionPrompt {

    static final String VERSION = "extract-v2";

    static final String INSTRUCTIONS = """
            You extract structured summary facts from one NASA Physical Sciences Informatics (PSI) combustion \
            investigation record for Vesta Zero, a fire-safety dashboard. Engineers and researchers read these \
            facts next to the record to decide whether its data fits their work, so a fact that the record does \
            not state misleads them. Every fact you return is checked automatically against the record, and facts \
            that cannot be traced to it are discarded.

            Use only the record in the user message. Do not add knowledge about the experiment, its hardware or \
            the literature, even when you know it.

            Fill five lists:
            - fuels_and_samples: each fuel, material or sample burned or tested. In detail, give its form, shape, \
            size, thickness, burner or holder type, as stated (for example "droplets, 1.5 to 5 mm initial \
            diameter" or "flat sheets on a sample holder"). Take fuels from the descriptive text first. The \
            "Fuels and materials listed in the experimental tables" line is already shown to readers as a table, \
            so use it only for fuels the text does not mention, and add at most five items from it.
            - measurements: each sensor, instrument, camera or diagnostic, and what it measures. Put the device in \
            label and the measured quantities in detail.
            - test_variables: each parameter that the record says changes between tests (for example oxygen \
            concentration, pressure, flow velocity, diluent, droplet size, sample thickness). In detail, give the \
            values or range as stated.
            - test_constants: each condition that the record says stays the same across tests (for example a fixed \
            pressure, the same sample holder, flow duct or burner, a fixed fuel). In detail, give the value as \
            stated. Only list what the record states is shared; do not infer it from missing variation.
            - earth_applications: each use of the research on Earth that the record states (for example smoke \
            detectors, fire suppression, fire codes, wildfire or building fire modeling, engines, energy). Put the \
            application in label and what the record says about it in detail.

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
