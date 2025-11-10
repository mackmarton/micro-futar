package org.bme.micro_futar.logistics.mappers;

import org.bme.micro_futar.logistics.entities.Courier;
import org.bme.micro_futar.shared.dtos.CourierDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourierMapper {

    Courier toEntity(CourierDTO courierDTO);

    CourierDTO toDTO(Courier courier);
}

