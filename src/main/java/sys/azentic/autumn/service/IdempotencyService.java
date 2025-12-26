package sys.azentic.autumn.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * Servicio para gestionar idempotencia usando Redis.
 * Almacena claves únicas para evitar procesamiento duplicado de transferencias.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:transfer:";
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration idempotencyTtl;

    /**
     * Verifica si una clave de idempotencia ya existe.
     * 
     * @param idempotencyKey Clave única de la transferencia
     * @return true si la clave ya existe (transferencia duplicada)
     */
    public boolean exists(UUID idempotencyKey) {
        String key = buildKey(idempotencyKey);
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Almacena una clave de idempotencia con el ID de la transferencia.
     * 
     * @param idempotencyKey Clave única de la transferencia
     * @param transferId ID de la transferencia creada
     * @return true si se almacenó correctamente, false si ya existía
     */
    public boolean store(UUID idempotencyKey, UUID transferId) {
        String key = buildKey(idempotencyKey);
        
        // setIfAbsent retorna true solo si la clave no existía previamente
        Boolean stored = redisTemplate.opsForValue().setIfAbsent(
            key, 
            transferId.toString(), 
            idempotencyTtl
        );
        
        if (Boolean.TRUE.equals(stored)) {
            log.debug("Clave de idempotencia almacenada: {} -> {}", idempotencyKey, transferId);
            return true;
        } else {
            log.warn("Intento de almacenar clave de idempotencia duplicada: {}", idempotencyKey);
            return false;
        }
    }

    /**
     * Obtiene el ID de la transferencia asociada a una clave de idempotencia.
     * 
     * @param idempotencyKey Clave única de la transferencia
     * @return ID de la transferencia, o null si no existe
     */
    public UUID getTransferId(UUID idempotencyKey) {
        String key = buildKey(idempotencyKey);
        Object value = redisTemplate.opsForValue().get(key);
        
        if (value != null) {
            return UUID.fromString(value.toString());
        }
        return null;
    }

    /**
     * Lista todas las entradas de idempotencia (clave -> transferId) almacenadas en Redis.
     * Utilizado principalmente para debug en entorno de desarrollo.
     *
     * @return mapa con la clave completa de Redis y el valor (transferId) en formato String
     */
    public java.util.Map<String, String> listAllIdempotencyEntries() {
        java.util.Set<String> keys = redisTemplate.keys(IDEMPOTENCY_KEY_PREFIX + "*");
        java.util.Map<String, String> result = new java.util.HashMap<>();
        if (keys == null || keys.isEmpty()) {
            return result;
        }
        for (String key : keys) {
            Object val = redisTemplate.opsForValue().get(key);
            result.put(key, val != null ? val.toString() : null);
        }
        return result;
    }

    /**
     * Elimina una clave de idempotencia (solo para casos especiales, como testing).
     */
    public void delete(UUID idempotencyKey) {
        String key = buildKey(idempotencyKey);
        redisTemplate.delete(key);
        log.debug("Clave de idempotencia eliminada: {}", idempotencyKey);
    }

    private String buildKey(UUID idempotencyKey) {
        return IDEMPOTENCY_KEY_PREFIX + idempotencyKey.toString();
    }
}
