# Vesta Zero — MVP Specification

Status: **approved** (2026-11-14) and kept up to date during the build; later additions are marked with their date.
Architecture: see [architecture.md](architecture.md).

## 1. Goal

A web dashboard that helps teachers, students, researchers and aerospace engineers find, rank, compare and
understand NASA PSI microgravity combustion investigations for **fire safety in crewed habitats**
(spacecraft, stations, lunar/Martian bases, pressurized rovers). Every fact shown carries its source
(PSI ID + DOI) and its provenance (`DOCUMENTED` or `AI_EXTRACTED`).

## 2. Data scope (verified against PSI on 2026-11-14)

PSI search `term=combustion, type=investigation` returns 26 records. After review, **24 fire-related
investigations** are in scope (23 from the initial survey plus PSI-47 DAFT / DAFT-2).

| PSI ID | Acronym | Type | Experimental table (CSV) | Test conditions in CSV |
|---|---|---|---|---|
| PSI-10 | ACME Flame Design | Flight | 2 files (normal, inverse) | O2, pressure |
| PSI-20 | ACME BRE | Flight | 1 | O2 (free text lists) |
| PSI-21 | ACME CLD Flame | Flight | 1 | fuel conc. only |
| PSI-22 | ACME E-FIELD Flames | Flight | 1 | fuel conc., mesh polarity |
| PSI-23 | ACME s-Flame | Flight | 1 | atmosphere as text |
| PSI-159 | ACME CFI-G | Flight | 2 files (normal, inverse) | O2 (up to 0.85), pressure |
| PSI-39 | CFI | Flight | 1 | O2, pressure |
| PSI-68 | FLEX-2 | Flight | 1 (summary ranges) | O2, pressure, flow |
| PSI-69 | FLEX | Flight | 1 | O2, pressure (mmHg), He, CO2 |
| PSI-25 | BASS-II | Flight | 1 | O2/CO2/CO initial-final |
| PSI-26 | BASS | Flight | 1 | material only |
| PSI-106 | SLICE | Flight | 1 | fuel only |
| PSI-107 | SPICE | Flight | 1 | fuel, nozzle, air rate |
| PSI-98 | SAFFIRE-I | Flight | 1 | material, O2, flow |
| PSI-99 | SAFFIRE-II | Flight | 1 | material, O2, flow (values with units) |
| PSI-100 | SAFFIRE-III | Flight | 1 | material, flow |
| PSI-101 | SAME-R | Flight | 1 | sample, inlet velocity |
| PSI-102 | SAME | Flight | 1 | material, air flow |
| PSI-47 | DAFT / DAFT-2 | Flight | 1 | sample material (dust) |
| PSI-142 | Cool flame transitions | Ground | 1 (index of published figures, not tests) | none |
| PSI-60 | SPICE data modeling | Ground | none | none |
| PSI-62 | Concurrent flame spread | Ground | none | none |
| PSI-115 | SAME data modeling | Ground | none | none |
| PSI-117 | Cool flame dynamics (CFD) | Ground | none | none |

**Excluded:** PSI-181 and PSI-77 (soldering), PSI-108 (STDCE-1 convection), PSI-179 (ASE aerosol sampling, not combustion).

Data facts that drive design:
- 20 of 24 investigations have a CSV; columns differ in every file.
- Values mix fractions (`0.21`) and percents (`21.5`), units inside cells (`20 cm/s`, `1 atm`),
  ranges (`21.5 - 21.7`, `0.17; 0.21; 0.25`), approximations (`~ 21.5`) and free-text notes.
- Pressure appears in atm, bar and mmHg. One file (PSI-69) is cp1252-encoded, the rest UTF-8 with BOM.
- Naive parsing produces wrong values (e.g. XO2 = 2, O2 = 3400 %). Mapping must be curated per file and validated.

## 3. MVP features and acceptance criteria

### F1 — Catalog with filters
- Lists the 24 investigations: acronym, title, type (flight/ground), sub-area, hardware, DOI link, CSV available yes/no.
- Filters: type, hardware family (CIR/ACME, MDCA, MSG, Cygnus, other), fuel state (gas, liquid droplet, solid, aerosol), safety pillar.
- **Accept:** all 24 appear; each filter narrows the list correctly; every row links to its DOI (`https://doi.org/<doi>`).

### F2 — Investigation detail
- Objective, approach, hypothesis, research impacts, hardware, flight program, dates, publications (title + DOI), license CC0-1.0, "Cite this investigation" with DOI.
- Test conditions table from the CSV: normalized columns (fuel/material, O2 fraction, pressure atm, flow cm/s) plus the original row on demand.
- AI-extracted summary fields (fuels and sample geometry, sensors/measurements, variables that change between tests) each with a provenance badge and the source text span. Added 2026-11-14 (prompt v2): what stays the same between tests, and applications on Earth.
- Download of the investigation's test conditions as CSV or JSON (added 2026-11-14).
- **Accept:** every value shows a provenance badge; missing values show "not documented", never a guess; the raw CSV row is viewable.

### F3 — Explainable ranking (crew environment relevance)
- Environment presets (added 2026-11-14): spacecraft/station cabin (assumed Earth air, 21 % O2 at 14.7 psi), lunar base and rover (≥ 30 % O2 at ≤ 10.2 psi, default), Martian base (no value in the challenge: assumed lunar, flagged), spacesuit reference (100 % O2 at 4.3 psi). The preset sets the target of the atmosphere criterion (id `atmosphere`); the lunar preset reproduces the original score.
- Deterministic score from the criteria in section 5, weights adjustable with sliders in the UI.
- Each result shows its per-criterion score and a one-line reason (e.g. "max tested O2 0.40 at 0.42 atm").
- **Accept:** same inputs give the same order; changing a weight re-orders the list immediately; every score has a breakdown.

### F4 — Comparator (2–3 investigations)
- Side by side: objectives, fuels, O2 range, pressure range, flow range, hardware, sensors, number of tests.
- An AI paragraph on similarities and differences, with citations.
- **Accept:** ranges come only from normalized documented data; the AI paragraph cites PSI IDs for each claim.

### F5 — AI assistant with citations
- Free-text question; answer is grounded only on stored investigation data (retrieval over our DB, no web).
- Each claim cites PSI ID + DOI. Missing data gets "not documented".
- If the question mentions the Moon, Mars, or partial gravity, the answer includes the partial-gravity gap notice.
- **Accept:** 5 scripted demo questions (section 8) answered with valid citations; answers are cached so re-asking costs no API call.

### F6 — Partial-gravity gap notice
- Persistent banner (and on F3/F5 when relevant): "PSI has no partial-gravity data yet (expected from LUCI and FM2). E-FIELD only mimics partial gravity with electric fields. Microgravity and 1-g results bound the intermediate range."
- **Accept:** visible on home, ranking and any lunar/Martian assistant answer.

## 4. Out of scope

- Raw data files (images, video, sensor dumps). Only detail JSON, experimental-table CSVs and, if needed, final-report PDFs.
- Rockets and propellants (outside PSI).
- User accounts, authentication, write access to data.
- A dedicated Earth-applications view (optional in the challenge). Covered instead by the AI-extracted "Applications on Earth" field in F2.
- Cloud deployment (stretch goal; local demo + video is enough).

## 5. Ranking criteria (initial weights, adjustable)

Lunar habitat target: O2 ≥ 0.30 mole fraction and P ≤ 10.2 psi (0.694 atm). Earth baseline: 0.21 at 1 atm.
Since 2026-11-14 the first criterion is "Atmosphere proximity" and its target follows the environment preset of F3; the formula below is the lunar preset.

| Criterion | Weight | Score in [0, 1] | Source |
|---|---|---|---|
| Lunar atmosphere proximity | 0.30 | Per test: mean of `clamp((O2 − 0.21) / 0.09)` and `clamp((1.0 − P_atm) / 0.306)`; missing component counts 0. Investigation = max over tests. No conditions → 0, reason "not documented". | CSV |
| Habitat relevance | 0.20 | Flight + sub-area "Spacecraft Fire Safety" = 1.0; other flight = 0.6; ground modeling = 0.3 | JSON |
| Safety pillar | 0.15 | Maps to prevention / detection / suppression = 1.0; fundamental flame physics = 0.3 | Curated table from challenge resources |
| Material type | 0.20 | Solids (fabrics, plastics) = 1.0; liquid droplets = 0.5; gases = 0.4; dust/aerosol = 0.3 | CSV / JSON |
| Structured data | 0.15 | CSV with normalized test conditions = 1.0; CSV without conditions = 0.5; none = 0 | Ingestion result |

Final score = Σ(weight × score) / Σ(weights). Pillar and material maps live in code as versioned tables and are shown in the UI ("how is this scored?").

## 6. Ingestion

Runs as `./gradlew bootRun --args='--spring.profiles.active=ingest'` (or a dedicated command); idempotent; results cached in Postgres.

PSI endpoints (unofficial, no auth, may change):
- Search: `GET /geode-py/ws/repo/search?term=<q>&source=psi&type=investigation&from=0&size=50` (Elasticsearch-style `hits`). Used only to verify the ID list.
- Detail: `GET /geode-py/ws/repo/investigations/<PSI-ID>` (17 KB to 8 MB; size comes from file lists).
- Download: `GET /geode-py/ws/studies/<PSI-ID>/download?file=<name>&version=<v>&redirect=false` → body is a signed S3 URL as plain text. Request it right before use; never store or log it.

Rules:
- At least 2 s between PSI requests; one retry with backoff on timeout (seen in practice).
- Store `raw_json` **without** `added`, `removed`, `images` file lists (size) and without contact emails/phones (privacy). Keep contact names and roles.
- CSV decoding: try UTF-8 (BOM), fall back to cp1252.
- Normalization through one curated mapping per CSV file (`ExperimentalTableParser` per file schema; generic fallback keeps only `raw_row`).
- Parse ranges and lists into `min`/`max`; strip units and convert: percent → fraction, bar/mmHg → atm.
- Sanity bounds: O2 fraction in [0, 1], pressure in (0, 10] atm, flow in [0, 100] cm/s. Out-of-bounds → null + ingestion warning (never silently kept).
- Ingestion report: per investigation, rows read, rows normalized, warnings. Shown in the UI as "data quality".

## 7. Data model

- `investigation` (psi_id PK, acronym, title, doi, project_type, research_area, sub_area, flight_program, hardware, hardware_family, fuel_state, safety_pillar, objective, approach, hypothesis, research_impacts, start_date, end_date, publications jsonb, raw_json jsonb, ingested_at)
- `test_condition` (id, investigation_id, source_file, row_index, test_label, fuel_or_material, o2_min, o2_max, pressure_atm_min, pressure_atm_max, flow_cm_s_min, flow_cm_s_max, raw_row jsonb)
- `ai_extraction` (id, investigation_id, field, value jsonb, source_span, confidence, model, prompt_version, created_at). Implemented as one row per field whose `value` is the JSON array of facts `{label, detail, source_span}` (empty array = not documented); the `source_span` and `confidence` columns stay unused.
- `llm_cache` (hash PK, request jsonb, response jsonb, model, created_at)
- `ingestion_warning` (id, investigation_id, source_file, row_index, column, raw_value, reason)

## 8. HTTP API (backend, JSON)

| Method | Path | Feature |
|---|---|---|
| GET | `/api/investigations?type=&hardware=&fuelState=&pillar=` | F1 |
| GET | `/api/investigations/{psiId}` | F2 |
| GET | `/api/investigations/{psiId}/test-conditions` | F2 |
| POST | `/api/ranking` (body: weights) | F3 |
| GET | `/api/ranking/criteria` | F3 |
| POST | `/api/compare` (body: 2–3 psiIds) | F4 |
| POST | `/api/assistant/ask` (body: question) | F5 |
| GET | `/api/investigations/{psiId}/extraction` | F2 (AI summary) |
| GET | `/api/ranking/environments` | F3 (added 2026-11-14) |
| POST | `/api/compare/narrative` (body: 2–3 psiIds) | F4 (AI paragraph) |
| GET | `/api/test-conditions/export?format=&psiIds=&minO2Percent=&maxPressurePsi=` | Data export (added 2026-11-14) |
| GET | `/api/meta/health` | Operations |

Data-quality warnings are returned inside the investigation detail (F2) instead of a separate endpoint.

## 9. LLM usage

- Claude through the Anthropic Java SDK; model configurable via `ANTHROPIC_MODEL`.
- Two calls only: (a) offline extraction per investigation at ingestion time (structured JSON, validated against a schema, with `source_span`); (b) online answers for F4/F5.
- System rules: answer only from provided records; cite PSI ID + DOI; never invent numbers; "not documented" when absent; add partial-gravity notice for Moon/Mars/partial-g.
- Every response cached in `llm_cache`; demo runs fully from cache.

## 10. Demo scenarios (acceptance for F5)

1. "Which investigations tested oxygen levels close to a lunar habitat (≥30 % O2, low pressure)?"
2. "What materials were burned in SAFFIRE and how large were the samples?"
3. "Compare FLEX and FLEX-2: what changed between them?"
4. "Which experiments help design smoke detectors for spacecraft?"
5. "Will a cotton fabric burn the same way on the Moon as on the ISS?" (must show partial-gravity gap)

## 11. Time plan

- Sat 14 Nov: ingestion (JSON + CSV + curated mappings), DB, F1, F2, F3 end to end.
- Sun 15 Nov morning: F4, F5, F6, polish.
- Sun 15 Nov afternoon: README, video, submission form. Submit before 11:59 PM, target mid-afternoon.

## 12. Open items

- Submission (confirmed from the Space Apps guide): 30-second video or 7-slide demo in English (or with English subtitles), public link to the final project, English text fields, disclosure of SI use.
- Final-report PDFs: not needed; the PSI JSON text was enough for the F2 extraction.
- PSI-22 (E-FIELD): its experimental table could not be normalized; it is shown as a data-quality warning.
