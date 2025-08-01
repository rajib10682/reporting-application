\c reportdb;


CREATE TABLE IF NOT EXISTS fxrate_info (
    fx_id VARCHAR(50) NOT NULL,
    fx_name VARCHAR(100) NOT NULL,
    year INTEGER NOT NULL,
    currency VARCHAR(10) NOT NULL,
    m1_rate DECIMAL(15,6),
    m2_rate DECIMAL(15,6),
    m3_rate DECIMAL(15,6),
    m4_rate DECIMAL(15,6),
    m5_rate DECIMAL(15,6),
    m6_rate DECIMAL(15,6),
    m7_rate DECIMAL(15,6),
    m8_rate DECIMAL(15,6),
    m9_rate DECIMAL(15,6),
    m10_rate DECIMAL(15,6),
    m11_rate DECIMAL(15,6),
    m12_rate DECIMAL(15,6),
    PRIMARY KEY (fx_id, fx_name, year, currency)
);

CREATE TABLE IF NOT EXISTS scenario_info (
    scenario_id BIGSERIAL PRIMARY KEY,
    scenario_name VARCHAR(100) UNIQUE NOT NULL,
    start_date DATE,
    end_date DATE,
    fx_rate VARCHAR(50) -- Foreign key reference to fxrate_info
);

CREATE TABLE IF NOT EXISTS account_info (
    account_id VARCHAR(50) NOT NULL,
    account_parent_id VARCHAR(50) NOT NULL,
    period_id VARCHAR(20) NOT NULL,
    account_name VARCHAR(200),
    account_level INTEGER,
    PRIMARY KEY (account_id, account_parent_id, period_id)
);

CREATE TABLE IF NOT EXISTS segment_info (
    segment_id VARCHAR(50) NOT NULL,
    segment_parent_id VARCHAR(50) NOT NULL,
    period_id VARCHAR(20) NOT NULL,
    segment_name VARCHAR(200),
    segment_level INTEGER,
    PRIMARY KEY (segment_id, segment_parent_id, period_id)
);

CREATE TABLE IF NOT EXISTS geography_info (
    geo_id VARCHAR(50) NOT NULL,
    geo_parent_id VARCHAR(50) NOT NULL,
    period_id VARCHAR(20) NOT NULL,
    geo_name VARCHAR(200),
    geo_level INTEGER,
    PRIMARY KEY (geo_id, geo_parent_id, period_id)
);

CREATE TABLE IF NOT EXISTS goc_info (
    goc VARCHAR(50) NOT NULL,
    segment_id VARCHAR(50) NOT NULL,
    geo_id VARCHAR(50) NOT NULL,
    period_id VARCHAR(20) NOT NULL,
    goc_value DECIMAL(15,2),
    PRIMARY KEY (goc, segment_id, geo_id, period_id)
);

CREATE TABLE IF NOT EXISTS user_info (
    user_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_fxrate_currency_year ON fxrate_info(currency, year);
CREATE INDEX IF NOT EXISTS idx_scenario_name ON scenario_info(scenario_name);
CREATE INDEX IF NOT EXISTS idx_account_period ON account_info(period_id);
CREATE INDEX IF NOT EXISTS idx_segment_period ON segment_info(period_id);
CREATE INDEX IF NOT EXISTS idx_geography_period ON geography_info(period_id);
CREATE INDEX IF NOT EXISTS idx_goc_period ON goc_info(period_id);
