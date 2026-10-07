package org.bme.micro_futar.logistics.mappers;

import org.bme.micro_futar.logistics.entities.Currency;
import org.bme.micro_futar.shared.dtos.CurrencyDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CurrencyMapper {
    Currency toEntity(CurrencyDTO currencyDTO);
    CurrencyDTO toDTO(Currency currency);
}
