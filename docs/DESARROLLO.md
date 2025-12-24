# Autumn Banking System - Guía de Desarrollo

## 🎯 Estado Actual del Proyecto

Se ha creado la **arquitectura base completa** del sistema de transferencias bancarias. El proyecto está configurado con patrones profesionales enterprise-grade.

---

## 📁 Estructura del Proyecto

```
autumn/
├── src/main/java/sys/azentic/autumn/
│   ├── domain/
│   │   ├── entity/          ✅ Entidades JPA con auditoría
│   │   └── enums/           ✅ Enumeraciones del dominio
│   ├── repository/          ✅ Repositorios con queries custom y locks
│   ├── service/             ⏳ Pendiente: lógica de negocio
│   │   └── impl/
│   ├── controller/          ✅ Controllers REST (placeholders)
│   ├── dto/                 ✅ Request/Response DTOs
│   │   ├── request/
│   │   └── response/
│   ├── mapper/              ✅ MapStruct mappers
│   ├── config/              ✅ Configuraciones (JPA, Redis, Security)
│   ├── security/            ✅ Security básica (JWT placeholder)
│   ├── exception/           ✅ Excepciones custom + handler global
│   └── audit/               ✅ Event Sourcing con hash chaining
│
├── src/main/resources/
│   ├── application.yml      ✅ Multi-perfil (dev/test/prod)
│   └── db/migration/        ✅ Scripts Flyway
│
└── pom.xml                  ✅ Dependencias enterprise
```

---

## ✅ Lo que YA está implementado

### 1. **Entidades JPA** (`domain/entity/`)
- ✅ `Account` - Control de concurrencia optimista + métodos de negocio
- ✅ `Transfer` - Máquina de estados completa
- ✅ `LedgerEntry` - Inmutable, double-entry bookkeeping
- ✅ `AuditEvent` - Event sourcing con hash chaining

### 2. **Repositorios** (`repository/`)
- ✅ `AccountRepository` - Incluye `findByAccountNumberWithLock()` para bloqueo pesimista
- ✅ `TransferRepository` - Búsquedas por idempotency key
- ✅ `LedgerEntryRepository` - Solo lectura/inserción
- ✅ `AuditEventRepository` - Validación de cadena de hashes

### 3. **Infraestructura**
- ✅ **Redis** configurado para idempotencia (TTL 24h)
- ✅ **Flyway** con migraciones SQL completas + datos de prueba
- ✅ **Spring Security** básica (permitiendo todo por ahora)
- ✅ **Exception Handling** global con respuestas RFC 7807

### 4. **Auditoría**
- ✅ `AuditService` - Crea eventos inmutables con SHA-256 hash chaining
- ✅ `JpaAuditingConfig` - @CreatedDate, @LastModifiedDate automáticos

### 5. **DTOs y Validaciones**
- ✅ `TransferRequest` - Validaciones Bean Validation completas
- ✅ `TransferResponse`, `AccountResponse`, `ErrorResponse`
- ✅ Mappers MapStruct automáticos

---

## 🚧 Siguiente Fase: Implementar Servicios (Tu Trabajo)

### **Tarea 1: Crear `AccountService`**
**Ubicación:** `service/impl/AccountServiceImpl.java`

**Responsabilidades:**
```java
@Service
@Transactional
public class AccountServiceImpl implements AccountService {
    
    // Métodos a implementar:
    
    1. getAccountById(UUID id)
       - Buscar en AccountRepository
       - Lanzar AccountNotFoundException si no existe
       - Mapear a AccountResponse
    
    2. getAccountByNumber(String accountNumber)
       - Similar a getAccountById
    
    3. getBalance(UUID accountId)
       - Retornar solo el saldo
    
    4. resetDailyLimits()
       - Proceso nocturno para resetear dailyUsed
       - Usar @Scheduled o ejecutar manualmente
}
```

**Conceptos clave a practicar:**
- Inyección de dependencias (`@RequiredArgsConstructor`)
- `@Transactional` para gestión de transacciones
- Uso de `Optional<>` para manejo de nulos
- Mappers con MapStruct

---

### **Tarea 2: Crear `TransferService` (Lo más complejo)**
**Ubicación:** `service/impl/TransferServiceImpl.java`

**Flujo de creación de transferencia:**

```java
@Service
@Transactional
public class TransferServiceImpl implements TransferService {
    
    public TransferResponse createTransfer(TransferRequest request) {
        
        // PASO 1: Validar idempotencia
        if (idempotencyService.exists(request.getIdempotencyKey())) {
            UUID existingTransferId = idempotencyService.getTransferId(...);
            return getTransferById(existingTransferId); // Retornar existente
        }
        
        // PASO 2: Obtener cuentas CON BLOQUEO PESIMISTA
        Account source = accountRepository
            .findByAccountNumberWithLock(request.getSourceAccountNumber())
            .orElseThrow(() -> new AccountNotFoundException(...));
        
        Account destination = accountRepository
            .findByAccountNumberWithLock(request.getDestinationAccountNumber())
            .orElseThrow(() -> new AccountNotFoundException(...));
        
        // PASO 3: Validaciones de negocio
        validateTransfer(source, destination, request.getAmount());
        
        // PASO 4: Crear entidad Transfer con estado PENDING
        Transfer transfer = Transfer.builder()
            .idempotencyKey(request.getIdempotencyKey())
            .sourceAccount(source)
            .destinationAccount(destination)
            .amount(request.getAmount())
            .status(TransferStatus.PENDING)
            .requiresApproval(request.getAmount().compareTo(new BigDecimal("10000")) > 0)
            .build();
        
        transfer = transferRepository.save(transfer);
        
        // PASO 5: Auditoría
        auditService.createAuditEvent(
            transfer.getId(),
            "Transfer",
            AuditEventType.TRANSFER_CREATED,
            transfer,
            null,
            null
        );
        
        // PASO 6: Almacenar en Redis para idempotencia
        idempotencyService.store(request.getIdempotencyKey(), transfer.getId());
        
        // PASO 7: Si no requiere aprobación, ejecutar inmediatamente
        if (!transfer.getRequiresApproval()) {
            executeTransfer(transfer);
        }
        
        return transferMapper.toResponse(transfer);
    }
    
    private void executeTransfer(Transfer transfer) {
        // PASO 8: Cambiar estado a PROCESSING
        transfer.startProcessing();
        
        try {
            // PASO 9: Ejecutar débito y crédito
            Account source = transfer.getSourceAccount();
            Account destination = transfer.getDestinationAccount();
            
            source.debit(transfer.getAmount());
            destination.credit(transfer.getAmount());
            
            accountRepository.save(source);
            accountRepository.save(destination);
            
            // PASO 10: Crear asientos en el libro mayor
            LedgerEntry debit = LedgerEntry.createDebit(
                transfer, source, transfer.getAmount(), 
                "Transferencia enviada a " + destination.getAccountNumber()
            );
            
            LedgerEntry credit = LedgerEntry.createCredit(
                transfer, destination, transfer.getAmount(),
                "Transferencia recibida de " + source.getAccountNumber()
            );
            
            ledgerEntryRepository.save(debit);
            ledgerEntryRepository.save(credit);
            
            // PASO 11: Marcar como completada
            transfer.markAsCompleted();
            transferRepository.save(transfer);
            
            // PASO 12: Auditoría de éxito
            auditService.createAuditEvent(
                transfer.getId(),
                "Transfer",
                AuditEventType.TRANSFER_COMPLETED,
                transfer,
                null,
                null
            );
            
        } catch (Exception e) {
            // PASO 13: Manejo de errores (Saga Pattern)
            transfer.markAsFailed(e.getMessage());
            transferRepository.save(transfer);
            
            auditService.createAuditEvent(
                transfer.getId(),
                "Transfer",
                AuditEventType.TRANSFER_FAILED,
                Map.of("error", e.getMessage()),
                null,
                null
            );
            
            throw e;
        }
    }
    
    private void validateTransfer(Account source, Account dest, BigDecimal amount) {
        // Validar estado de cuentas
        if (!source.canTransfer(amount)) {
            throw new InsufficientBalanceException(...);
        }
        
        if (!dest.canReceive()) {
            throw new InactiveAccountException(...);
        }
        
        // Validar moneda
        if (source.getCurrency() != dest.getCurrency()) {
            throw new CurrencyMismatchException(...);
        }
        
        // Validar límite diario
        if (source.getDailyUsed().add(amount).compareTo(source.getDailyLimit()) > 0) {
            throw new DailyLimitExceededException(...);
        }
    }
}
```

---

## 🔧 Cómo Empezar a Desarrollar

### 1. **Configurar Base de Datos PostgreSQL**

```bash
# Opción A: Docker
docker run --name autumn-postgres \
  -e POSTGRES_DB=autumn_dev \
  -e POSTGRES_USER=autumn_user \
  -e POSTGRES_PASSWORD=autumn_pass \
  -p 5432:5432 \
  -d postgres:16

# Opción B: Instalar localmente y crear la DB
createdb autumn_dev
```

### 2. **Configurar Redis**

```bash
# Docker
docker run --name autumn-redis -p 6379:6379 -d redis:7-alpine
```

### 3. **Compilar el Proyecto**

```bash
mvn clean install
```

**Importante:** La primera compilación generará las clases de MapStruct en `target/generated-sources/`.

### 4. **Ejecutar la Aplicación**

```bash
mvn spring-boot:run
```

Flyway ejecutará las migraciones automáticamente y creará las tablas + datos de prueba.

### 5. **Probar los Endpoints**

```bash
# Consultar cuenta de prueba
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

---

## 📚 Conceptos Clave para Aprender

### 1. **Bloqueo Pesimista** (`PESSIMISTIC_WRITE`)
- Evita race conditions cuando dos transferencias intentan debitar la misma cuenta
- Se usa en `AccountRepository.findByAccountNumberWithLock()`
- Solo funciona dentro de `@Transactional`

### 2. **Idempotencia**
- El mismo request enviado múltiples veces produce el mismo resultado
- Se valida con Redis antes de procesar
- Clave para APIs distribuidas

### 3. **Event Sourcing**
- Cada cambio se registra como un evento inmutable
- Los eventos tienen hash chaining para detectar manipulaciones
- Se puede reconstruir el estado del sistema desde los eventos

### 4. **Patrón Saga**
- Para transacciones distribuidas o de múltiples pasos
- Si falla un paso, se ejecuta compensación (rollback manual)
- Ejemplo: Si el crédito falla después del débito, se revierte el débito

### 5. **Transacciones Spring**
- `@Transactional` crea una transacción de base de datos
- Si ocurre una excepción, todo se revierte automáticamente
- Importante: Los locks pesimistas se liberan al terminar la transacción

---

## 🎓 Ejercicios Propuestos (Orden de Dificultad)

### Nivel 1: Básico
1. ✅ Implementar `AccountService.getAccountById()`
2. ✅ Implementar `AccountService.getAccountByNumber()`
3. ✅ Conectar los controllers con los servicios

### Nivel 2: Intermedio
4. ✅ Implementar `TransferService.createTransfer()` (sin validaciones complejas)
5. ✅ Probar idempotencia enviando el mismo request dos veces
6. ✅ Implementar `TransferService.getTransferById()`

### Nivel 3: Avanzado
7. ✅ Implementar todas las validaciones de negocio
8. ✅ Agregar auditoría completa en cada paso
9. ✅ Implementar lógica de aprobación manual para montos altos

### Nivel 4: Expert
10. ✅ Implementar compensación (Saga Pattern) cuando falla el crédito
11. ✅ Crear tests de concurrencia con múltiples hilos
12. ✅ Implementar JWT Authentication Filter completo

---

## 🧪 Testing

Los tests se crearán con **Testcontainers** para PostgreSQL y Redis reales:

```java
@SpringBootTest
@Testcontainers
class TransferServiceTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);
    
    @Test
    void shouldCreateTransferSuccessfully() {
        // ...
    }
}
```

---

## 🚀 Próximos Pasos Sugeridos

1. **Semana 1-2**: Implementar servicios básicos (Account, Transfer sin validaciones)
2. **Semana 3**: Agregar validaciones completas y auditoría
3. **Semana 4**: Testing exhaustivo + manejo de errores
4. **Semana 5**: Implementar JWT + autorización
5. **Semana 6**: Tests de concurrencia + optimizaciones

---

## 📖 Recursos de Aprendizaje

- **Transacciones**: [Spring Transaction Management](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)
- **JPA Locking**: [Optimistic vs Pessimistic](https://www.baeldung.com/jpa-optimistic-locking)
- **Event Sourcing**: [Martin Fowler - Event Sourcing](https://martinfowler.com/eaaDev/EventSourcing.html)
- **Saga Pattern**: [Microservices.io - Saga](https://microservices.io/patterns/data/saga.html)

---

## 💡 Consejos

1. **Empieza simple**: No intentes implementar todo a la vez
2. **Usa logs**: Agrega `log.debug()` para entender el flujo
3. **Testea manualmente**: Usa Postman o curl antes de escribir tests
4. **Lee los comentarios**: Cada clase tiene documentación explicativa
5. **Pregunta**: Si algo no está claro, revisa la documentación de Spring Boot

---

¡Tienes una base sólida para construir un sistema bancario profesional! 🎉
