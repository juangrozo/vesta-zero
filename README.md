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
cp .env.example .env        # then set ANTHROPIC_API_KEY (optional: without it the AI features are disabled)
docker compose up --build
```

- Frontend: http://localhost:3000
- Backend health: http://localhost:8080/api/meta/health

The first start loads `db/seed/01-vesta-seed.sql.gz` into the empty database: the 24 investigations, their
experimental tables, the AI extractions and a few cached assistant answers. The dashboard works right away,
with no calls to PSI and no API spend. The seed is loaded only when the `vesta-db` volume is empty;
`docker compose down -v` deletes the volume and starts over from the seed.

Refresh the data from NASA PSI and rerun the AI extraction inside Docker (optional):

```bash
docker compose run --rm backend --spring.profiles.active=ingest    # ~3 min, one PSI request every 2 s
docker compose run --rm backend --spring.profiles.active=extract   # one Claude call per new investigation
```

Regenerate the seed after refreshing:

```bash
docker compose exec -T db pg_dump -U vesta -d vesta --no-owner --no-privileges | gzip -9 > db/seed/01-vesta-seed.sql.gz
```

Development mode (database in Docker, backend and frontend on the host):

```bash
docker compose up -d db
cd backend && ./gradlew bootRun
cd frontend && npm install && npm run dev
```

Load data from NASA PSI without Docker (24 investigations, ~3 minutes, throttled to one request every 2 s):

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

### Data export

The **Data** page filters documented test conditions across investigations (for example the lunar habitat
range, at least 30 % O2 at 10.2 psi or less), previews them and downloads them as CSV or JSON. Each row has
the PSI ID and DOI to cite, O2, pressure and flow in common units, and the original table cells. The same
export is available from the API:

```bash
curl -o lunar.csv "http://localhost:8080/api/test-conditions/export?format=csv&minO2Percent=30&maxPressurePsi=10.2"
```

### AI summary on the detail page

Each investigation page shows fuels and samples, sensors and measurements, what changed and what stayed the
same between tests, and applications on Earth, extracted by Claude from the PSI text. The extraction runs offline once, after ingestion (one call per
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

## Use of SI

Vesta Zero uses Super Intelligence (SI) tools in the following places, as the NASA Space Apps submission
guide requires.

**Development process**

Vesta Zero was built with Spec-Driven Development. The team chose the challenge, defined the idea and wrote
the specification: scope, acceptance criteria, architecture (hexagonal, SOLID), data rules and limits (see
[docs/spec.md](docs/spec.md) and [docs/architecture.md](docs/architecture.md)). Code was then written with
the help of Claude Code (Anthropic) against that specification, in small increments; the team reviewed,
tested and approved every change before it was committed.

**Inside the app (Claude by Anthropic, model `claude-opus-5-5`, called from the backend only)**

- **Research assistant** (`/assistant`) and **AI comparison** (`/compare`): Claude answers from the stored PSI
  records only and cites each investigation. Citations and numbers are checked against the records, and
  answers are labeled *AI-extracted* in the interface.
- **AI summary on the detail page**: an offline step (`extract` profile) asks Claude for fuels and samples,
  measurements, what changed and what stayed the same between tests, and applications on Earth. Every item
  keeps the verbatim PSI passage it comes from; items whose passage is not found in the record are discarded.
  These values carry the *AI-extracted* badge, separate from *Documented* PSI values. The model and prompt
  version are stored with every extraction (`ai_extraction` table).

The ranking, the data export and the documented values are deterministic and do not use SI.

**Visual identity**

- The Vesta Zero logo and the navigation icons were designed with SI (Claude, Anthropic). The logo SVG files
  state this in their metadata (`<title>SI-generated logo</title>` and `<desc>`). They use no NASA branding.

## License

Code: to be defined. Data: CC0-1.0 (NASA PSI).
