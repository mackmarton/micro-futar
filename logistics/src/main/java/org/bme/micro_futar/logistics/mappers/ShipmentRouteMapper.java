package org.bme.micro_futar.logistics.mappers;

import org.bme.micro_futar.logistics.dtos.ShipmentRouteDTO;
import org.bme.micro_futar.logistics.entities.ShipmentRoute;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentRouteMapper {

    ShipmentRoute toEntity(ShipmentRouteDTO shipmentRouteDTO);

    ShipmentRouteDTO toDTO(ShipmentRoute shipmentRoute);
}
