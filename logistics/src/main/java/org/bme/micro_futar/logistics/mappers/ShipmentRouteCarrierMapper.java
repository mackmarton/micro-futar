package org.bme.micro_futar.logistics.mappers;

import org.bme.micro_futar.logistics.dtos.ShipmentRouteCarrierDTO;
import org.bme.micro_futar.logistics.entities.ShipmentRouteCarrier;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentRouteCarrierMapper {

    ShipmentRouteCarrier toEntity(ShipmentRouteCarrierDTO dto);

    ShipmentRouteCarrierDTO toDTO(ShipmentRouteCarrier entity);
}
