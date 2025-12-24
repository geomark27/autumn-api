package sys.azentic.autumn.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sys.azentic.autumn.dto.response.AccountResponse;

import java.util.UUID;

/**
 * Controlador REST para operaciones de cuentas.
 * 
 * Endpoints:
 * - GET /api/v1/accounts/{id}              - Consultar cuenta por ID
 * - GET /api/v1/accounts/number/{number}   - Consultar cuenta por número
 * - GET /api/v1/accounts/{id}/balance      - Consultar saldo actual
 */
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    // TODO: Inyectar AccountService cuando se implemente
    // private final AccountService accountService;

    /**
     * Consulta una cuenta por su ID.
     * 
     * @param id ID de la cuenta
     * @return Datos de la cuenta
     */
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable UUID id) {
        log.info("Consultando cuenta con ID: {}", id);
        
        // TODO: Implementar lógica
        // AccountResponse response = accountService.getAccountById(id);
        
        return ResponseEntity.ok().build();
    }

    /**
     * Consulta una cuenta por su número.
     * 
     * @param accountNumber Número de cuenta
     * @return Datos de la cuenta
     */
    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccountByNumber(@PathVariable String accountNumber) {
        log.info("Consultando cuenta con número: {}", accountNumber);
        
        // TODO: Implementar lógica
        // AccountResponse response = accountService.getAccountByNumber(accountNumber);
        
        return ResponseEntity.ok().build();
    }

    /**
     * Consulta el saldo actual de una cuenta.
     * 
     * @param id ID de la cuenta
     * @return Saldo disponible
     */
    @GetMapping("/{id}/balance")
    public ResponseEntity<?> getAccountBalance(@PathVariable UUID id) {
        log.info("Consultando saldo de cuenta ID: {}", id);
        
        // TODO: Implementar lógica
        // BigDecimal balance = accountService.getBalance(id);
        
        return ResponseEntity.ok().build();
    }
}
