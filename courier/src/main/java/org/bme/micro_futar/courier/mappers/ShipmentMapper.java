package org.bme.micro_futar.courier.mappers;

import org.bme.micro_futar.courier.entities.Shipment;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    Shipment toEntity(ShipmentDTO shipmentDTO);

    ShipmentDTO toDTO(Shipment shipment);
}
