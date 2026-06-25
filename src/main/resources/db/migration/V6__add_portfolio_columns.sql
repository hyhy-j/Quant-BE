-- portfolio_recommendations: strategy_type nullable 처리
ALTER TABLE portfolio_recommendations
    ALTER COLUMN strategy_type DROP NOT NULL;

-- 기존 수치 컬럼 정밀도 확장 (소수점 4자리)
ALTER TABLE portfolio_recommendations
    ALTER COLUMN mdd TYPE NUMERIC(7, 4),
    ALTER COLUMN sharpe_ratio TYPE NUMERIC(7, 4);

-- 신규 컬럼 추가
ALTER TABLE portfolio_recommendations
    ADD COLUMN risk_type       VARCHAR(20) CHECK (risk_type IN ('AGGRESSIVE', 'NEUTRAL', 'STABLE')),
    ADD COLUMN expected_return NUMERIC(7, 4),
    ADD COLUMN backtest_curve  TEXT,
    ADD COLUMN monthly_returns TEXT,
    ADD COLUMN top_stocks      TEXT,
    ADD COLUMN report          TEXT;

-- portfolio_items: 종목명·수량 컬럼 추가
ALTER TABLE portfolio_items
    ADD COLUMN name     VARCHAR(200),
    ADD COLUMN quantity BIGINT;