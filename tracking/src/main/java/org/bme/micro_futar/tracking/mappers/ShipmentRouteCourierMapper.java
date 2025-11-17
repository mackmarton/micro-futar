package org.bme.micro_futar.tracking.mappers;

import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.bme.micro_futar.tracking.entities.ShipmentRouteCourier;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentRouteCourierMapper {

    ShipmentRouteCourier toEntity(ShipmentRouteCourierDTO dto);

    ShipmentRouteCourierDTO toDTO(ShipmentRouteCourier entity);
}
