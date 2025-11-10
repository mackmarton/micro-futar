package org.bme.micro_futar.mappers;

import org.bme.micro_futar.entities.PackageSize;
import org.bme.micro_futar.shared.dtos.PackageSizeDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PackageSizeMapper {

    PackageSize toEntity(PackageSizeDTO packageSizeDTO);

    PackageSizeDTO toDTO(PackageSize packageSize);
}
