package org.bme.micro_futar.logistics.mappers;

import org.bme.micro_futar.logistics.dtos.CarrierDTO;
import org.bme.micro_futar.logistics.entities.Carrier;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CarrierMapper {

    Carrier toEntity(CarrierDTO carrierDTO);

    CarrierDTO toDTO(Carrier carrier);
}

