package sys.azentic.autumn.mapper;

import org.mapstruct.Mapper;
import sys.azentic.autumn.domain.entity.Account;
import sys.azentic.autumn.dto.response.AccountResponse;

/**
 * Mapper para convertir entidades Account a DTOs.
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountResponse toResponse(Account account);
}
