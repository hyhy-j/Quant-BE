CREATE TABLE IF NOT EXISTS portfolio_snapshots
(
    id           BIGSERIAL      PRIMARY KEY,
    user_id      BIGINT         NOT NULL,
    date         DATE           NOT NULL,
    total_assets NUMERIC(18, 4) NOT NULL,
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_portfolio_snapshots_user_date UNIQUE (user_id, date)
);

CREATE INDEX idx_portfolio_snapshots_user_date ON portfolio_snapshots (user_id, date DESC);