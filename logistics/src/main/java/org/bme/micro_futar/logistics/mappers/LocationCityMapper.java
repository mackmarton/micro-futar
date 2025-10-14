package org.bme.micro_futar.logistics.mappers;

import org.bme.micro_futar.logistics.entities.LocationCity;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationCityMapper {

    LocationCity toEntity(LocationCityDTO locationCityDTO);

    LocationCityDTO toDTO(LocationCity locationCity);
}
