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
