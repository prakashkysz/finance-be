CREATE TABLE users (
    id                    BIGSERIAL PRIMARY KEY,
    email                 VARCHAR(255) NOT NULL,
    mobile_number         VARCHAR(10)  NOT NULL,
    password_hash         VARCHAR(100) NOT NULL,
    role                  VARCHAR(30)  NOT NULL,
    enabled               BOOLEAN      NOT NULL DEFAULT TRUE,
    failed_login_attempts INT          NOT NULL DEFAULT 0,
    locked_until          TIMESTAMPTZ,
    last_login_at         TIMESTAMPTZ,
    created_at            TIMESTAMPTZ  NOT NULL,
    updated_at            TIMESTAMPTZ  NOT NULL,
    version               BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_mobile_number UNIQUE (mobile_number)
);

CREATE TABLE refresh_tokens (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked    BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash)
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);

CREATE TABLE credit_score_reports (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    pan_hash   VARCHAR(64) NOT NULL,
    pan_masked VARCHAR(10) NOT NULL,
    score      INT         NOT NULL CHECK (score BETWEEN 300 AND 900),
    band       VARCHAR(20) NOT NULL,
    source     VARCHAR(30) NOT NULL,
    checked_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_credit_score_reports_user_checked ON credit_score_reports (user_id, checked_at DESC);
CREATE INDEX idx_credit_score_reports_pan_hash ON credit_score_reports (pan_hash);

CREATE TABLE loan_products (
    id                     BIGSERIAL PRIMARY KEY,
    code                   VARCHAR(30)    NOT NULL,
    name                   VARCHAR(100)   NOT NULL,
    description            VARCHAR(500),
    base_rate              NUMERIC(5, 2)  NOT NULL,
    min_amount             NUMERIC(15, 2) NOT NULL,
    max_amount             NUMERIC(15, 2) NOT NULL,
    min_tenure_months      INT            NOT NULL,
    max_tenure_months      INT            NOT NULL,
    min_credit_score       INT            NOT NULL,
    processing_fee_percent NUMERIC(5, 2)  NOT NULL,
    active                 BOOLEAN        NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_loan_products_code UNIQUE (code)
);

CREATE TABLE interest_rate_rules (
    id          BIGSERIAL PRIMARY KEY,
    factor      VARCHAR(20)    NOT NULL,
    min_value   NUMERIC(15, 2) NOT NULL,
    max_value   NUMERIC(15, 2),
    adjustment  NUMERIC(5, 2)  NOT NULL,
    description VARCHAR(255)   NOT NULL,
    active      BOOLEAN        NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_interest_rate_rules_factor ON interest_rate_rules (factor);
