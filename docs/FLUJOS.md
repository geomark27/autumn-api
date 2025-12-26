# 🔄 Flujos de Lógica de Negocio - Autumn Banking System

**Diagramas de secuencia y comportamiento del sistema**

---

## 📑 Índice

1. [Flujo Principal: Crear Transferencia](#flujo-principal-crear-transferencia)
2. [Flujo: Consultar Cuenta](#flujo-consultar-cuenta)
3. [Flujo: Consultar Transferencia](#flujo-consultar-transferencia)
4. [Flujo: Transferencia con Aprobación Manual](#flujo-transferencia-con-aprobación-manual)
5. [Flujo: Manejo de Errores y Compensación](#flujo-manejo-de-errores-y-compensación)
6. [Flujo: Validaciones de Negocio](#flujo-validaciones-de-negocio)
7. [Máquina de Estados](#máquina-de-estados)
8. [Interacción entre Componentes](#interacción-entre-componentes)

---

## 🎯 Flujo Principal: Crear Transferencia

### Diagrama de Secuencia

```
Cliente              Controller          TransferService     IdempotencyService   AccountRepository   AuditService   LedgerRepository
  │                      │                      │                    │                    │                │               │
  │  POST /transfers    │                      │                    │                    │                │               │
  ├─────────────────────>│                      │                    │                    │                │               │
  │                      │                      │                    │                    │                │               │
  │                      │ @Valid               │                    │                    │                │               │
  │                      │ (Bean Validation)    │                    │                    │                │               │
  │                      │                      │                    │                    │                │               │
  │                      │ createTransfer()     │                    │                    │                │               │
  │                      ├─────────────────────>│                    │                    │                │               │
  │                      │                      │                    │                    │                │               │
  │                      │                      │ exists(key)?       │                    │                │               │
  │                      │                      ├───────────────────>│                    │                │               │
  │                      │                      │                    │                    │                │               │
  │                      │                      │ ◄─────────────────┤                    │                │               │
  │                      │                      │ false              │                    │                │               │
  │                      │                      │                    │                    │                │               │
  │                      │                      │ findByAccountNumberWithLock()           │                │               │
  │                      │                      ├────────────────────────────────────────>│                │               │
  │                      │                      │                    │                    │                │               │
  │                      │                      │ ◄───────────────────────────────────────┤                │               │
  │                      │                      │ sourceAccount (LOCKED)                  │                │               │
  │                      │                      │                    │                    │                │               │
  │                      │                      │ findByAccountNumberWithLock()           │                │               │
  │                      │                      ├────────────────────────────────────────>│                │               │
  │                      │                      │                    │                    │                │               │
  │                      │                      │ ◄───────────────────────────────────────┤                │               │
  │                      │                      │ destinationAccount (LOCKED)             │                │               │
  │                      │                      │                    │                    │                │               │
  │                      │                      │ validateTransfer() │                    │                │               │
  │                      │                      │ (interno)          │                    │                │               │
  │                      │                      │                    │                    │                │               │
  │                      │                      │ save(transfer)     │                    │                │               │
  │                      │                      ├────────────────────────────────────────>│                │               │
  │                      │                      │                    │                    │                │               │
  │                      │                      │                    │      createAuditEvent(CREATED)      │               │
  │                      │                      ├──────────────────────────────────────────────────────────>│               │
  │                      │                      │                    │                    │                │               │
  │                      │                      │ store(key, id)     │                    │                │               │
  │                      │                      ├───────────────────>│                    │                │               │
  │                      │                      │                    │                    │                │               │
  │                      │                      │ executeTransfer()  │                    │                │               │
  │                      │                      │ (si no requiere    │                    │                │               │
  │                      │                      │  aprobación)       │                    │                │               │
  │                      │                      │                    │                    │                │               │
  │                      │                      │ source.debit(amt)  │                    │                │               │
  │                      │                      │ dest.credit(amt)   │                    │                │               │
  │                      │                      │                    │                    │                │               │
  │                      │                      │ save(source)       │                    │                │               │
  │                      │                      ├────────────────────────────────────────>│                │               │
  │                      │                      │                    │                    │                │               │
  │                      │                      │ save(destination)  │                    │                │               │
  │                      │                      ├────────────────────────────────────────>│                │               │
  │                      │                      │                    │                    │                │               │
  │                      │                      │                    │                    │                │ save(debit)   │
  │                      │                      ├────────────────────────────────────────────────────────────────────────>│
  │                      │                      │                    │                    │                │               │
  │                      │                      │                    │                    │                │ save(credit)  │
  │                      │                      ├────────────────────────────────────────────────────────────────────────>│
  │                      │                      │                    │                    │                │               │
  │                      │                      │ save(transfer)     │                    │                │               │
  │                      │                      │ status=COMPLETED   │                    │                │               │
  │                      │                      ├────────────────────────────────────────>│                │               │
  │                      │                      │                    │                    │                │               │
  │                      │                      │                    │      createAuditEvent(COMPLETED)    │               │
  │                      │                      ├──────────────────────────────────────────────────────────>│               │
  │                      │                      │                    │                    │                │               │
  │                      │ ◄─────────────────────┤                    │                    │                │               │
  │                      │ TransferResponse    │                    │                    │                │               │
  │                      │                      │                    │                    │                │               │
  │ ◄─────────────────────┤                      │                    │                    │                │               │
  │ 201 CREATED          │                      │                    │                    │                │               │
  │ {transferResponse}   │                      │                    │                    │                │               │
```

---

### Paso a Paso Detallado

#### **1. Recepción del Request**

```java
POST /api/v1/transfers
Content-Type: application/json

{
  "idempotencyKey": "550e8400-e29b-41d4-a716-446655440001",
  "sourceAccountNumber": "1234567890",
  "destinationAccountNumber": "0987654321",
  "amount": 500.00,
  "description": "Pago de servicios"
}
```

**Controller:**
```java
@PostMapping
public ResponseEntity<TransferResponse> createTransfer(
    @Valid @RequestBody TransferRequest request) {
    
    // @Valid ejecuta validaciones automáticamente
    // Si falla: MethodArgumentNotValidException → 400 BAD_REQUEST
    
    TransferResponse response = transferService.createTransfer(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

---

#### **2. Validación de Idempotencia**

```java
// En TransferService
if (idempotencyService.exists(request.getIdempotencyKey())) {
    UUID existingId = idempotencyService.getTransferId(request.getIdempotencyKey());
    Transfer existing = transferRepository.findById(existingId)
        .orElseThrow(() -> new TransferNotFoundException(existingId));
    
    return transferMapper.toResponse(existing);
}
```

**Redis Check:**
```
Key: "idempotency:transfer:550e8400-e29b-41d4-a716-446655440001"
Value: null (no existe)

→ Proceder con la transferencia
```

---

#### **3. Obtener Cuentas CON Bloqueo Pesimista**

```java
@Transactional  // CRÍTICO: El lock solo funciona dentro de transacción
public TransferResponse createTransfer(TransferRequest request) {
    
    // Bloqueo pesimista: Nadie más puede modificar estas cuentas hasta que termine
    Account source = accountRepository
        .findByAccountNumberWithLock(request.getSourceAccountNumber())
        .orElseThrow(() -> new AccountNotFoundException(request.getSourceAccountNumber()));
    
    Account destination = accountRepository
        .findByAccountNumberWithLock(request.getDestinationAccountNumber())
        .orElseThrow(() -> new AccountNotFoundException(request.getDestinationAccountNumber()));
    
    // Si otro hilo intenta obtener estas cuentas, ESPERARÁ aquí
}
```

**Estado en Base de Datos:**
```sql
-- PostgreSQL adquiere bloqueos exclusivos en estas filas
SELECT * FROM accounts WHERE account_number = '1234567890' FOR UPDATE;
SELECT * FROM accounts WHERE account_number = '0987654321' FOR UPDATE;

-- Otros hilos: WAITING...
```

---

#### **4. Validaciones de Negocio**

```java
private void validateTransfer(Account source, Account destination, BigDecimal amount) {
    
    // 1. Verificar que cuenta origen esté activa
    if (source.getStatus() != AccountStatus.ACTIVE) {
        throw new InactiveAccountException(
            source.getAccountNumber(), 
            source.getStatus().name()
        );
    }
    
    // 2. Verificar que cuenta destino pueda recibir
    if (!destination.canReceive()) {
        throw new InactiveAccountException(
            destination.getAccountNumber(),
            destination.getStatus().name()
        );
    }
    
    // 3. Verificar misma moneda
    if (source.getCurrency() != destination.getCurrency()) {
        throw new CurrencyMismatchException(
            source.getCurrency().name(),
            destination.getCurrency().name()
        );
    }
    
    // 4. Verificar saldo suficiente
    if (source.getBalance().compareTo(amount) < 0) {
        throw new InsufficientBalanceException(
            source.getAccountNumber(),
            amount,
            source.getBalance()
        );
    }
    
    // 5. Verificar límite diario
    BigDecimal newDailyUsed = source.getDailyUsed().add(amount);
    if (newDailyUsed.compareTo(source.getDailyLimit()) > 0) {
        throw new DailyLimitExceededException(
            source.getAccountNumber(),
            source.getDailyLimit(),
            source.getDailyUsed(),
            amount
        );
    }
}
```

**Ejemplo de validación fallida:**
```
Request: Transfer $15,000 desde cuenta con saldo $10,000

→ InsufficientBalanceException
→ GlobalExceptionHandler
→ Response: 400 BAD_REQUEST
{
  "status": 400,
  "error": "Insufficient Balance",
  "message": "Saldo insuficiente en cuenta 1234567890. Requerido: 15000.00, Disponible: 10000.00"
}
```

---

#### **5. Crear Entidad Transfer (Estado PENDING)**

```java
// Determinar si requiere aprobación
BigDecimal approvalThreshold = new BigDecimal("10000.00");
boolean requiresApproval = amount.compareTo(approvalThreshold) > 0;

Transfer transfer = Transfer.builder()
    .idempotencyKey(request.getIdempotencyKey())
    .sourceAccount(source)
    .destinationAccount(destination)
    .amount(amount)
    .description(request.getDescription())
    .status(TransferStatus.PENDING)
    .requiresApproval(requiresApproval)
    .build();

transfer = transferRepository.save(transfer);
```

**Estado en DB:**
```sql
INSERT INTO transfers (
    id, 
    idempotency_key, 
    source_account_id, 
    destination_account_id,
    amount, 
    status,
    requires_approval,
    created_at,
    updated_at
) VALUES (
    '123e4567-e89b-12d3-a456-426614174000',
    '550e8400-e29b-41d4-a716-446655440001',
    'acc-source-id',
    'acc-dest-id',
    500.00,
    'PENDING',
    false,
    NOW(),
    NOW()
);
```

---

#### **6. Auditoría: Evento TRANSFER_CREATED**

```java
auditService.createAuditEvent(
    transfer.getId(),           // aggregateId
    "Transfer",                 // aggregateType
    AuditEventType.TRANSFER_CREATED,
    transfer,                   // payload (se serializa a JSON)
    null,                       // userId (por implementar)
    null                        // metadata
);
```

**Cálculo del Hash:**
```java
// Obtener hash del último evento
String previousHash = auditEventRepository.findLatestEvent()
    .map(AuditEvent::getEventHash)
    .orElse("0");  // "0" si es el primer evento del sistema

// Serializar payload
String payload = objectMapper.writeValueAsString(transfer);
// {"id":"123e4567...", "amount":500.00, "status":"PENDING", ...}

// Calcular hash
String data = previousHash + "|" + 
              transfer.getId() + "|" + 
              "TRANSFER_CREATED" + "|" + 
              payload + "|" + 
              LocalDateTime.now();

byte[] hashBytes = SHA-256(data);
String eventHash = HexFormat.of().formatHex(hashBytes);
// "a3f7c8d2e1b9..."

// Guardar evento
INSERT INTO audit_events (
    id, aggregate_id, event_type, payload,
    event_hash, previous_hash, created_at
) VALUES (...);
```

---

#### **7. Almacenar en Redis (Idempotencia)**

```java
boolean stored = idempotencyService.store(
    request.getIdempotencyKey(),
    transfer.getId()
);

if (!stored) {
    // Muy raro: Otro hilo almacenó la misma clave simultáneamente
    throw new DuplicateTransferException(request.getIdempotencyKey());
}
```

**Redis:**
```
SET idempotency:transfer:550e8400-e29b-41d4-a716-446655440001 
    "123e4567-e89b-12d3-a456-426614174000"
EX 86400  // TTL: 24 horas
```

---

#### **8. Ejecutar Transferencia (Si NO requiere aprobación)**

```java
if (!transfer.getRequiresApproval()) {
    executeTransfer(transfer);
}
// Si requiere aprobación, queda en estado PENDING para aprobación manual
```

---

#### **9. Ejecución de la Transferencia**

```java
private void executeTransfer(Transfer transfer) {
    
    // A. Cambiar estado a PROCESSING
    transfer.startProcessing();
    transferRepository.save(transfer);
    
    auditService.createAuditEvent(
        transfer.getId(),
        "Transfer",
        AuditEventType.TRANSFER_PROCESSING,
        transfer
    );
    
    try {
        Account source = transfer.getSourceAccount();
        Account destination = transfer.getDestinationAccount();
        BigDecimal amount = transfer.getAmount();
        
        // B. Debitar cuenta origen
        source.debit(amount);
        accountRepository.save(source);
        
        auditService.createAuditEvent(
            transfer.getId(),
            "Transfer",
            AuditEventType.ACCOUNT_DEBITED,
            Map.of(
                "accountNumber", source.getAccountNumber(),
                "amount", amount,
                "balanceAfter", source.getBalance()
            )
        );
        
        // C. Creditar cuenta destino
        destination.credit(amount);
        accountRepository.save(destination);
        
        auditService.createAuditEvent(
            transfer.getId(),
            "Transfer",
            AuditEventType.ACCOUNT_CREDITED,
            Map.of(
                "accountNumber", destination.getAccountNumber(),
                "amount", amount,
                "balanceAfter", destination.getBalance()
            )
        );
        
        // D. Crear asientos en libro mayor (Double-Entry)
        LedgerEntry debit = LedgerEntry.createDebit(
            transfer,
            source,
            amount,
            "Transferencia enviada a " + destination.getAccountNumber()
        );
        ledgerEntryRepository.save(debit);
        
        LedgerEntry credit = LedgerEntry.createCredit(
            transfer,
            destination,
            amount,
            "Transferencia recibida de " + source.getAccountNumber()
        );
        ledgerEntryRepository.save(credit);
        
        // E. Marcar como completada
        transfer.markAsCompleted();
        transferRepository.save(transfer);
        
        auditService.createAuditEvent(
            transfer.getId(),
            "Transfer",
            AuditEventType.TRANSFER_COMPLETED,
            transfer
        );
        
    } catch (Exception e) {
        // Manejo de errores (ver flujo de compensación)
        handleTransferFailure(transfer, e);
        throw e;
    }
}
```

**Estado Final en DB:**

```sql
-- Cuentas actualizadas
UPDATE accounts SET balance = 9500.00, daily_used = 500.00 WHERE account_number = '1234567890';
UPDATE accounts SET balance = 5500.00 WHERE account_number = '0987654321';

-- Transfer completada
UPDATE transfers SET status = 'COMPLETED', completed_at = NOW() WHERE id = '...';

-- Libro mayor (2 asientos)
INSERT INTO ledger_entries (transfer_id, account_id, type, amount, balance_after) VALUES
  ('...', 'acc-source', 'DEBIT', 500.00, 9500.00),
  ('...', 'acc-dest', 'CREDIT', 500.00, 5500.00);

-- Eventos de auditoría (5 eventos en total)
TRANSFER_CREATED
TRANSFER_PROCESSING
ACCOUNT_DEBITED
ACCOUNT_CREDITED
TRANSFER_COMPLETED
```

---

#### **10. Retornar Respuesta**

```java
return transferMapper.toResponse(transfer);
```

**Response:**
```json
HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "idempotencyKey": "550e8400-e29b-41d4-a716-446655440001",
  "sourceAccountNumber": "1234567890",
  "destinationAccountNumber": "0987654321",
  "amount": 500.00,
  "status": "COMPLETED",
  "description": "Pago de servicios",
  "errorMessage": null,
  "requiresApproval": false,
  "createdAt": "2025-12-26T10:30:00",
  "completedAt": "2025-12-26T10:30:01"
}
```

---

## 📊 Flujo: Consultar Cuenta

```
Cliente          Controller        AccountService      AccountRepository      AccountMapper
  │                  │                    │                     │                    │
  │ GET /accounts   │                    │                     │                    │
  │    /number/123  │                    │                     │                    │
  ├────────────────>│                    │                     │                    │
  │                  │                    │                     │                    │
  │                  │ getAccountByNumber()                    │                    │
  │                  ├───────────────────>│                     │                    │
  │                  │                    │                     │                    │
  │                  │                    │ findByAccountNumber()                   │
  │                  │                    ├────────────────────>│                    │
  │                  │                    │                     │                    │
  │                  │                    │ ◄───────────────────┤                    │
  │                  │                    │ Optional<Account>   │                    │
  │                  │                    │                     │                    │
  │                  │                    │ orElseThrow(        │                    │
  │                  │                    │   AccountNotFound)  │                    │
  │                  │                    │                     │                    │
  │                  │                    │ toResponse(account) │                    │
  │                  │                    ├──────────────────────────────────────────>│
  │                  │                    │                     │                    │
  │                  │                    │ ◄─────────────────────────────────────────┤
  │                  │ ◄───────────────────┤ AccountResponse     │                    │
  │                  │ AccountResponse    │                     │                    │
  │                  │                    │                     │                    │
  │ ◄────────────────┤                    │                     │                    │
  │ 200 OK           │                    │                     │                    │
```

### Implementación

```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    
    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        
        return accountMapper.toResponse(account);
    }
}
```

**Response:**
```json
{
  "id": "acc-001",
  "accountNumber": "1234567890",
  "balance": 9500.00,
  "currency": "USD",
  "status": "ACTIVE",
  "ownerName": "Juan Pérez",
  "dailyLimit": 50000.00,
  "dailyUsed": 500.00,
  "createdAt": "2025-01-01T00:00:00"
}
```

---

## 🔍 Flujo: Consultar Transferencia

```
Cliente          Controller        TransferService     TransferRepository     TransferMapper
  │                  │                    │                     │                    │
  │ GET /transfers  │                    │                     │                    │
  │    /{id}        │                    │                     │                    │
  ├────────────────>│                    │                     │                    │
  │                  │                    │                     │                    │
  │                  │ getTransferById()  │                     │                    │
  │                  ├───────────────────>│                     │                    │
  │                  │                    │                     │                    │
  │                  │                    │ findById()          │                    │
  │                  │                    ├────────────────────>│                    │
  │                  │                    │                     │                    │
  │                  │                    │ ◄───────────────────┤                    │
  │                  │                    │ Optional<Transfer>  │                    │
  │                  │                    │                     │                    │
  │                  │                    │ toResponse()        │                    │
  │                  │                    ├──────────────────────────────────────────>│
  │                  │                    │                     │                    │
  │                  │ ◄───────────────────┤                     │                    │
  │                  │ TransferResponse   │                     │                    │
  │                  │                    │                     │                    │
  │ ◄────────────────┤                    │                     │                    │
  │ 200 OK           │                    │                     │                    │
```

---

## 🔐 Flujo: Transferencia con Aprobación Manual

### Escenario: Monto > $10,000

```
Cliente          TransferService                    ApprovalService (Futuro)
  │                    │                                      │
  │ Transfer $15,000   │                                      │
  ├───────────────────>│                                      │
  │                    │                                      │
  │                    │ amount > 10000?                      │
  │                    │ → requiresApproval = true            │
  │                    │                                      │
  │                    │ save(transfer)                       │
  │                    │ status = PENDING                     │
  │                    │                                      │
  │                    │ NO EJECUTAR executeTransfer()        │
  │                    │                                      │
  │ ◄───────────────────┤                                      │
  │ {                  │                                      │
  │   status: PENDING, │                                      │
  │   requiresApproval: true                                  │
  │ }                  │                                      │
  │                    │                                      │
  │                    │                                      │
  │ (Más tarde...)     │                                      │
  │ ADMIN aprueba      │                                      │
  ├────────────────────────────────────────────────────────────>│
  │                    │                                      │
  │                    │ approve(transferId, adminUserId)     │
  │                    ◄───────────────────────────────────────┤
  │                    │                                      │
  │                    │ transfer.setApprovedBy(adminId)      │
  │                    │ transfer.setApprovedAt(NOW)          │
  │                    │                                      │
  │                    │ executeTransfer(transfer)            │
  │                    │                                      │
  │                    │ status = COMPLETED                   │
  │                    │                                      │
```

### Implementación

```java
public TransferResponse createTransfer(TransferRequest request) {
    // ... validaciones ...
    
    BigDecimal approvalThreshold = new BigDecimal("10000.00");
    boolean requiresApproval = request.getAmount().compareTo(approvalThreshold) > 0;
    
    Transfer transfer = Transfer.builder()
        // ... otros campos ...
        .requiresApproval(requiresApproval)
        .build();
    
    transfer = transferRepository.save(transfer);
    
    auditService.createAuditEvent(/*...*/);
    idempotencyService.store(/*...*/);
    
    // Solo ejecutar si NO requiere aprobación
    if (!requiresApproval) {
        executeTransfer(transfer);
    } else {
        log.info("Transferencia {} requiere aprobación manual", transfer.getId());
        // Enviar notificación a ADMIN (por implementar)
    }
    
    return transferMapper.toResponse(transfer);
}

// Método para que ADMIN apruebe
@Transactional
public TransferResponse approveTransfer(UUID transferId, UUID adminUserId) {
    Transfer transfer = transferRepository.findById(transferId)
        .orElseThrow(() -> new TransferNotFoundException(transferId));
    
    if (transfer.getStatus() != TransferStatus.PENDING) {
        throw new IllegalStateException("Solo se pueden aprobar transferencias PENDING");
    }
    
    if (!transfer.getRequiresApproval()) {
        throw new IllegalStateException("Esta transferencia no requiere aprobación");
    }
    
    transfer.setApprovedBy(adminUserId);
    transfer.setApprovedAt(LocalDateTime.now());
    
    auditService.createAuditEvent(
        transfer.getId(),
        "Transfer",
        AuditEventType.TRANSFER_APPROVED,
        Map.of("approvedBy", adminUserId)
    );
    
    executeTransfer(transfer);
    
    return transferMapper.toResponse(transfer);
}
```

---

## ⚠️ Flujo: Manejo de Errores y Compensación (Saga Pattern)

### Escenario: Falla el crédito después del débito

```
TransferService                 AccountRepository           Exception
  │                                     │                        │
  │ executeTransfer()                   │                        │
  │                                     │                        │
  │ source.debit(500)                   │                        │
  │ save(source) ✅                     │                        │
  ├────────────────────────────────────>│                        │
  │ Balance: $10,000 → $9,500           │                        │
  │                                     │                        │
  │ destination.credit(500)             │                        │
  │ save(destination) ❌                │                        │
  ├────────────────────────────────────>│                        │
  │                                     │                        │
  │                                     │ FALLO DE RED           │
  │                                     ├───────────────────────>│
  │                                     │                        │
  │ catch (Exception e)                 │                        │
  │ {                                   │                        │
  │   // COMPENSACIÓN                   │                        │
  │   source.credit(500)  // Devolver   │                        │
  │   save(source)                      │                        │
  ├────────────────────────────────────>│                        │
  │   Balance: $9,500 → $10,000 ✅      │                        │
  │                                     │                        │
  │   transfer.markAsCompensated()      │                        │
  │   save(transfer)                    │                        │
  │                                     │                        │
  │   auditEvent(COMPENSATED)           │                        │
  │ }                                   │                        │
```

### Implementación

```java
private void executeTransfer(Transfer transfer) {
    transfer.startProcessing();
    transferRepository.save(transfer);
    
    try {
        Account source = transfer.getSourceAccount();
        Account destination = transfer.getDestinationAccount();
        BigDecimal amount = transfer.getAmount();
        
        // Paso 1: Debitar (EXITOSO)
        source.debit(amount);
        accountRepository.save(source);
        
        auditService.createAuditEvent(
            transfer.getId(),
            "Transfer",
            AuditEventType.ACCOUNT_DEBITED,
            Map.of("accountNumber", source.getAccountNumber(), "amount", amount)
        );
        
        // Paso 2: Creditar (FALLA)
        destination.credit(amount);
        accountRepository.save(destination); // ❌ Excepción aquí
        
        // ... resto del código ...
        
    } catch (Exception e) {
        log.error("Error en transferencia {}: {}", transfer.getId(), e.getMessage());
        
        // COMPENSACIÓN: Revertir el débito
        handleTransferFailure(transfer, e);
        
        throw e; // Re-lanzar para que el controller maneje el error
    }
}

private void handleTransferFailure(Transfer transfer, Exception error) {
    try {
        // Intentar compensar
        Account source = transfer.getSourceAccount();
        BigDecimal amount = transfer.getAmount();
        
        // Devolver el dinero a la cuenta origen
        source.credit(amount);
        accountRepository.save(source);
        
        // Marcar transferencia como compensada
        transfer.markAsCompensated("Error durante ejecución: " + error.getMessage());
        transferRepository.save(transfer);
        
        // Auditar compensación
        auditService.createAuditEvent(
            transfer.getId(),
            "Transfer",
            AuditEventType.TRANSFER_COMPENSATED,
            Map.of(
                "reason", error.getMessage(),
                "amountReturned", amount,
                "accountCredited", source.getAccountNumber()
            )
        );
        
        log.info("Transferencia {} compensada exitosamente", transfer.getId());
        
    } catch (Exception compensationError) {
        log.error("ERROR CRÍTICO: Fallo en compensación de transferencia {}", 
                  transfer.getId(), compensationError);
        
        // Marcar como FAILED y alertar a soporte
        transfer.markAsFailed("Error en compensación: " + compensationError.getMessage());
        transferRepository.save(transfer);
        
        auditService.createAuditEvent(
            transfer.getId(),
            "Transfer",
            AuditEventType.TRANSFER_FAILED,
            Map.of("error", compensationError.getMessage())
        );
        
        // TODO: Enviar alerta a equipo de soporte
    }
}
```

**Resultado:**
```json
HTTP/1.1 500 Internal Server Error

{
  "timestamp": "2025-12-26T10:35:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Error al procesar transferencia. Se ha compensado la operación.",
  "path": "/api/v1/transfers"
}

// En DB: Transfer con status = COMPENSATED
// Juan recuperó sus $500
```

---

## ✅ Flujo: Validaciones de Negocio

### Validaciones por Capa

#### **1. Controller (Bean Validation)**

```java
@NotNull
@DecimalMin("1.00")
BigDecimal amount;

→ Si falla: MethodArgumentNotValidException
→ Handler: 400 BAD_REQUEST con details por campo
```

#### **2. Service (Reglas de Negocio)**

```
┌─────────────────────────────────────────────────┐
│  Validaciones en createTransfer()               │
├─────────────────────────────────────────────────┤
│  1. Cuenta origen existe?                       │
│     → AccountNotFoundException (404)            │
│                                                  │
│  2. Cuenta destino existe?                      │
│     → AccountNotFoundException (404)            │
│                                                  │
│  3. Cuenta origen ACTIVA?                       │
│     → InactiveAccountException (400)            │
│                                                  │
│  4. Cuenta destino puede recibir?               │
│     → InactiveAccountException (400)            │
│                                                  │
│  5. Misma moneda?                               │
│     → CurrencyMismatchException (400)           │
│                                                  │
│  6. Saldo suficiente?                           │
│     → InsufficientBalanceException (400)        │
│                                                  │
│  7. Límite diario no excedido?                  │
│     → DailyLimitExceededException (400)         │
│                                                  │
│  8. Idempotencia no violada?                    │
│     → Retorna transferencia existente           │
└─────────────────────────────────────────────────┘
```

---

## 🔄 Máquina de Estados

```
                    ┌──────────┐
                    │          │
                    │ PENDING  │◄─── Creación inicial
                    │          │
                    └────┬─────┘
                         │
                         │ executeTransfer()
                         │ (si no requiere aprobación)
                         │
                    ┌────▼─────┐
                    │          │
                    │PROCESSING│◄─── Ejecución en curso
                    │          │
                    └────┬─────┘
                         │
           ┌─────────────┼─────────────┐
           │             │             │
           │ Éxito       │ Error       │ Error + Compensación
           │             │             │
      ┌────▼────┐   ┌────▼────┐  ┌────▼────────┐
      │         │   │         │  │             │
      │COMPLETED│   │ FAILED  │  │ COMPENSATED │
      │         │   │         │  │             │
      └─────────┘   └─────────┘  └─────────────┘
      
      (FINAL)       (FINAL)      (FINAL)
```

### Transiciones Permitidas

| Estado Actual | Evento | Estado Siguiente | Condición |
|--------------|--------|------------------|-----------|
| PENDING | `executeTransfer()` | PROCESSING | No requiere aprobación |
| PENDING | `approveTransfer()` | PROCESSING | Requiere aprobación + Admin aprobó |
| PROCESSING | `markAsCompleted()` | COMPLETED | Ejecución exitosa |
| PROCESSING | `markAsFailed()` | FAILED | Error sin posibilidad de compensar |
| PROCESSING | `markAsCompensated()` | COMPENSATED | Error + compensación exitosa |

**Estados finales:** No permiten más transiciones.

---

## 🏗️ Interacción entre Componentes

### Vista de Alto Nivel

```
┌──────────────────────────────────────────────────────────────────┐
│                          CLIENT                                  │
└───────────────────────────────┬──────────────────────────────────┘
                                │
                                │ HTTP Request
                                │
┌───────────────────────────────▼──────────────────────────────────┐
│                        CONTROLLER LAYER                          │
│  - TransferController                                            │
│  - AccountController                                             │
│  - @Valid, @RequestBody, @PathVariable                           │
└───────────────────────────────┬──────────────────────────────────┘
                                │
                                │ DTO
                                │
┌───────────────────────────────▼──────────────────────────────────┐
│                         SERVICE LAYER                            │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────────┐   │
│  │ AccountService │  │ TransferService│  │IdempotencyService│   │
│  └────────────────┘  └────────────────┘  └──────────────────┘   │
│                                                                  │
│  @Transactional - Lógica de negocio - Orquestación             │
└─────┬──────────────────┬─────────────────┬──────────────────────┘
      │                  │                 │
      │                  │                 │
      │                  │                 └───────────────┐
      │                  │                                 │
┌─────▼──────────────────▼─────────────────┐         ┌────▼─────┐
│       REPOSITORY LAYER                   │         │  REDIS   │
│  - AccountRepository                     │         │          │
│  - TransferRepository                    │         │  Cache   │
│  - LedgerEntryRepository                 │         └──────────┘
│  - AuditEventRepository                  │
│                                          │
│  @Lock, @Query, Spring Data JPA          │
└─────┬────────────────────────────────────┘
      │
      │ JDBC/Hibernate
      │
┌─────▼────────────────────────────────────┐
│           POSTGRESQL                     │
│                                          │
│  - accounts                              │
│  - transfers                             │
│  - ledger_entries                        │
│  - audit_events                          │
└──────────────────────────────────────────┘
```

### Flujo de Datos

```
Request DTO
    ↓
Controller (validación)
    ↓
Service (lógica de negocio)
    ↓
Repository (persistencia)
    ↓
Entity
    ↓
Mapper (Entity → DTO)
    ↓
Response DTO
    ↓
Controller
    ↓
Cliente (JSON)
```

---

## 📝 Resumen de Responsabilidades

### Controller
- ✅ Recibir requests HTTP
- ✅ Validar estructura con `@Valid`
- ✅ Delegar a servicios
- ✅ Retornar responses HTTP
- ❌ NO contiene lógica de negocio

### Service
- ✅ Lógica de negocio
- ✅ Validaciones complejas
- ✅ Orquestación de repositorios
- ✅ Gestión de transacciones
- ✅ Auditoría
- ❌ NO accede directamente a HTTP

### Repository
- ✅ Acceso a datos
- ✅ Queries SQL/JPQL
- ✅ Locks
- ❌ NO contiene lógica de negocio

### Entity
- ✅ Representación del dominio
- ✅ Métodos de negocio simples (debit, credit)
- ✅ Validaciones a nivel de campo
- ❌ NO contiene lógica compleja

---

## 🎯 Próximos Pasos para Implementar

### Orden Recomendado

1. **AccountService** (fácil - práctica)
   ```java
   getAccountById()
   getAccountByNumber()
   getBalance()
   ```

2. **TransferService - Crear transferencia básica**
   ```java
   createTransfer() {
       // Sin validaciones complejas al inicio
       // Solo flujo feliz
   }
   ```

3. **TransferService - Agregar validaciones**
   ```java
   validateTransfer()
   - Todas las reglas de negocio
   ```

4. **TransferService - Auditoría completa**
   ```java
   - Integrar AuditService en cada paso
   ```

5. **TransferService - Compensación**
   ```java
   handleTransferFailure()
   - Implementar Saga Pattern
   ```

6. **TransferService - Aprobación manual**
   ```java
   approveTransfer()
   - Para montos altos
   ```

---

Este documento te da el mapa completo de cómo debe comportarse el sistema. Úsalo como referencia mientras implementas los servicios. 🚀
