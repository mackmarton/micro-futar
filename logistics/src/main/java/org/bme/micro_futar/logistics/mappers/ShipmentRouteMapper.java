package org.bme.micro_futar.logistics.mappers;

import org.bme.micro_futar.logistics.entities.ShipmentRoute;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShipmentRouteMapper {

    ShipmentRoute toEntity(ShipmentRouteDTO shipmentRouteDTO);

    ShipmentRouteDTO toDTO(ShipmentRoute shipmentRoute);

    List<ShipmentRoute> toEntityList(List<ShipmentRouteDTO> shipmentRouteDTOs);
}
