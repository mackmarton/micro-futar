package org.bme.micro_futar.logistics.mappers;

import org.bme.micro_futar.logistics.entities.DepoShipment;
import org.bme.micro_futar.shared.dtos.DepoShipmentDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DepoShipmentMapper {
    DepoShipmentDTO toDto(DepoShipment depoShipment);
    DepoShipment fromDto(DepoShipmentDTO depoShipmentDTO);
}

