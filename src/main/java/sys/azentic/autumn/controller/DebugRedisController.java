package sys.azentic.autumn.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sys.azentic.autumn.service.IdempotencyService;

import java.util.Map;

/**
 * Controlador de debug para exponer claves de Redis relacionadas con idempotencia.
 * Habilitado solo en perfil `dev` para evitar exponer datos en producción.
 */
@RestController
@RequestMapping("/api/v1/debug/redis")
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class DebugRedisController {

    private final IdempotencyService idempotencyService;

    /**
     * Retorna todas las entradas con prefijo `idempotency:transfer:` en Redis.
     */
    @GetMapping("/idempotency")
    public ResponseEntity<Map<String, String>> listIdempotencyEntries() {
        log.debug("Listando entradas de idempotencia en Redis (dev-only)");
        Map<String, String> entries = idempotencyService.listAllIdempotencyEntries();
        return ResponseEntity.ok(entries);
    }
}
