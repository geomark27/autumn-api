package sys.azentic.autumn.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sys.azentic.autumn.domain.entity.Transfer;
import sys.azentic.autumn.dto.response.TransferResponse;

/**
 * Mapper para convertir entidades Transfer a DTOs.
 * MapStruct genera la implementación automáticamente en tiempo de compilación.
 */
@Mapper(componentModel = "spring")
public interface TransferMapper {

    @Mapping(source = "sourceAccount.accountNumber", target = "sourceAccountNumber")
    @Mapping(source = "destinationAccount.accountNumber", target = "destinationAccountNumber")
    TransferResponse toResponse(Transfer transfer);
}
