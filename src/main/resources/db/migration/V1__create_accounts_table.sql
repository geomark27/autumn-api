-- V1__create_accounts_table.sql
-- Tabla de cuentas bancarias

CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    balance DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL DEFAULT 0,
    owner_name VARCHAR(255) NOT NULL,
    owner_email VARCHAR(100),
    daily_limit DECIMAL(19, 4) NOT NULL DEFAULT 50000.0000,
    daily_used DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    
    CONSTRAINT chk_balance_positive CHECK (balance >= 0),
    CONSTRAINT chk_daily_used_positive CHECK (daily_used >= 0),
    CONSTRAINT chk_currency CHECK (currency IN ('USD', 'EUR', 'MXN', 'PEN')),
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'BLOCKED', 'CLOSED'))
);

CREATE INDEX idx_account_number ON accounts(account_number);
CREATE INDEX idx_account_status ON accounts(status);
CREATE INDEX idx_account_owner_email ON accounts(owner_email);

COMMENT ON TABLE accounts IS 'Cuentas bancarias del sistema';
COMMENT ON COLUMN accounts.version IS 'Control de concurrencia optimista';
COMMENT ON COLUMN accounts.daily_limit IS 'Límite diario de transferencias salientes';
COMMENT ON COLUMN accounts.daily_used IS 'Acumulado de transferencias realizadas hoy';
