package org.bme.micro_futar.tracking.mappers;

import org.bme.micro_futar.shared.dtos.DepoDTO;
import org.bme.micro_futar.tracking.entities.Depo;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DepoMapper {

    Depo toEntity(DepoDTO depoDTO);

    DepoDTO toDTO(Depo depo);

    List<DepoDTO> toDTOList(List<Depo> depoList);
}
