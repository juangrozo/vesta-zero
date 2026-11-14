# Vesta Zero — Architecture

Status: **approved** (2026-11-14).

## 1. Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3, Spring WebFlux (functional `RouterFunction` + handlers, no controllers), R2DBC |
| Build | Gradle with **Groovy DSL** (`build.gradle`, `settings.gradle`), Gradle Wrapper (`./gradlew`), version catalog in `gradle/libs.versions.toml`, Java 21 toolchain |
| Database | PostgreSQL 16. Flyway migrations (JDBC, at startup only); runtime access through R2DBC |
| LLM | Claude via the official Anthropic Java SDK (`com.anthropic:anthropic-java`), called only from the backend. Key in `.env` as `ANTHROPIC_API_KEY`, never committed |
| Frontend | TypeScript, Next.js (App Router), TailwindCSS. UI language: English |
| Infra | Docker Compose (postgres + backend + frontend) |

No Python unless Java cannot parse a source. No extra services.

Fallback: if WebFlux/R2DBC slows delivery, switch to Spring MVC + JDBC. Only adapters change.

## 2. Backend: clean architecture (ports and adapters)

Single Gradle module, layered by package. Dependencies point inward only:
`entrypoint -> application -> domain` and `infrastructure -> application/domain`.
The `domain` package has no Spring or framework imports. An ArchUnit test enforces these rules.

```
com.ignis.vesta
├── domain/              # plain Java, no framework
│   ├── model/           # Investigation, TestCondition, AiExtraction, RankingResult
│   ├── vo/              # PsiId, Doi, OxygenPercent, Pressure, Provenance (DOCUMENTED | AI_EXTRACTED)
│   ├── ranking/         # RankingCriterion, RankingEngine, CriterionScore
│   └── port/            # InvestigationRepository, PsiGateway, LlmGateway, ExperimentalTableParser
├── application/         # one use case per class
│   ├── IngestInvestigationsUseCase
│   ├── ListInvestigationsUseCase
│   ├── GetInvestigationUseCase
│   ├── RankInvestigationsUseCase
│   ├── CompareInvestigationsUseCase
│   └── AskAssistantUseCase
├── infrastructure/      # adapters
│   ├── psi/             # WebClient client for the PSI API, throttled
│   ├── persistence/     # R2DBC repositories, jsonb columns
│   ├── llm/             # Anthropic adapter + DB-backed cache
│   ├── csv/             # experimental table parsers
│   └── config/          # @Configuration wiring of ports to adapters
└── entrypoint/
    ├── http/            # RouterFunction, handlers, DTOs, mappers
    └── cli/             # ingestion command (ApplicationRunner, profile "ingest")
```

## 3. Design patterns

| Pattern | Where | Why |
|---|---|---|
| Ports and Adapters | `domain/port` interfaces, `infrastructure` implementations | Swap PSI, DB or LLM without touching use cases |
| Strategy | One class per `RankingCriterion` (e.g. `LunarAtmosphereProximityCriterion`, `SafetyPillarCriterion`, `MaterialTypeCriterion`, `HabitatRelevanceCriterion`, `StructuredDataCriterion`) | Add criteria without editing the engine; weights adjustable from the UI |
| Weighted sum with breakdown | `RankingEngine` | Deterministic, explainable rank: every result carries per-criterion score and reason |
| Registry / Factory | `ExperimentalTableParserRegistry` selects a parser by CSV headers; generic fallback stores only `raw_row` | CSV columns differ per investigation |
| Decorator | `CachingLlmGateway` wraps `AnthropicLlmGateway`; `ThrottledPsiGateway` wraps the PSI client | Caching and rate limiting without changing the core adapters |
| Value Object | `OxygenPercent`, `Pressure`, `Doi`, `PsiId` | Validate on construction; no invalid numbers in the domain |
| DTO + Mapper | `entrypoint/http` | Domain objects never leave through HTTP as-is |

## 4. SOLID

- **S** — one use case per class; one handler per resource.
- **O** — a new ranking criterion or CSV parser is a new class; the engine and registry stay closed.
- **L** — every `RankingCriterion` returns a score in `[0, 1]` plus an explanation; the engine treats all of them the same.
- **I** — small ports (`LlmGateway` exposes `extract()` and `answer()` only).
- **D** — use cases depend on ports, never on WebClient, R2DBC or the Anthropic SDK. Wiring lives in `infrastructure/config`.

## 5. Frontend structure

```
frontend/src/
├── app/                 # routes: /, /investigations/[id], /ranking, /compare, /assistant
├── features/            # catalog, ranking, compare, assistant
│   └── <feature>/{components, hooks, api.ts, types.ts}
└── shared/
    ├── ui/              # ProvenanceBadge, DoiLink, PartialGravityBanner, ...
    └── lib/http/        # single HTTP client to the backend
```

- Presentational components hold no data-fetching logic; hooks call `api.ts`.
- The frontend never sees the Anthropic key; all LLM calls go through the backend.

## 6. Repository layout

```
vesta-zero/
├── backend/             # Gradle project (Groovy DSL)
├── frontend/            # Next.js app
├── docs/                # architecture.md
├── docker-compose.yml
├── .env.example         # ANTHROPIC_API_KEY=, DB settings (no real values)
└── README.md
```
