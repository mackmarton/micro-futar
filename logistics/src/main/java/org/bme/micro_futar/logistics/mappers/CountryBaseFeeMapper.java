package org.bme.micro_futar.logistics.mappers;

import org.bme.micro_futar.logistics.dtos.CountryBaseFeeDTO;
import org.bme.micro_futar.logistics.entities.CountryBaseFee;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CountryBaseFeeMapper {

    CountryBaseFee toEntity(CountryBaseFeeDTO countryBaseFeeDTO);

    CountryBaseFeeDTO toDTO(CountryBaseFee countryBaseFee);
}
