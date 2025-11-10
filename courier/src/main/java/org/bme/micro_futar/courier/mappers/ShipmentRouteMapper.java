package org.bme.micro_futar.courier.mappers;

import org.bme.micro_futar.courier.entities.ShipmentRoute;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShipmentRouteMapper {

    ShipmentRoute toEntity(ShipmentRouteDTO shipmentRouteDTO);

    ShipmentRouteDTO toDTO(ShipmentRoute shipmentRoute);

    List<ShipmentRoute> toEntityList(List<ShipmentRouteDTO> shipmentRouteDTOs);
}
