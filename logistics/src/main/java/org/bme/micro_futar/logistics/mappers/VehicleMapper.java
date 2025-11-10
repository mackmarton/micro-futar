package org.bme.micro_futar.logistics.mappers;

import org.bme.micro_futar.logistics.entities.Vehicle;
import org.bme.micro_futar.shared.dtos.VehicleDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    Vehicle toEntity(VehicleDTO vehicleDTO);

    VehicleDTO toDTO(Vehicle vehicle);
}

