package sys.azentic.autumn.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sys.azentic.autumn.domain.entity.AuditEvent;
import sys.azentic.autumn.domain.enums.AuditEventType;
import sys.azentic.autumn.repository.AuditEventRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Servicio de auditoría con Event Sourcing y Hash Chaining.
 * 
 * Cada evento genera un hash SHA-256 que incluye:
 * - Hash del evento anterior
 * - ID del agregado
 * - Tipo de evento
 * - Payload
 * - Timestamp
 * 
 * Esto crea una cadena de confianza inmutable.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * Crea un nuevo evento de auditoría.
     * La transacción es REQUIRES_NEW para garantizar que el evento se persista
     * incluso si la transacción principal falla.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditEvent createAuditEvent(
            UUID aggregateId,
            String aggregateType,
            AuditEventType eventType,
            Object payloadObject,
            UUID userId,
            String metadata) {
        
        try {
            // Serializar el payload a JSON
            String payload = objectMapper.writeValueAsString(payloadObject);
            
            // Obtener el hash del último evento para encadenamiento
            String previousHash = auditEventRepository.findLatestEvent()
                .map(AuditEvent::getEventHash)
                .orElse("0"); // El primer evento tiene previousHash = "0"
            
            // Calcular el hash del evento actual
            LocalDateTime now = LocalDateTime.now();
            String eventHash = calculateHash(previousHash, aggregateId, eventType, payload, now);
            
            // Crear el evento
            AuditEvent event = AuditEvent.builder()
                .aggregateId(aggregateId)
                .aggregateType(aggregateType)
                .eventType(eventType)
                .payload(payload)
                .eventHash(eventHash)
                .previousHash(previousHash)
                .userId(userId)
                .metadata(metadata)
                .build();
            
            AuditEvent savedEvent = auditEventRepository.save(event);
            log.debug("Evento de auditoría creado: {} para agregado {}", eventType, aggregateId);
            
            return savedEvent;
            
        } catch (Exception e) {
            log.error("Error al crear evento de auditoría", e);
            throw new RuntimeException("Error al crear evento de auditoría", e);
        }
    }

    /**
     * Sobrecarga sin userId ni metadata para eventos del sistema.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditEvent createAuditEvent(
            UUID aggregateId,
            String aggregateType,
            AuditEventType eventType,
            Object payloadObject) {
        return createAuditEvent(aggregateId, aggregateType, eventType, payloadObject, null, null);
    }

    /**
     * Calcula el hash SHA-256 del evento.
     */
    private String calculateHash(
            String previousHash,
            UUID aggregateId,
            AuditEventType eventType,
            String payload,
            LocalDateTime timestamp) {
        
        try {
            String data = previousHash + "|" +
                         aggregateId.toString() + "|" +
                         eventType.name() + "|" +
                         payload + "|" +
                         timestamp.toString();
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            
            return HexFormat.of().formatHex(hashBytes);
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al calcular hash SHA-256", e);
        }
    }

    /**
     * Verifica la integridad de la cadena de eventos.
     * Retorna true si todos los hashes son válidos.
     */
    @Transactional(readOnly = true)
    public boolean verifyHashChain() {
        var events = auditEventRepository.findAllForHashValidation();
        
        for (AuditEvent event : events) {
            String recalculatedHash = calculateHash(
                event.getPreviousHash(),
                event.getAggregateId(),
                event.getEventType(),
                event.getPayload(),
                event.getCreatedAt()
            );
            
            if (!recalculatedHash.equals(event.getEventHash())) {
                log.error("¡Integridad de hash comprometida! Evento ID: {}", event.getId());
                return false;
            }
        }
        
        return true;
    }
}
