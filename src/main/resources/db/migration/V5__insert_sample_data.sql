-- V5__insert_sample_data.sql
-- Datos de ejemplo para desarrollo y testing

-- Insertar cuentas de prueba
INSERT INTO accounts (id, account_number, balance, currency, status, version, owner_name, owner_email, daily_limit, daily_used, created_at, updated_at) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', '1234567890', 10000.0000, 'USD', 'ACTIVE', 0, 'Juan Pérez', 'juan.perez@email.com', 50000.0000, 0.0000, NOW(), NOW()),
    ('550e8400-e29b-41d4-a716-446655440002', '0987654321', 5000.0000, 'USD', 'ACTIVE', 0, 'María García', 'maria.garcia@email.com', 50000.0000, 0.0000, NOW(), NOW()),
    ('550e8400-e29b-41d4-a716-446655440003', '1111222233', 15000.0000, 'EUR', 'ACTIVE', 0, 'Carlos López', 'carlos.lopez@email.com', 50000.0000, 0.0000, NOW(), NOW()),
    ('550e8400-e29b-41d4-a716-446655440004', '4444555566', 2000.0000, 'USD', 'BLOCKED', 0, 'Ana Martínez', 'ana.martinez@email.com', 50000.0000, 0.0000, NOW(), NOW());

COMMENT ON TABLE accounts IS 'Cuentas de prueba: 
- 1234567890: Juan (USD, $10,000, ACTIVE)
- 0987654321: María (USD, $5,000, ACTIVE)
- 1111222233: Carlos (EUR, €15,000, ACTIVE)
- 4444555566: Ana (USD, $2,000, BLOCKED)';
