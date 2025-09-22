CREATE TABLE trade_transactions (
    id BIGSERIAL PRIMARY KEY,
    trade_id VARCHAR(64) NOT NULL,
    account_id VARCHAR(64) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    trade_date DATE NOT NULL,
    source_file VARCHAR(256),
    ingested_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE ledger_transactions (
    id BIGSERIAL PRIMARY KEY,
    trade_id VARCHAR(64) NOT NULL,
    account_id VARCHAR(64) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    trade_date DATE NOT NULL,
    source_file VARCHAR(256),
    ingested_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE reconciliation_results (
    id BIGSERIAL PRIMARY KEY,
    trade_id VARCHAR(64) NOT NULL,
    account_id VARCHAR(64) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    trade_amount NUMERIC(19, 4),
    ledger_amount NUMERIC(19, 4),
    mismatch_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'UNRESOLVED',
    mismatch_reason TEXT,
    last_reconciled_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    acknowledged_by VARCHAR(128),
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_trade_transactions_trade_id ON trade_transactions (trade_id);
CREATE INDEX idx_ledger_transactions_trade_id ON ledger_transactions (trade_id);
CREATE INDEX idx_reconciliation_results_status ON reconciliation_results (status);
