-- V2__create_transfers_table.sql
-- Tabla de transferencias bancarias

CREATE TABLE transfers (
    id UUID PRIMARY KEY,
    idempotency_key UUID NOT NULL UNIQUE,
    source_account_id UUID NOT NULL,
    destination_account_id UUID NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    description VARCHAR(500),
    error_message VARCHAR(1000),
    requires_approval BOOLEAN NOT NULL DEFAULT FALSE,
    approved_by UUID,
    approved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    
    CONSTRAINT fk_source_account FOREIGN KEY (source_account_id) REFERENCES accounts(id),
    CONSTRAINT fk_destination_account FOREIGN KEY (destination_account_id) REFERENCES accounts(id),
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_different_accounts CHECK (source_account_id != destination_account_id),
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'COMPENSATED'))
);

CREATE UNIQUE INDEX idx_transfer_idempotency_key ON transfers(idempotency_key);
CREATE INDEX idx_transfer_status ON transfers(status);
CREATE INDEX idx_transfer_source_account ON transfers(source_account_id);
CREATE INDEX idx_transfer_destination_account ON transfers(destination_account_id);
CREATE INDEX idx_transfer_created_at ON transfers(created_at);
CREATE INDEX idx_transfer_requires_approval ON transfers(requires_approval, status);

COMMENT ON TABLE transfers IS 'Transferencias bancarias entre cuentas';
COMMENT ON COLUMN transfers.idempotency_key IS 'Clave única para garantizar idempotencia';
COMMENT ON COLUMN transfers.requires_approval IS 'Requiere aprobación manual para montos altos';
