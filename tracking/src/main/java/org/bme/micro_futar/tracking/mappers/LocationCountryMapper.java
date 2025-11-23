package org.bme.micro_futar.tracking.mappers;

import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.bme.micro_futar.tracking.entities.LocationCountry;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationCountryMapper {

    LocationCountry toEntity(LocationCountryDTO locationCountryDTO);

    LocationCountryDTO toDTO(LocationCountry locationCountry);
}
