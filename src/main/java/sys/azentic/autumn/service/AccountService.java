package sys.azentic.autumn.service;

import sys.azentic.autumn.dto.response.AccountResponse;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Interfaz de servicio para operaciones de cuentas bancarias.
 * Define el contrato de negocio para gestión de cuentas.
 */
public interface AccountService {

    /**
     * Obtiene una cuenta por su ID.
     *
     * @param accountId ID único de la cuenta
     * @return Información de la cuenta
     * @throws sys.azentic.autumn.exception.AccountNotFoundException si no existe
     */
    AccountResponse getAccountById(UUID accountId);

    /**
     * Obtiene una cuenta por su número de cuenta.
     *
     * @param accountNumber Número de cuenta único
     * @return Información de la cuenta
     * @throws sys.azentic.autumn.exception.AccountNotFoundException si no existe
     */
    AccountResponse getAccountByNumber(String accountNumber);

    /**
     * Consulta el saldo actual de una cuenta.
     *
     * @param accountId ID de la cuenta
     * @return Saldo disponible
     * @throws sys.azentic.autumn.exception.AccountNotFoundException si no existe
     */
    BigDecimal getBalance(UUID accountId);
}
