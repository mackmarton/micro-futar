package org.bme.micro_futar.orders.mappers;

import org.bme.micro_futar.orders.entities.LocationCountry;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationCountryMapper {

    LocationCountry toEntity(LocationCountryDTO locationCountryDTO);

    LocationCountryDTO toDTO(LocationCountry locationCountry);
}
