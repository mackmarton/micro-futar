package org.bme.micro_futar.orders.mappers;

import org.bme.micro_futar.orders.entities.Currency;
import org.bme.micro_futar.shared.dtos.CurrencyDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CurrencyMapper {
    Currency toEntity(CurrencyDTO currencyDTO);
    CurrencyDTO toDTO(Currency currency);
}
