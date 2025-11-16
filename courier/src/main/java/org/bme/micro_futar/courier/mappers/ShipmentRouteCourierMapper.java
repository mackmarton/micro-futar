package org.bme.micro_futar.courier.mappers;

import org.bme.micro_futar.courier.entities.ShipmentRouteCourier;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentRouteCourierMapper {

    ShipmentRouteCourier toEntity(ShipmentRouteCourierDTO dto);

    ShipmentRouteCourierDTO toDTO(ShipmentRouteCourier entity);
}
