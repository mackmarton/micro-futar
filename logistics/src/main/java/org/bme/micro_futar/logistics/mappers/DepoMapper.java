package org.bme.micro_futar.logistics.mappers;

import org.bme.micro_futar.logistics.dtos.DepoDTO;
import org.bme.micro_futar.logistics.entities.Depo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DepoMapper {

    Depo toEntity(DepoDTO depoDTO);

    DepoDTO toDTO(Depo depo);
}
