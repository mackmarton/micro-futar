package org.bme.micro_futar.logistics.mappers;

import org.bme.micro_futar.logistics.entities.LocationCountry;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationCountryMapper {

    LocationCountry toEntity(LocationCountryDTO locationCountryDTO);

    LocationCountryDTO toDTO(LocationCountry locationCountry);
}
