package org.bme.micro_futar.mappers;

import org.bme.micro_futar.entities.Shipment;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    Shipment toEntity(ShipmentDTO shipmentDTO);

    ShipmentDTO toDTO(Shipment shipment);
}
