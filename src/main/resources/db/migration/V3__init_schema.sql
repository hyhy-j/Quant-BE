-- ==================== AUTH ====================

CREATE TABLE investment_profiles
(
    id                BIGSERIAL     PRIMARY KEY,
    user_id           BIGINT        NOT NULL REFERENCES users (id),
    investment_goal   VARCHAR(255)  NOT NULL,
    risk_tolerance    SMALLINT      NOT NULL CHECK (risk_tolerance BETWEEN 1 AND 5),
    investment_period VARCHAR(50)   NOT NULL,
    investable_amount DECIMAL(18,0) NOT NULL,
    profile_type      VARCHAR(20)   NOT NULL CHECK (profile_type IN ('AGGRESSIVE', 'NEUTRAL', 'STABLE')),
    is_current        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP,
    deleted_at        TIMESTAMP
);

CREATE INDEX idx_investment_profiles_user_id ON investment_profiles (user_id);

-- ==================== STOCK / MARKET ====================

CREATE TABLE stocks
(
    code       VARCHAR(20)  PRIMARY KEY,
    name       VARCHAR(200) NOT NULL,
    market     VARCHAR(20)  NOT NULL,
    sector     VARCHAR(100),
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE stock_prices
(
    id         BIGSERIAL      PRIMARY KEY,
    stock_code VARCHAR(20)    NOT NULL,
    open       NUMERIC(18, 4) NOT NULL,
    high       NUMERIC(18, 4) NOT NULL,
    low        NUMERIC(18, 4) NOT NULL,
    close      NUMERIC(18, 4) NOT NULL,
    volume     BIGINT         NOT NULL,
    date       DATE           NOT NULL,
    created_at TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT uq_stock_prices_code_date UNIQUE (stock_code, date)
);

CREATE INDEX idx_stock_prices_code_date ON stock_prices (stock_code, date DESC);

CREATE TABLE technical_indicators
(
    id             BIGSERIAL      PRIMARY KEY,
    stock_code     VARCHAR(20)    NOT NULL,
    date           DATE           NOT NULL,
    ma5            NUMERIC(18, 4),
    ma20           NUMERIC(18, 4),
    ma60           NUMERIC(18, 4),
    rsi            NUMERIC(5, 2),
    macd           NUMERIC(18, 4),
    macd_signal    NUMERIC(18, 4),
    macd_histogram NUMERIC(18, 4),
    bb_upper       NUMERIC(18, 4),
    bb_middle      NUMERIC(18, 4),
    bb_lower       NUMERIC(18, 4),
    volume_anomaly BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP,
    deleted_at     TIMESTAMP,
    CONSTRAINT uq_technical_indicators_code_date UNIQUE (stock_code, date)
);

-- ==================== NEWS ====================

CREATE TABLE news_articles
(
    id           BIGSERIAL     PRIMARY KEY,
    stock_code   VARCHAR(20)   NOT NULL,
    title        VARCHAR(500)  NOT NULL,
    content      TEXT,
    source       VARCHAR(100),
    published_at VARCHAR(100),
    created_at   TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP,
    deleted_at   TIMESTAMP
);

CREATE INDEX idx_news_articles_stock_code ON news_articles (stock_code);

CREATE TABLE news_sentiments
(
    id              BIGSERIAL    PRIMARY KEY,
    news_article_id BIGINT       REFERENCES news_articles (id),
    stock_code      VARCHAR(20)  NOT NULL,
    score           DECIMAL(3,1) NOT NULL CHECK (score BETWEEN -1.0 AND 1.0),
    reason          TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    deleted_at      TIMESTAMP
);

CREATE INDEX idx_news_sentiments_stock_code ON news_sentiments (stock_code);

-- ==================== MARKET REPORT ====================

CREATE TABLE market_reports
(
    id           BIGSERIAL    PRIMARY KEY,
    report_type  VARCHAR(20)  NOT NULL CHECK (report_type IN ('MORNING', 'EVENING')),
    title        VARCHAR(300) NOT NULL,
    summary      TEXT,
    content      TEXT,
    generated_at TIMESTAMP    NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP,
    deleted_at   TIMESTAMP
);

CREATE INDEX idx_market_reports_generated_at ON market_reports (generated_at DESC);

CREATE TABLE user_report_reads
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT    NOT NULL REFERENCES users (id),
    report_id   BIGINT    NOT NULL REFERENCES market_reports (id),
    is_read     BOOLEAN   NOT NULL DEFAULT FALSE,
    notified_at TIMESTAMP,
    read_at     TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    deleted_at  TIMESTAMP,
    CONSTRAINT uq_user_report_reads UNIQUE (user_id, report_id)
);

-- ==================== PORTFOLIO ====================

CREATE TABLE portfolio_recommendations
(
    id                  BIGSERIAL     PRIMARY KEY,
    user_id             BIGINT        NOT NULL REFERENCES users (id),
    profile_id          BIGINT        REFERENCES investment_profiles (id),
    strategy_type       VARCHAR(20)   NOT NULL CHECK (strategy_type IN ('MOMENTUM', 'BALANCED', 'VALUE')),
    momentum_period     VARCHAR(5),
    total_amount        DECIMAL(18,0) NOT NULL,
    expected_return_min NUMERIC(5, 2),
    expected_return_max NUMERIC(5, 2),
    mdd                 NUMERIC(5, 2),
    sharpe_ratio        NUMERIC(5, 2),
    backtest_1y_return  NUMERIC(5, 2),
    status              VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    created_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,
    deleted_at          TIMESTAMP
);

CREATE INDEX idx_portfolio_recommendations_user_id ON portfolio_recommendations (user_id);

CREATE TABLE portfolio_items
(
    id                BIGSERIAL    PRIMARY KEY,
    recommendation_id BIGINT       NOT NULL REFERENCES portfolio_recommendations (id),
    stock_code        VARCHAR(20)  NOT NULL,
    weight            NUMERIC(5,2) NOT NULL,
    amount            DECIMAL(18,0),
    momentum_return   NUMERIC(5,2),
    reason            TEXT,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP,
    deleted_at        TIMESTAMP
);

CREATE INDEX idx_portfolio_items_recommendation_id ON portfolio_items (recommendation_id);

-- ==================== PAPER TRADING ====================

CREATE TABLE virtual_accounts
(
    id                 BIGSERIAL     PRIMARY KEY,
    user_id            BIGINT        NOT NULL UNIQUE REFERENCES users (id),
    balance            DECIMAL(18,0) NOT NULL,
    initial_balance    DECIMAL(18,0) NOT NULL,
    total_realized_pnl DECIMAL(18,0) NOT NULL DEFAULT 0,
    is_trading_halted  BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP,
    deleted_at         TIMESTAMP
);

CREATE TABLE virtual_holdings
(
    id         BIGSERIAL      PRIMARY KEY,
    account_id BIGINT         NOT NULL REFERENCES virtual_accounts (id),
    stock_code VARCHAR(20)    NOT NULL,
    quantity   BIGINT         NOT NULL,
    avg_price  NUMERIC(18, 4) NOT NULL,
    created_at TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT uq_virtual_holdings_account_stock UNIQUE (account_id, stock_code)
);

CREATE TABLE orders
(
    id             BIGSERIAL      PRIMARY KEY,
    account_id     BIGINT         NOT NULL REFERENCES virtual_accounts (id),
    stock_code     VARCHAR(20)    NOT NULL,
    order_type     VARCHAR(10)    NOT NULL CHECK (order_type IN ('MARKET', 'LIMIT')),
    side           VARCHAR(4)     NOT NULL CHECK (side IN ('BUY', 'SELL')),
    quantity       BIGINT         NOT NULL,
    price          NUMERIC(18, 4),
    executed_price NUMERIC(18, 4),
    executed_qty   BIGINT,
    status         VARCHAR(20)    NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'EXECUTED', 'CANCELLED', 'REJECTED')),
    reject_reason  TEXT,
    executed_at    TIMESTAMP,
    created_at     TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP,
    deleted_at     TIMESTAMP
);

CREATE INDEX idx_orders_account_id ON orders (account_id);
CREATE INDEX idx_orders_created_at ON orders (created_at DESC);

CREATE TABLE risk_violations
(
    id         BIGSERIAL   PRIMARY KEY,
    account_id BIGINT      NOT NULL REFERENCES virtual_accounts (id),
    order_id   BIGINT      REFERENCES orders (id),
    rule_type  VARCHAR(50) NOT NULL CHECK (rule_type IN ('CONCENTRATION_LIMIT', 'DAILY_TRADE_LIMIT', 'LOSS_HALT')),
    detail     TEXT,
    created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

-- ==================== NOTIFICATION ====================

CREATE TABLE notification_settings
(
    id                     BIGSERIAL    PRIMARY KEY,
    user_id                BIGINT       NOT NULL UNIQUE REFERENCES users (id),
    email_enabled          BOOLEAN      NOT NULL DEFAULT TRUE,
    web_push_enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    price_alert_enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    price_change_threshold NUMERIC(5,2)          DEFAULT 5.0,
    report_notify_enabled  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at             TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMP,
    deleted_at             TIMESTAMP
);

CREATE TABLE notifications
(
    id             BIGSERIAL    PRIMARY KEY,
    user_id        BIGINT       NOT NULL REFERENCES users (id),
    type           VARCHAR(50)  NOT NULL,
    title          VARCHAR(300) NOT NULL,
    message        TEXT,
    channel        VARCHAR(20)  NOT NULL,
    reference_id   BIGINT,
    reference_type VARCHAR(50),
    is_read        BOOLEAN      NOT NULL DEFAULT FALSE,
    sent_at        TIMESTAMP,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP,
    deleted_at     TIMESTAMP
);

CREATE INDEX idx_notifications_user_id_created_at ON notifications (user_id, created_at DESC);

-- ==================== AGENT LOG ====================

CREATE TABLE agent_activity_logs
(
    id          BIGSERIAL    PRIMARY KEY,
    agent_type  VARCHAR(20)  NOT NULL,
    action      VARCHAR(100) NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    detail      TEXT,
    started_at  TIMESTAMP,
    finished_at TIMESTAMP,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    deleted_at  TIMESTAMP
);

CREATE INDEX idx_agent_activity_logs_created_at ON agent_activity_logs (created_at DESC);