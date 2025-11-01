package org.bme.micro_futar.orders.mappers;

import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.orders.entities.Shipment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    Shipment toEntity(ShipmentDTO orderDTO);

    ShipmentDTO toDTO(Shipment shipment);
}
