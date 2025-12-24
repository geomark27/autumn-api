-- V3__create_ledger_entries_table.sql
-- Tabla del libro mayor (inmutable)

CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY,
    transfer_id UUID NOT NULL,
    account_id UUID NOT NULL,
    type VARCHAR(10) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    balance_after DECIMAL(19, 4) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_ledger_transfer FOREIGN KEY (transfer_id) REFERENCES transfers(id),
    CONSTRAINT fk_ledger_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT chk_type CHECK (type IN ('DEBIT', 'CREDIT')),
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_balance_after_positive CHECK (balance_after >= 0)
);

CREATE INDEX idx_ledger_transfer_id ON ledger_entries(transfer_id);
CREATE INDEX idx_ledger_account_id ON ledger_entries(account_id);
CREATE INDEX idx_ledger_created_at ON ledger_entries(created_at);

COMMENT ON TABLE ledger_entries IS 'Libro mayor - Registro inmutable de movimientos (Double-entry bookkeeping)';
COMMENT ON COLUMN ledger_entries.balance_after IS 'Saldo de la cuenta DESPUÉS de aplicar este movimiento';
