package org.bme.micro_futar.courier.mappers;

import org.bme.micro_futar.courier.entities.ShipmentRouteCourier;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShipmentRouteCourierMapper {

    ShipmentRouteCourier toEntity(ShipmentRouteCourierDTO dto);

    @Mapping(target = "pickedUpForDelivery", ignore = true)
    @Mapping(target = "failed", ignore = true)
    ShipmentRouteCourierDTO toDTO(ShipmentRouteCourier entity);
}
