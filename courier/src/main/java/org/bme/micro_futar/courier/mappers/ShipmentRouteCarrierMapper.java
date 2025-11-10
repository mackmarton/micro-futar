package org.bme.micro_futar.courier.mappers;

import org.bme.micro_futar.courier.entities.ShipmentRouteCarrier;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCarrierDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentRouteCarrierMapper {

    ShipmentRouteCarrier toEntity(ShipmentRouteCarrierDTO dto);

    ShipmentRouteCarrierDTO toDTO(ShipmentRouteCarrier entity);
}
