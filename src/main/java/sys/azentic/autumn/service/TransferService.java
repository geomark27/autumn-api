package sys.azentic.autumn.service;

import java.util.List;
import java.util.UUID;

import sys.azentic.autumn.dto.request.TransferRequest;
import sys.azentic.autumn.dto.response.TransferResponse;

/**
 * Interfaz de servicio para operaciones de transferencias bancarias.
 * 
 * Define el contrato de negocio para:
 * - Crear transferencias con validaciones, locks y auditoría
 * - Consultar transferencias existentes
 */
public interface TransferService {
    
    /**
     * Crea una nueva transferencia.
     * 
     * Realiza:
     * 1. Validación de idempotencia
     * 2. Obtiene cuentas CON bloqueo pesimista
     * 3. Validaciones de negocio
     * 4. Ejecuta transferencia (débito/crédito)
     * 5. Registra en libro mayor
     * 6. Crea eventos de auditoría
     * 
     * @param request Datos de la transferencia (idempotencyKey, cuentas, monto, etc.)
     * @return Respuesta con estado de la transferencia creada
     * @throws AccountNotFoundException si alguna cuenta no existe
     * @throws InactiveAccountException si alguna cuenta no está activa
     * @throws InsufficientBalanceException si no hay saldo
     * @throws DailyLimitExceededException si se excede el límite diario
     * @throws CurrencyMismatchException si las monedas no coinciden
     */
    TransferResponse createTransfer(TransferRequest request);
    
    /**
     * Consulta una transferencia por su ID.
     * 
     * @param transferId ID de la transferencia
     * @return Datos de la transferencia
     * @throws TransferNotFoundException si no existe
     */
    TransferResponse getTransferById(UUID transferId);
    
    /**
     * Lista todas las transferencias de una cuenta (como origen o destino).
     * 
     * @param accountId ID de la cuenta
     * @return Lista de transferencias asociadas a la cuenta
     * @throws AccountNotFoundException si la cuenta no existe
     */
    List<TransferResponse> getTransfersByAccount(UUID accountId);
}
