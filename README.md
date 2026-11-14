# Vesta Zero

AI-assisted dashboard that summarizes, ranks and interprets NASA Physical Sciences Informatics (PSI)
microgravity combustion investigations to deliver fire safety insights for crewed exploration
(spacecraft, stations, lunar and Martian bases, pressurized rovers).

Built by team **Ignis Zero** for the NASA Space Apps Challenge 2026 — challenge *Flame in Freefall:
AI-Powered Fire Safety Insights from Microgravity Combustion Data*.

> Status: work in progress (project skeleton).

## Stack

- Backend: Java 21, Spring Boot 3.5, Spring WebFlux (functional routes), R2DBC, PostgreSQL 16, Flyway, Gradle (Groovy DSL)
- Frontend: Next.js, TypeScript, TailwindCSS
- LLM: Claude via the Anthropic Java SDK (backend only)
- Infra: Docker Compose

See [docs/architecture.md](docs/architecture.md).

## Run locally

Requirements: Docker with Compose. For development without Docker: JDK 21 and Node 22.

```bash
cp .env.example .env        # then set ANTHROPIC_API_KEY
docker compose up --build
```

- Frontend: http://localhost:3000
- Backend health: http://localhost:8080/api/meta/health

Development mode:

```bash
docker compose up -d db
cd backend && ./gradlew bootRun
cd frontend && npm install && npm run dev
```

Load data from NASA PSI (24 investigations, ~3 minutes, throttled to one request every 2 s):

```bash
cd backend && ./gradlew bootRun --args='--spring.profiles.active=ingest'
```

Ingestion is idempotent. Values that cannot be normalized (text such as `RAMP`, out-of-range numbers) are
never guessed: they are stored in `ingestion_warning` and the original row is kept in `test_condition.raw_row`.

### AI assistant

The assistant (`/assistant`) calls Claude (`claude-opus-5-5`) from the backend only. Set `ANTHROPIC_API_KEY` in `.env`;
without it the rest of the dashboard works and the assistant reports that it is not configured.

- The 24 investigation records fit in one prompt, so Claude sees all of them; the record block is prompt-cached for 1 hour.
- Answers are structured JSON. Citations are checked against the catalog, and every number in an answer is checked
  against the source records; anything that does not match is shown as a warning.
- Answers are stored in the `llm_cache` table, so repeated questions (for example during a demo) make no API call.

### AI summary on the detail page

Each investigation page shows fuels and samples, sensors and measurements, and variables between tests,
extracted by Claude from the PSI text. The extraction runs offline once, after ingestion (one call per
investigation):

```bash
cd backend && ./gradlew bootRun --args='--spring.profiles.active=extract'
# options: --only=PSI-69,PSI-25 to limit the run, --force to redo stored extractions
```

- Every item carries the verbatim PSI passage it comes from. An item is kept only if that passage is found in
  the record and every number in the item appears in the passage; everything else is discarded and logged.
- Results are stored in `ai_extraction`; reruns skip investigations already extracted with the same model and
  prompt version, so the page never calls the model.

Tests (includes architecture rules enforced with ArchUnit):

```bash
cd backend && ./gradlew test
```

## Data sources

All data comes from the NASA PSI repository (https://psi.nasa.gov), license CC0-1.0. Each investigation
is cited by its DOI in the dashboard. Only metadata and experimental-table CSVs are used; no raw data files
are downloaded.

## License

Code: to be defined. Data: CC0-1.0 (NASA PSI).
