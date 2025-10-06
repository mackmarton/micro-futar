package org.bme.micro_futar.orders.mappers;

import org.bme.micro_futar.orders.entities.PackageSize;
import org.bme.micro_futar.shared.dtos.PackageSizeDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PackageSizeMapper {

    PackageSize toEntity(PackageSizeDTO packageSizeDTO);

    PackageSizeDTO toDTO(PackageSize packageSize);
}
