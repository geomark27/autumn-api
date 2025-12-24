-- V4__create_audit_events_table.sql
-- Tabla de eventos de auditoría con hash chaining

CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    event_hash VARCHAR(64) NOT NULL,
    previous_hash VARCHAR(64),
    user_id UUID,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL,
    
    CONSTRAINT chk_event_type CHECK (event_type IN (
        'TRANSFER_CREATED', 
        'TRANSFER_VALIDATED', 
        'TRANSFER_PROCESSING', 
        'TRANSFER_COMPLETED', 
        'TRANSFER_FAILED', 
        'TRANSFER_COMPENSATED',
        'ACCOUNT_DEBITED',
        'ACCOUNT_CREDITED',
        'IDEMPOTENCY_CHECK'
    ))
);

CREATE INDEX idx_audit_aggregate_id ON audit_events(aggregate_id);
CREATE INDEX idx_audit_event_type ON audit_events(event_type);
CREATE INDEX idx_audit_created_at ON audit_events(created_at);
CREATE INDEX idx_audit_user_id ON audit_events(user_id);

COMMENT ON TABLE audit_events IS 'Eventos de auditoría inmutables con hash chaining para Event Sourcing';
COMMENT ON COLUMN audit_events.event_hash IS 'Hash SHA-256 del evento actual';
COMMENT ON COLUMN audit_events.previous_hash IS 'Hash del evento anterior en la cadena';
COMMENT ON COLUMN audit_events.payload IS 'Datos del evento en formato JSON';
