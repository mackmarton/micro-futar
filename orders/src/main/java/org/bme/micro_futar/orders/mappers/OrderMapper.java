package org.bme.micro_futar.orders.mappers;

import dtos.OrderDTO;
import org.bme.micro_futar.orders.entities.OrderEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderEntity toEntity(OrderDTO orderDTO);

    OrderDTO toDTO(OrderEntity orderEntity);
}
