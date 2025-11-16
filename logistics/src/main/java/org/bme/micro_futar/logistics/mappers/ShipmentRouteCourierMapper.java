package org.bme.micro_futar.logistics.mappers;

import org.bme.micro_futar.logistics.entities.ShipmentRouteCourier;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentRouteCourierMapper {

    ShipmentRouteCourier toEntity(ShipmentRouteCourierDTO dto);

    ShipmentRouteCourierDTO toDTO(ShipmentRouteCourier entity);
}
