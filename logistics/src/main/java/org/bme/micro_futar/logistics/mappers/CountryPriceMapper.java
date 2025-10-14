package org.bme.micro_futar.logistics.mappers;

import org.bme.micro_futar.logistics.entities.CountryPrice;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CountryPriceMapper {

    CountryPrice toEntity(CountryPriceDTO countryPriceDTO);

    CountryPriceDTO toDTO(CountryPrice countryPrice);
}
