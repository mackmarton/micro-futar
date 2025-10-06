package org.bme.micro_futar.orders.mappers;

import org.bme.micro_futar.orders.entities.OrderEntity;
import org.bme.micro_futar.shared.dtos.OrderDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderEntity toEntity(OrderDTO orderDTO);

    OrderDTO toDTO(OrderEntity orderEntity);
}
