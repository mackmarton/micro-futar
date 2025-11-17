package org.bme.micro_futar.tracking.mappers;

import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.tracking.entities.Shipment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    Shipment toEntity(ShipmentDTO shipmentDTO);

    ShipmentDTO toDTO(Shipment shipment);
}
