package org.bme.micro_futar.tracking.mappers;

import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.bme.micro_futar.tracking.entities.LocationCity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationCityMapper {

    LocationCity toEntity(LocationCityDTO locationCityDTO);

    LocationCityDTO toDTO(LocationCity locationCity);
}
