package org.bme.micro_futar.orders.mappers;

import org.bme.micro_futar.orders.entities.LocationCity;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationCityMapper {

    LocationCity toEntity(LocationCityDTO locationCityDTO);

    LocationCityDTO toDTO(LocationCity locationCity);
}
