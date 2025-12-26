# 📘 Arquitectura y Configuración del Proyecto Autumn Banking System

**Sistema de Transferencias Bancarias - Enterprise Grade**

Fecha de creación: Diciembre 2025  
Framework: Spring Boot 4.0.1 + Java 21

---

## 📑 Tabla de Contenidos

1. [Resumen del Proyecto](#resumen-del-proyecto)
2. [Configuración del Proyecto](#configuración-del-proyecto)
3. [Estructura de Paquetes](#estructura-de-paquetes)
4. [Modelo de Datos](#modelo-de-datos)
5. [Patrones de Diseño](#patrones-de-diseño)
6. [Infraestructura](#infraestructura)
7. [DTOs y Validaciones](#dtos-y-validaciones)
8. [Manejo de Excepciones](#manejo-de-excepciones)
9. [Docker y Automatización](#docker-y-automatización)
10. [Endpoints REST](#endpoints-rest)
11. [Estado Actual](#estado-actual)
12. [Próximos Pasos](#próximos-pasos)
13. [Conceptos Clave](#conceptos-clave)

---

## 🎯 Resumen del Proyecto

Se ha creado la **arquitectura base completa** de un sistema bancario profesional de transferencias con patrones enterprise-grade. El proyecto está listo para que implementes la lógica de negocio mientras aprendes conceptos avanzados de Spring Boot.

### Características Principales

- ✅ **Idempotencia** con Redis (evita transferencias duplicadas)
- ✅ **Event Sourcing** con hash chaining SHA-256 (auditoría inmutable)
- ✅ **Bloqueo Pesimista** para prevenir race conditions
- ✅ **Double-Entry Bookkeeping** (contabilidad profesional)
- ✅ **Multi-perfil** (dev, test, prod)
- ✅ **Migraciones Flyway** con datos de prueba
- ✅ **Docker Compose** para infraestructura
- ✅ **Makefile** con comandos útiles

---

## 🏗️ Configuración del Proyecto

### Dependencias Principales (pom.xml)

| Categoría | Tecnología | Propósito |
|-----------|-----------|-----------|
| **Framework** | Spring Boot 4.0.1 | Base del proyecto |
| **Persistencia** | Spring Data JPA + Hibernate | ORM y gestión de base de datos |
| **Base de Datos** | PostgreSQL 16 | Base de datos relacional |
| **Migraciones** | Flyway | Versionado de esquema DB |
| **Cache** | Spring Data Redis | Idempotencia y cache |
| **Seguridad** | Spring Security | Autenticación y autorización |
| **Validación** | Bean Validation | Validaciones declarativas |
| **JWT** | jjwt 0.12.6 | Tokens de autenticación (placeholder) |
| **Reducción Código** | Lombok 1.18.34 | Elimina boilerplate (@Getter, @Slf4j) |
| **Mapeo** | MapStruct 1.6.3 | Conversión Entity ↔ DTO automática |
| **Testing** | Testcontainers 1.20.4 | Tests con contenedores reales |

### Plugins Maven

```xml
<plugin>maven-compiler-plugin</plugin>
  - Procesa anotaciones: Lombok + MapStruct
  - Genera código en tiempo de compilación

<plugin>spring-boot-maven-plugin</plugin>
  - Empaqueta JAR ejecutable
```

---

## 🗂️ Estructura de Paquetes (Clean Architecture)

```
sys.azentic.autumn/
│
├── domain/
│   ├── entity/              # Entidades JPA del dominio
│   │   ├── Account.java
│   │   ├── Transfer.java
│   │   ├── LedgerEntry.java
│   │   └── AuditEvent.java
│   │
│   └── enums/               # Enumeraciones
│       ├── TransferStatus.java
│       ├── AccountStatus.java
│       ├── Currency.java
│       ├── LedgerEntryType.java
│       └── AuditEventType.java
│
├── repository/              # Acceso a datos (Spring Data JPA)
│   ├── AccountRepository.java
│   ├── TransferRepository.java
│   ├── LedgerEntryRepository.java
│   └── AuditEventRepository.java
│
├── service/                 # Lógica de negocio (PENDIENTE DE IMPLEMENTAR)
│   ├── AccountService.java
│   ├── TransferService.java
│   ├── IdempotencyService.java  ✅ Implementado
│   └── impl/
│
├── controller/              # Endpoints REST
│   ├── TransferController.java  ✅ Skeleton creado
│   └── AccountController.java   ✅ Skeleton creado
│
├── dto/
│   ├── request/             # DTOs de entrada
│   │   └── TransferRequest.java
│   │
│   └── response/            # DTOs de salida
│       ├── TransferResponse.java
│       ├── AccountResponse.java
│       └── ErrorResponse.java
│
├── mapper/                  # MapStruct - conversión automática
│   ├── TransferMapper.java
│   └── AccountMapper.java
│
├── config/                  # Configuraciones
│   ├── JpaAuditingConfig.java
│   ├── RedisConfig.java
│   └── SecurityConfig.java
│
├── security/                # JWT (placeholders)
│   ├── JwtUserDetails.java
│   └── JwtUtils.java
│
├── exception/               # Excepciones custom
│   ├── AccountNotFoundException.java
│   ├── TransferNotFoundException.java
│   ├── InsufficientBalanceException.java
│   ├── DuplicateTransferException.java
│   ├── InactiveAccountException.java
│   ├── CurrencyMismatchException.java
│   ├── DailyLimitExceededException.java
│   └── GlobalExceptionHandler.java  ✅ @RestControllerAdvice
│
└── audit/                   # Event Sourcing
    └── AuditService.java    ✅ Hash chaining implementado
```

---

## 💾 Modelo de Datos

### 📊 Diagrama de Entidades

```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│   Account   │◄───────►│   Transfer   │────────►│ LedgerEntry │
└─────────────┘         └──────────────┘         └─────────────┘
      │                        │
      │                        │
      │                        ▼
      │                 ┌──────────────┐
      └────────────────►│  AuditEvent  │
                        └──────────────┘
```

### 1️⃣ Account (Cuenta Bancaria)

```java
@Entity
@Table(name = "accounts")
public class Account {
    UUID id;                        // PK
    String accountNumber;           // Único, índice
    BigDecimal balance;             // Saldo actual
    Currency currency;              // USD, EUR, MXN, PEN
    AccountStatus status;           // ACTIVE, BLOCKED, CLOSED
    Long version;                   // ⚡ Control de concurrencia optimista
    String ownerName;
    String ownerEmail;
    BigDecimal dailyLimit;          // Límite diario de transferencias
    BigDecimal dailyUsed;           // Acumulado usado hoy
    LocalDateTime createdAt;        // @CreatedDate
    LocalDateTime updatedAt;        // @LastModifiedDate
    
    // Métodos de negocio
    boolean canTransfer(BigDecimal amount);
    boolean canReceive();
    void debit(BigDecimal amount);
    void credit(BigDecimal amount);
    void resetDailyUsage();
}
```

**Características:**
- ✅ Control de concurrencia **optimista** con `@Version`
- ✅ Métodos de negocio encapsulados
- ✅ Auditoría automática con `@CreatedDate` y `@LastModifiedDate`

---

### 2️⃣ Transfer (Transferencia)

```java
@Entity
@Table(name = "transfers")
public class Transfer {
    UUID id;                        // PK
    UUID idempotencyKey;            // ⚡ Clave única para evitar duplicados
    Account sourceAccount;          // FK - Cuenta origen
    Account destinationAccount;     // FK - Cuenta destino
    BigDecimal amount;              // Monto
    TransferStatus status;          // Estado: PENDING → PROCESSING → COMPLETED/FAILED/COMPENSATED
    String description;
    String errorMessage;
    Boolean requiresApproval;       // true si amount > $10,000
    UUID approvedBy;
    LocalDateTime approvedAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    LocalDateTime completedAt;
    
    // Métodos de estado
    void markAsCompleted();
    void markAsFailed(String error);
    void markAsCompensated(String reason);
    void startProcessing();
    boolean isFinalState();
}
```

**Estados posibles:**
```
PENDING → PROCESSING → COMPLETED
                    ├→ FAILED
                    └→ COMPENSATED (Saga Pattern)
```

**Características:**
- ✅ Máquina de estados bien definida
- ✅ Idempotencia con `idempotencyKey` (índice único)
- ✅ Aprobación manual para montos altos

---

### 3️⃣ LedgerEntry (Libro Mayor) - **INMUTABLE**

```java
@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {
    UUID id;                        // PK
    Transfer transfer;              // FK - Transferencia asociada
    Account account;                // FK - Cuenta afectada
    LedgerEntryType type;           // DEBIT o CREDIT
    BigDecimal amount;              // Monto del movimiento
    BigDecimal balanceAfter;        // ⚡ Saldo DESPUÉS del movimiento
    String description;
    LocalDateTime createdAt;
    
    // Factory methods
    static LedgerEntry createDebit(...);
    static LedgerEntry createCredit(...);
}
```

**Double-Entry Bookkeeping:**
```
Transferencia de $500: Juan → María

LedgerEntry #1:
  - account: Juan
  - type: DEBIT
  - amount: 500
  - balanceAfter: 9500

LedgerEntry #2:
  - account: María
  - type: CREDIT
  - amount: 500
  - balanceAfter: 5500
```

**CRÍTICO:** Esta tabla es **INMUTABLE**. Solo se permite:
- ✅ INSERT (crear nuevos registros)
- ✅ SELECT (consultar)
- ❌ UPDATE (NUNCA)
- ❌ DELETE (NUNCA)

---

### 4️⃣ AuditEvent (Event Sourcing) - **INMUTABLE**

```java
@Entity
@Table(name = "audit_events")
public class AuditEvent {
    UUID id;                        // PK
    UUID aggregateId;               // ID de la entidad auditada (ej: transferId)
    String aggregateType;           // "Transfer", "Account", etc.
    AuditEventType eventType;       // TRANSFER_CREATED, TRANSFER_COMPLETED, etc.
    String payload;                 // ⚡ Datos del evento (JSON)
    String eventHash;               // ⚡ SHA-256 del evento actual
    String previousHash;            // ⚡ Hash del evento anterior (chain)
    UUID userId;
    String metadata;
    LocalDateTime createdAt;
}
```

**Hash Chaining:**
```
Evento 1:
  previousHash: "0" (primer evento)
  eventHash: SHA256(0 + data) = "a3f7..."

Evento 2:
  previousHash: "a3f7..."
  eventHash: SHA256(a3f7 + data) = "b8c2..."

Evento 3:
  previousHash: "b8c2..."
  eventHash: SHA256(b8c2 + data) = "d1e9..."
```

**Si alguien modifica el Evento 2 manualmente en la DB:**
- El `eventHash` del Evento 2 ya no coincide
- El `previousHash` del Evento 3 apunta al hash viejo
- ✅ **La cadena se rompe → DETECTADO**

---

## 🎯 Patrones de Diseño

### 1. Idempotencia (Redis)

**Problema:** Cliente envía el mismo request 2 veces por error de red.

**Solución:**
```java
@Service
public class IdempotencyService {
    
    // TTL: 24 horas
    boolean exists(UUID idempotencyKey);
    boolean store(UUID idempotencyKey, UUID transferId);
    UUID getTransferId(UUID idempotencyKey);
}
```

**Flujo:**
```
1. Cliente envía: POST /transfers { idempotencyKey: "abc123", ... }
2. Sistema verifica Redis: exists("abc123")? NO
3. Sistema procesa transferencia → ID: "xyz789"
4. Sistema guarda en Redis: store("abc123", "xyz789")
5. Cliente reintenta (error de red): POST /transfers { idempotencyKey: "abc123", ... }
6. Sistema verifica Redis: exists("abc123")? SÍ
7. Sistema retorna transferencia existente: get("abc123") → "xyz789"
```

✅ **Resultado:** Solo se ejecuta una vez, pero el cliente recibe respuesta exitosa en ambos casos.

---

### 2. Event Sourcing con Hash Chaining

**Problema:** Auditoría que puede ser manipulada.

**Solución:**
```java
@Service
public class AuditService {
    
    AuditEvent createAuditEvent(UUID aggregateId, AuditEventType type, Object payload);
    String calculateHash(String prevHash, UUID aggId, AuditEventType type, String payload, LocalDateTime ts);
    boolean verifyHashChain(); // Detecta manipulación
}
```

**Cálculo del Hash:**
```java
String data = previousHash + "|" + aggregateId + "|" + eventType + "|" + payload + "|" + timestamp;
byte[] hashBytes = SHA-256(data);
String eventHash = HEX(hashBytes);
```

**Beneficios:**
- ✅ Inmutabilidad verificable
- ✅ Detección de manipulación
- ✅ Reconstrucción del estado desde eventos
- ✅ Compliance y auditoría forense

---

### 3. Bloqueo Pesimista (Prevenir Race Conditions)

**Problema:**
```
Cuenta Juan: $1000

T1: Transferir $600 a María  ──┐
T2: Transferir $700 a Carlos ──┤── Ejecutan simultáneamente
                                └─→ Saldo final: -$300 ❌
```

**Solución:**
```java
@Repository
public interface AccountRepository {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberWithLock(String accountNumber);
}
```

**Flujo corregido:**
```
T1: findByAccountNumberWithLock("Juan") → BLOQUEA fila
T2: findByAccountNumberWithLock("Juan") → ESPERA...
T1: Procesa transferencia ($1000 - $600 = $400)
T1: Commit → LIBERA bloqueo
T2: Obtiene cuenta con saldo $400
T2: Valida: $700 > $400 → RECHAZA ✅
```

**IMPORTANTE:** Solo funciona dentro de `@Transactional`.

---

### 4. Double-Entry Bookkeeping

**Principio contable:** Toda operación afecta al menos 2 cuentas.

**Implementación:**
```java
// Transferencia de $500: Juan → María

// Paso 1: Débito en cuenta origen
LedgerEntry debit = LedgerEntry.createDebit(
    transfer, 
    juan, 
    500, 
    "Transferencia enviada a María"
);
juan.debit(500); // Balance: $9500

// Paso 2: Crédito en cuenta destino
LedgerEntry credit = LedgerEntry.createCredit(
    transfer, 
    maria, 
    500,
    "Transferencia recibida de Juan"
);
maria.credit(500); // Balance: $5500

ledgerEntryRepository.save(debit);
ledgerEntryRepository.save(credit);
```

**Reconciliación:**
```sql
SELECT 
    SUM(CASE WHEN type = 'DEBIT' THEN amount ELSE 0 END) AS total_debits,
    SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE 0 END) AS total_credits
FROM ledger_entries
WHERE DATE(created_at) = '2025-12-26';

-- total_debits DEBE IGUALAR total_credits
```

---

### 5. Saga Pattern (Compensación)

**Problema:** Falla el crédito después del débito exitoso.

**Flujo normal:**
```
1. Debitar cuenta origen ✅
2. Creditar cuenta destino ✅
→ Estado: COMPLETED
```

**Flujo con error:**
```
1. Debitar cuenta origen ✅ (Juan: $1000 → $500)
2. Creditar cuenta destino ❌ (Error de red a base de datos)
→ ¿Juan perdió $500? ❌
```

**Compensación:**
```java
try {
    source.debit(500);
    accountRepository.save(source);
    
    destination.credit(500);
    accountRepository.save(destination); // ❌ Falla aquí
    
} catch (Exception e) {
    // COMPENSACIÓN: Revertir débito
    source.credit(500); // Devolver dinero a Juan
    accountRepository.save(source);
    
    transfer.markAsCompensated("Error al acreditar cuenta destino");
    transferRepository.save(transfer);
    
    auditService.createAuditEvent(transfer.getId(), TRANSFER_COMPENSATED, ...);
}
```

✅ **Resultado:** Juan recupera su dinero, estado: `COMPENSATED`.

---

## 🔧 Infraestructura

### application.yml - Multi-perfil

```yaml
# Común para todos los perfiles
spring:
  application:
    name: autumn-banking-system
  jpa:
    open-in-view: false
  flyway:
    enabled: true

---
# Perfil: dev
spring:
  profiles: dev
  datasource:
    url: jdbc:postgresql://localhost:5432/autumn_dev
  data:
    redis:
      host: localhost
      port: 6379

logging:
  level:
    sys.azentic.autumn: DEBUG

---
# Perfil: prod
spring:
  profiles: prod
  datasource:
    url: ${DATABASE_URL}        # Variables de entorno
  data:
    redis:
      host: ${REDIS_HOST}
      password: ${REDIS_PASSWORD}

logging:
  level:
    sys.azentic.autumn: INFO
```

---

### Migraciones Flyway

**Ubicación:** `src/main/resources/db/migration/`

| Archivo | Descripción |
|---------|-------------|
| `V1__create_accounts_table.sql` | Tabla accounts con índices y constraints |
| `V2__create_transfers_table.sql` | Tabla transfers con FK a accounts |
| `V3__create_ledger_entries_table.sql` | Tabla ledger_entries (inmutable) |
| `V4__create_audit_events_table.sql` | Tabla audit_events con hash chaining |
| `V5__insert_sample_data.sql` | 4 cuentas de prueba con saldos |

**Datos de Prueba:**
```sql
1234567890 → Juan Pérez    (USD $10,000) ACTIVE
0987654321 → María García  (USD $5,000)  ACTIVE
1111222233 → Carlos López  (EUR €15,000) ACTIVE
4444555566 → Ana Martínez  (USD $2,000)  BLOCKED
```

**Ejecución:** Automática al iniciar Spring Boot.

---

### Redis Configuration

```java
@Configuration
public class RedisConfig {
    
    @Bean
    RedisTemplate<String, Object> redisTemplate() {
        // Key: String (idempotency:transfer:UUID)
        // Value: JSON (GenericJackson2JsonRedisSerializer)
    }
    
    @Bean
    Duration idempotencyTtl() {
        return Duration.ofHours(24); // TTL para claves
    }
}
```

**Prefijo de claves:** `idempotency:transfer:{UUID}`

---

### Spring Security (Básica)

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.csrf(disable)
            .cors(cors -> ...)
            .sessionManagement(STATELESS)
            .authorizeHttpRequests(auth -> 
                auth.requestMatchers("/actuator/health").permitAll()
                    .anyRequest().permitAll() // TODO: cambiar a .authenticated()
            );
    }
}
```

**Estado actual:** PERMITE TODO (facilita desarrollo inicial).

**TODO futuro:**
- Implementar JwtAuthenticationFilter
- Agregar roles (ADMIN, USER)
- Validar ownership de cuentas

---

## 📋 DTOs y Validaciones

### TransferRequest

```java
public class TransferRequest {
    
    @NotNull(message = "La clave de idempotencia es obligatoria")
    UUID idempotencyKey;
    
    @NotBlank
    @Size(min = 10, max = 20)
    String sourceAccountNumber;
    
    @NotBlank
    @Size(min = 10, max = 20)
    String destinationAccountNumber;
    
    @NotNull
    @DecimalMin("1.00")
    @DecimalMax("999999999.99")
    @Digits(integer = 15, fraction = 4)
    BigDecimal amount;
    
    @Size(max = 500)
    String description;
    
    @AssertTrue(message = "Las cuentas deben ser diferentes")
    boolean isDifferentAccounts() {
        return !sourceAccountNumber.equals(destinationAccountNumber);
    }
}
```

**Validación automática:** `@Valid` en el controller.

---

### TransferResponse

```java
public class TransferResponse {
    UUID id;
    UUID idempotencyKey;
    String sourceAccountNumber;
    String destinationAccountNumber;
    BigDecimal amount;
    TransferStatus status;
    String description;
    String errorMessage;
    Boolean requiresApproval;
    LocalDateTime createdAt;
    LocalDateTime completedAt;
}
```

---

### ErrorResponse (RFC 7807)

```java
public class ErrorResponse {
    LocalDateTime timestamp;
    int status;
    String error;
    String message;
    String path;
    Map<String, String> details; // Errores de validación campo por campo
}
```

**Ejemplo:**
```json
{
  "timestamp": "2025-12-26T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Error de validación en los campos de entrada",
  "path": "/api/v1/transfers",
  "details": {
    "amount": "El monto mínimo es 1.00",
    "sourceAccountNumber": "El número de cuenta es obligatorio"
  }
}
```

---

## ⚠️ Manejo de Excepciones

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(AccountNotFoundException.class)
    → 404 NOT_FOUND
    
    @ExceptionHandler(InsufficientBalanceException.class)
    → 400 BAD_REQUEST
    
    @ExceptionHandler(DuplicateTransferException.class)
    → 409 CONFLICT
    
    @ExceptionHandler(InactiveAccountException.class)
    → 400 BAD_REQUEST
    
    @ExceptionHandler(CurrencyMismatchException.class)
    → 400 BAD_REQUEST
    
    @ExceptionHandler(DailyLimitExceededException.class)
    → 400 BAD_REQUEST
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    → 400 BAD_REQUEST (con details por campo)
    
    @ExceptionHandler(Exception.class)
    → 500 INTERNAL_SERVER_ERROR
}
```

**Todas las respuestas tienen formato `ErrorResponse`.**

---

## 🐳 Docker y Automatización

### docker-compose.yml

```yaml
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: autumn_dev
      POSTGRES_USER: autumn_user
      POSTGRES_PASSWORD: autumn_pass
    ports: ["5432:5432"]
    volumes: [postgres_data:/var/lib/postgresql/data]
    healthcheck: [pg_isready]
    
  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]
    volumes: [redis_data:/data]
    healthcheck: [redis-cli ping]
    
  app:
    build: .
    ports: ["8080:8080"]
    depends_on:
      - postgres
      - redis
```

---

### Makefile - Comandos Principales

| Comando | Descripción |
|---------|-------------|
| `make help` | Lista todos los comandos |
| `make setup` | Instala dependencias Maven |
| `make docker-up` | Levanta PostgreSQL + Redis |
| `make docker-up-all` | Levanta TODO (incluye app) |
| `make docker-down` | Detiene servicios |
| `make docker-clean` | Elimina contenedores + volúmenes |
| `make compile` | Compila el proyecto |
| `make test` | Ejecuta tests |
| `make run` | Ejecuta Spring Boot local |
| `make dev` | Flujo completo: docker + compile + run |
| `make dev-docker` | TODO en Docker |
| `make db-connect` | psql a PostgreSQL |
| `make redis-cli` | CLI de Redis |
| `make docker-logs` | Logs en tiempo real |
| `make status` | Estado de servicios + health |

---

### Dockerfile (Multi-stage)

```dockerfile
# Etapa 1: Compilación
FROM maven:3.9-eclipse-temurin-21-alpine AS build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución
FROM eclipse-temurin:21-jre-alpine
COPY --from=build /app/target/autumn-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Optimización:** Build de 2 etapas reduce tamaño de imagen final.

---

## 🌐 Endpoints REST

### Transfers

| Método | Endpoint | Descripción | Estado |
|--------|----------|-------------|--------|
| POST | `/api/v1/transfers` | Crear transferencia | 🟡 Placeholder |
| GET | `/api/v1/transfers/{id}` | Consultar por ID | 🟡 Placeholder |
| GET | `/api/v1/transfers/account/{accountId}` | Listar por cuenta | 🟡 Placeholder |

### Accounts

| Método | Endpoint | Descripción | Estado |
|--------|----------|-------------|--------|
| GET | `/api/v1/accounts/{id}` | Consultar por ID | 🟡 Placeholder |
| GET | `/api/v1/accounts/number/{number}` | Consultar por número | 🟡 Placeholder |
| GET | `/api/v1/accounts/{id}/balance` | Consultar saldo | 🟡 Placeholder |

**Controllers creados pero sin lógica** → Listos para inyectar servicios.

---

## ✅ Estado Actual

### ✅ Completamente Implementado

1. ✅ Estructura de paquetes (Clean Architecture)
2. ✅ Todas las entidades JPA con relaciones
3. ✅ Repositorios con queries custom
4. ✅ Bloqueo pesimista en `AccountRepository`
5. ✅ DTOs con validaciones Bean Validation
6. ✅ Mappers automáticos (MapStruct)
7. ✅ Excepciones custom + handler global
8. ✅ `IdempotencyService` (Redis completo)
9. ✅ `AuditService` (Event Sourcing + hash chaining)
10. ✅ Configuración multi-perfil
11. ✅ Migraciones Flyway + datos de prueba
12. ✅ Docker Compose + Dockerfile
13. ✅ Makefile con 25+ comandos
14. ✅ Spring Security básica
15. ✅ Controllers REST (skeletons)

---

### 🚧 Pendiente de Implementar (Tu trabajo)

#### **Fase 1: Servicios Básicos** (Nivel Principiante)

```java
@Service
public class AccountServiceImpl implements AccountService {
    
    AccountResponse getAccountById(UUID id);
    AccountResponse getAccountByNumber(String accountNumber);
    BigDecimal getBalance(UUID accountId);
}
```

**Objetivo:** Aprender:
- Inyección de dependencias
- `@Transactional`
- Manejo de `Optional<>`
- Uso de mappers

---

#### **Fase 2: Lógica de Transferencias** (Nivel Intermedio/Avanzado)

```java
@Service
@Transactional
public class TransferServiceImpl implements TransferService {
    
    TransferResponse createTransfer(TransferRequest request) {
        // 1. Validar idempotencia (Redis)
        // 2. Buscar cuentas CON LOCK
        // 3. Validar reglas de negocio
        // 4. Crear Transfer (PENDING)
        // 5. Auditar creación
        // 6. Ejecutar si no requiere aprobación
        // 7. Retornar respuesta
    }
    
    private void executeTransfer(Transfer transfer) {
        // 8. Cambiar a PROCESSING
        // 9. Debitar/Creditar
        // 10. Crear LedgerEntries
        // 11. Marcar COMPLETED
        // 12. Auditar
    }
    
    private void validateTransfer(Account src, Account dst, BigDecimal amt) {
        // Validar saldo, estado, moneda, límites
    }
}
```

**Objetivo:** Aprender:
- Transacciones complejas
- Bloqueos pesimistas
- Saga Pattern (compensación)
- Event Sourcing
- Double-entry bookkeeping

---

#### **Fase 3: Features Avanzadas** (Nivel Experto)

- [ ] Aprobación manual para transferencias > $10,000
- [ ] Compensación automática (rollback cuando falla crédito)
- [ ] JWT Authentication Filter completo
- [ ] Rate Limiting (bucket4j)
- [ ] Tests de integración con Testcontainers
- [ ] Tests de concurrencia (múltiples hilos)
- [ ] Proceso nocturno: resetear `dailyUsed`
- [ ] Reconciliación diaria (débitos = créditos)

---

## 🎓 Conceptos Clave que Practicarás

### 1. @Transactional (Spring)

```java
@Transactional
public void createTransfer() {
    // Todo lo que hagas aquí se ejecuta en una transacción
    // Si ocurre una excepción: ROLLBACK automático
    // Si termina sin errores: COMMIT automático
}
```

**Niveles de propagación:**
- `REQUIRED` (default) - Usa transacción existente o crea nueva
- `REQUIRES_NEW` - Siempre crea nueva (usado en AuditService)

---

### 2. Optimistic vs Pessimistic Locking

**Optimistic (`@Version`):**
```java
// Bueno para: Lecturas frecuentes, escrituras raras
Account account = accountRepository.findById(id);
account.setBalance(newBalance);
accountRepository.save(account); 
// ❌ Falla si otro hilo modificó antes (version++)
```

**Pessimistic (`@Lock`):**
```java
// Bueno para: Alta concurrencia en escrituras
Account account = accountRepository.findByAccountNumberWithLock("123");
account.debit(500);
accountRepository.save(account);
// ✅ Nadie más puede modificar hasta que termine la transacción
```

---

### 3. Idempotencia en APIs

**Problema:**
```
Cliente envía: POST /transfer { amount: 500 }
Red falla antes de recibir respuesta
Cliente reintenta: POST /transfer { amount: 500 }
¿Se ejecuta 2 veces? ❌
```

**Solución:**
```
Cliente envía: POST /transfer { 
  idempotencyKey: "abc-123",
  amount: 500 
}

Sistema:
  1. exists("abc-123")? NO → Procesa
  2. store("abc-123", "transfer-xyz")
  3. Retorna transfer-xyz

Cliente reintenta: POST /transfer { 
  idempotencyKey: "abc-123",
  amount: 500 
}

Sistema:
  1. exists("abc-123")? SÍ
  2. getTransferId("abc-123") → "transfer-xyz"
  3. Retorna transfer-xyz existente
```

---

### 4. Event Sourcing

**Concepto:** Almacenar EVENTOS en vez de ESTADO.

**Tradicional:**
```sql
UPDATE accounts SET balance = 9500 WHERE id = 'juan';
-- Perdiste la historia: ¿Por qué cambió?
```

**Event Sourcing:**
```sql
INSERT INTO audit_events (event_type, payload) VALUES (
  'ACCOUNT_DEBITED',
  '{"accountId":"juan", "amount":500, "reason":"Transfer to Maria"}'
);
-- Puedes reconstruir el saldo sumando todos los eventos
```

---

### 5. Double-Entry Bookkeeping

**Principio:** Por cada débito hay un crédito equivalente.

**Ejemplo:**
```
Transferencia $500: Juan → María

Débito:  Juan   -$500  (balance: $9500)
Crédito: María  +$500  (balance: $5500)
```

**Validación:**
```sql
SELECT SUM(amount) FROM ledger_entries WHERE type = 'DEBIT';   -- $50,000
SELECT SUM(amount) FROM ledger_entries WHERE type = 'CREDIT';  -- $50,000
-- DEBEN SER IGUALES
```

---

### 6. Saga Pattern

**Problema:** Transacciones distribuidas que no se pueden revertir con ROLLBACK.

**Ejemplo:**
```
1. Débito exitoso ✅
2. Llamada a API externa para notificar ❌ (Falla)
3. ¿Cómo revertir el débito?
```

**Solución Saga:**
```java
try {
    debit();
    callExternalAPI(); // ❌ Falla
} catch (Exception e) {
    compensateDebit(); // Acción de compensación manual
}
```

**Estados:**
- `COMPLETED` - Todo OK
- `FAILED` - Falló antes de ejecutar
- `COMPENSATED` - Ejecutó parcialmente y se revirtió

---

## 🚀 Cómo Empezar

### 1. Primera Vez

```bash
# Clonar/abrir el proyecto
cd autumn/

# Instalar dependencias
make setup

# Levantar infraestructura
make docker-up

# Compilar (genera clases MapStruct)
make compile

# Ejecutar aplicación
make run
```

**Acceder:**
- Spring Boot: http://localhost:8080/actuator/health
- PostgreSQL: `make db-connect`
- Redis: `make redis-cli`

---

### 2. Desarrollo Diario

```bash
# Levantar todo (una sola línea)
make dev

# Ver logs
make docker-logs

# Estado de servicios
make status
```

---

### 3. Probar Endpoints

**Con curl:**
```bash
# Consultar cuenta
curl http://localhost:8080/api/v1/accounts/number/1234567890

# Crear transferencia (cuando implementes el servicio)
curl -X POST http://localhost:8080/api/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "idempotencyKey": "550e8400-e29b-41d4-a716-446655440099",
    "sourceAccountNumber": "1234567890",
    "destinationAccountNumber": "0987654321",
    "amount": 500.00,
    "description": "Pago de prueba"
  }'
```

**Con Postman:**
- Importar colección (crear carpeta `/postman`)
- Guardar requests para pruebas repetitivas

---

### 4. Testing

```bash
# Ejecutar todos los tests
make test

# Solo unitarios
make test-unit

# Solo integración
make test-integration
```

---

## 📚 Recursos de Aprendizaje

### Documentación Oficial

- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring Security](https://docs.spring.io/spring-security/reference/)
- [Flyway](https://flywaydb.org/documentation/)
- [MapStruct](https://mapstruct.org/)

### Patrones y Buenas Prácticas

- [Martin Fowler - Event Sourcing](https://martinfowler.com/eaaDev/EventSourcing.html)
- [Microservices.io - Saga Pattern](https://microservices.io/patterns/data/saga.html)
- [Baeldung - JPA Locking](https://www.baeldung.com/jpa-optimistic-locking)
- [Spring Transaction Management](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)

---

## 🎯 Plan de Aprendizaje Sugerido

### Semana 1-2: Fundamentos
- ✅ Implementar `AccountService` (getById, getByNumber, getBalance)
- ✅ Conectar controllers con servicios
- ✅ Probar endpoints con Postman
- ✅ Escribir tests unitarios básicos

### Semana 3: Transferencias Básicas
- ✅ Implementar `createTransfer()` sin validaciones complejas
- ✅ Probar idempotencia (enviar mismo request 2 veces)
- ✅ Implementar `getTransferById()`
- ✅ Ver datos en PostgreSQL con `make db-connect`

### Semana 4: Validaciones y Auditoría
- ✅ Agregar todas las validaciones de negocio
- ✅ Integrar `AuditService` en cada paso
- ✅ Crear `LedgerEntry` por cada transferencia
- ✅ Verificar hash chain con `verifyHashChain()`

### Semana 5: Features Avanzadas
- ✅ Lógica de aprobación manual (montos > $10,000)
- ✅ Implementar compensación (Saga Pattern)
- ✅ Tests de concurrencia con múltiples hilos
- ✅ Reconciliación diaria (query SQL)

### Semana 6: Seguridad y Producción
- ✅ Implementar JWT Authentication Filter
- ✅ Agregar roles (ADMIN, USER)
- ✅ Rate Limiting
- ✅ Tests de integración con Testcontainers
- ✅ Documentación con Swagger/OpenAPI

---

## 🎉 Conclusión

Tienes un **proyecto enterprise-grade completo** con:

- ✅ Arquitectura limpia y escalable
- ✅ Patrones profesionales (Event Sourcing, Saga, Idempotencia)
- ✅ Infraestructura dockerizada
- ✅ Configuración multi-entorno
- ✅ Base de código lista para implementar lógica

**Este proyecto te enseñará conceptos que se usan en:**
- Bancos y Fintechs
- Sistemas de pagos (PayPal, Stripe)
- E-commerce de alto tráfico
- Plataformas críticas con compliance

**Siguiente paso:** Implementar `AccountService` y `TransferService` siguiendo la guía en [DESARROLLO.md](DESARROLLO.md).

¡Manos a la obra! 🚀
