package org.bme.micro_futar.logistics.mappers;

import org.bme.micro_futar.logistics.entities.OrderEntity;
import org.bme.micro_futar.shared.dtos.OrderDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderEntity toEntity(OrderDTO orderDTO);

    OrderDTO toDTO(OrderEntity orderEntity);
}
