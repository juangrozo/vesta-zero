-- Vesta Zero initial schema.

CREATE TABLE investigation (
    psi_id            VARCHAR(16) PRIMARY KEY,
    acronym           VARCHAR(64),
    title             TEXT        NOT NULL,
    doi               VARCHAR(64) NOT NULL,
    project_type      VARCHAR(64),
    research_area     VARCHAR(128),
    sub_area          VARCHAR(255),
    flight_program    VARCHAR(255),
    hardware          TEXT,
    hardware_family   VARCHAR(32),
    fuel_state        VARCHAR(32),
    safety_pillar     VARCHAR(32),
    objective         TEXT,
    approach          TEXT,
    hypothesis        TEXT,
    research_impacts  TEXT,
    start_date        DATE,
    end_date          DATE,
    publications      JSONB       NOT NULL DEFAULT '[]'::jsonb,
    raw_json          JSONB       NOT NULL,
    ingested_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE test_condition (
    id                BIGSERIAL PRIMARY KEY,
    investigation_id  VARCHAR(16) NOT NULL REFERENCES investigation (psi_id) ON DELETE CASCADE,
    source_file       TEXT        NOT NULL,
    row_index         INT         NOT NULL,
    test_label        VARCHAR(128),
    fuel_or_material  TEXT,
    o2_min            NUMERIC(6, 4),
    o2_max            NUMERIC(6, 4),
    pressure_atm_min  NUMERIC(8, 4),
    pressure_atm_max  NUMERIC(8, 4),
    flow_cm_s_min     NUMERIC(8, 3),
    flow_cm_s_max     NUMERIC(8, 3),
    raw_row           JSONB       NOT NULL,
    UNIQUE (investigation_id, source_file, row_index),
    CONSTRAINT o2_bounds       CHECK (o2_min IS NULL OR (o2_min >= 0 AND o2_max <= 1)),
    CONSTRAINT pressure_bounds CHECK (pressure_atm_min IS NULL OR (pressure_atm_min > 0 AND pressure_atm_max <= 10)),
    CONSTRAINT flow_bounds     CHECK (flow_cm_s_min IS NULL OR (flow_cm_s_min >= 0 AND flow_cm_s_max <= 100))
);

CREATE INDEX idx_test_condition_investigation ON test_condition (investigation_id);

CREATE TABLE ai_extraction (
    id                BIGSERIAL PRIMARY KEY,
    investigation_id  VARCHAR(16) NOT NULL REFERENCES investigation (psi_id) ON DELETE CASCADE,
    field             VARCHAR(64) NOT NULL,
    value             JSONB       NOT NULL,
    source_span       TEXT,
    confidence        NUMERIC(3, 2),
    model             VARCHAR(64) NOT NULL,
    prompt_version    VARCHAR(16) NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_ai_extraction_investigation ON ai_extraction (investigation_id, field);

CREATE TABLE llm_cache (
    hash        CHAR(64) PRIMARY KEY,
    request     JSONB       NOT NULL,
    response    JSONB       NOT NULL,
    model       VARCHAR(64) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ingestion_warning (
    id                BIGSERIAL PRIMARY KEY,
    investigation_id  VARCHAR(16) NOT NULL REFERENCES investigation (psi_id) ON DELETE CASCADE,
    source_file       TEXT,
    row_index         INT,
    column_name       TEXT,
    raw_value         TEXT,
    reason            TEXT        NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
