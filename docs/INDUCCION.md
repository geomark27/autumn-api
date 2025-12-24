# Sistema de Transferencias Bancarias (Robust Banking System)

Documentación técnica del sistema de transferencias, diseñado para garantizar consistencia financiera, idempotencia y trazabilidad completa.

---

## 1. Lógica de Negocio

### Flujo Principal de Transferencia
1. **Solicitud:** El usuario envía `cuenta_origen`, `cuenta_destino` y `monto`.
2. **Idempotencia:** Se valida el UUID único para evitar duplicidad de transacciones.
3. **Validaciones:**
   - Saldo suficiente.
   - Cuentas activas y válidas.
   - Límites diarios/mensuales.
4. **Ejecución (Transaccional):**
   - **Débito:** Resta el monto de la cuenta origen.
   - **Crédito:** Suma el monto a la cuenta destino.
   - **Libro Mayor:** Registro de movimientos inmutables (Double-entry bookkeeping).

### Estados de la Transferencia
| Estado | Descripción |
| :--- | :--- |
| `PENDING` | Solicitud recibida. |
| `PROCESSING` | Validaciones aprobadas, ejecución en curso. |
| `COMPLETED` | Éxito total. |
| `FAILED` | Error detectado (Negocio o Técnico). |
| `COMPENSATED` | Revertida automáticamente por fallo en el flujo. |

### Reglas Críticas
- **Mínimo:** $1.00 USD.
- **Aprobación Extra:** Requerida para montos > $10,000.
- **Divisas:** Solo permitidas entre cuentas de la misma moneda.
- **Saldos:** No se permiten saldos negativos en ningún escenario.

---

## 2. Arquitectura y Componentes Técnicos

### Esquema de Componentes
1. **Controller:** Maneja JWT, Rate Limiting y validación de esquemas.
2. **IdempotencyService:** Cache en **Redis** (TTL 24h) para evitar el doble procesamiento.
3. **SagaOrchestrator:** Gestiona la secuencia de pasos y las acciones de compensación.
4. **AuditService:** Implementa **Event Sourcing** con Hash Chaining para integridad.

### Modelo de Datos
#### `accounts`
- `id` (UUID), `account_number`, `balance`, `currency`, `version` (Optimistic Lock).

#### `ledger_entries` (Inmutable)
- `id`, `transfer_id`, `type` (DEBIT/CREDIT), `amount`, `balance_after`.

#### `audit_events`
- `aggregate_id`, `event_type`, `payload` (JSON), `hash` (SHA-256 encadenado).

---

## 3. Estrategias de Robustez

### Gestión de Concurrencia
Se utiliza **Pessimistic Locking** (`SELECT FOR UPDATE`) para bloquear las filas de las cuentas durante la transacción activa, evitando que dos hilos debiten del mismo saldo simultáneamente.

### Patrón Saga (Compensación)
En caso de que el débito sea exitoso pero el crédito falle por un error de red o de sistema:
1. El orquestador detecta el fallo.
2. Ejecuta un **evento de compensación**.
3. El dinero se devuelve a la cuenta origen.
4. El estado final se marca como `COMPENSATED`.

### Integridad de Auditoría
Cada evento de auditoría genera un hash único que incluye el hash del evento anterior. Esto crea una **cadena de confianza** que permite detectar cualquier manipulación manual en la base de datos.

---

## 4. Casos de Uso Críticos

### Escenario: Request Duplicado
- El cliente reintenta un POST por mala conexión.
- El sistema detecta el `idempotency_key` en Redis.
- Retorna el resultado de la transferencia original sin volver a mover dinero.

### Escenario: Conciliación Diaria
- Proceso programado a la 01:00 AM.
- Suma total de débitos vs créditos.
- Si hay discrepancia, se dispara una alerta de alta prioridad a Compliance.

---

## 5. Endpoints Principales

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/v1/transfers` | Crea transferencia (Requiere UUID idempotente). |
| `GET` | `/api/v1/transfers/{id}` | Consulta estado y trazabilidad. |
| `GET` | `/api/v1/accounts/{id}/balance` | Consulta saldo actual. |
| `GET` | `/api/v1/audit/{transferId}` | Historial de eventos (Hash Chaining). |

---

## 6. Estrategia de Testing
- **Unitarios:** Lógica de saldos y validaciones de límites.
- **Integración:** Postgres + Redis mediante Testcontainers.
- **Carga/Concurrencia:** Simulación de múltiples hilos intentando debitar de la misma cuenta para validar bloqueos.