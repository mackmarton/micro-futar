package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.entities.OrderEntity;
import org.bme.micro_futar.logistics.mappers.OrderMapper;
import org.bme.micro_futar.logistics.repositories.OrderRepository;
import org.bme.micro_futar.shared.dtos.OrderDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final ShipmentRoutePlanner shipmentRoutePlanner;

    @Transactional
    public void processOrder(OrderDTO orderDTO) {
        OrderEntity orderEntity = orderMapper.toEntity(orderDTO);
        orderRepository.save(orderEntity);
        shipmentRoutePlanner.planRouteForOrder(orderDTO);
    }

    public OrderDTO getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(orderMapper::toDTO)
                .orElse(null);
    }
}
