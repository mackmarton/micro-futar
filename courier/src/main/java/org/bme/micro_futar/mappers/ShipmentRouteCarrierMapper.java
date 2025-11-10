package org.bme.micro_futar.mappers;

import org.bme.micro_futar.entities.ShipmentRouteCarrier;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCarrierDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentRouteCarrierMapper {

    ShipmentRouteCarrier toEntity(ShipmentRouteCarrierDTO dto);

    ShipmentRouteCarrierDTO toDTO(ShipmentRouteCarrier entity);
}
