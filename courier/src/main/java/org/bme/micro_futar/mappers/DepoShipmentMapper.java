package org.bme.micro_futar.mappers;

import org.bme.micro_futar.entities.DepoShipment;
import org.bme.micro_futar.shared.dtos.DepoShipmentDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DepoShipmentMapper {
    DepoShipmentDTO toDto(DepoShipment depoShipment);
    DepoShipment fromDto(DepoShipmentDTO depoShipmentDTO);
}

