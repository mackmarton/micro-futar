package org.bme.micro_futar.mappers;

import org.bme.micro_futar.entities.Courier;
import org.bme.micro_futar.shared.dtos.CourierDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourierMapper {

    Courier toEntity(CourierDTO courierDTO);

    CourierDTO toDTO(Courier courier);
}

