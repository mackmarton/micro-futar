package org.bme.micro_futar.logistics.mappers;

import org.bme.micro_futar.logistics.dtos.DepoTransitDTO;
import org.bme.micro_futar.logistics.entities.DepoTransit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DepoTransitMapper {

    DepoTransit toEntity(DepoTransitDTO depoTransitDTO);

    DepoTransitDTO toDTO(DepoTransit depoTransit);
}
