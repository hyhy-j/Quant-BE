-- Drop old virtual account tables (replaced by AI-side managed tables)
DROP TABLE IF EXISTS risk_violations;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS virtual_holdings;
DROP TABLE IF EXISTS virtual_accounts;

-- ==================== TRADE ====================

CREATE TABLE portfolios
(
    id              BIGSERIAL      PRIMARY KEY,
    user_id         BIGINT         NOT NULL,
    balance         NUMERIC(18, 4) NOT NULL,
    initial_balance NUMERIC(18, 4) NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_portfolios_user_id UNIQUE (user_id)
);

CREATE TABLE holdings
(
    id         BIGSERIAL      PRIMARY KEY,
    user_id    BIGINT         NOT NULL,
    stock_code VARCHAR(10)    NOT NULL,
    quantity   BIGINT         NOT NULL DEFAULT 0,
    avg_price  NUMERIC(18, 4) NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_holdings_user_stock UNIQUE (user_id, stock_code)
);

CREATE INDEX IF NOT EXISTS idx_holdings_user_id ON holdings (user_id);

CREATE TABLE orders
(
    id            BIGSERIAL      PRIMARY KEY,
    user_id       BIGINT         NOT NULL,
    stock_code    VARCHAR(10)    NOT NULL,
    side          VARCHAR(4)     NOT NULL,
    quantity      BIGINT         NOT NULL,
    price         NUMERIC(18, 4) NOT NULL,
    amount        NUMERIC(20, 4) NOT NULL,
    balance_after NUMERIC(18, 4) NOT NULL,
    status        VARCHAR(10)    NOT NULL DEFAULT 'COMPLETED',
    executed_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_orders_side CHECK (side IN ('BUY', 'SELL'))
);

CREATE INDEX IF NOT EXISTS idx_orders_user_executed ON orders (user_id, executed_at DESC);