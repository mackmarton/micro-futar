package org.bme.micro_futar.logistics.mappers;

import org.bme.micro_futar.logistics.entities.LocationRegion;
import org.bme.micro_futar.shared.dtos.LocationRegionDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationRegionMapper {

    LocationRegion toEntity(LocationRegionDTO locationRegionDTO);

    LocationRegionDTO toDTO(LocationRegion locationRegion);
}
