package org.bme.micro_futar.orders.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.exceptions.NoServiceException;
import org.bme.micro_futar.orders.mappers.OrderMapper;
import org.bme.micro_futar.orders.kafka.OrderProducer;
import org.bme.micro_futar.orders.repositories.OrderRepository;
import org.bme.micro_futar.shared.dtos.OrderDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CountryPriceService countryPriceService;
    private final OrderMapper orderMapper;
    private final OrderProducer orderProducer;

    @Transactional
    public OrderDTO newOrder(OrderDTO orderDTO) {
        orderDTO.setPrice(calculatePrice(orderDTO));
        orderDTO.setConfirmed(false);
        var orderEntity = orderMapper.toEntity(orderDTO);
        var savedEntity = orderRepository.save(orderEntity);
        OrderDTO savedOrderDTO = orderMapper.toDTO(savedEntity);
        orderProducer.sendOrder(savedOrderDTO);
        return savedOrderDTO;
    }

    @Transactional
    public OrderDTO confirm(long id) {
        var orderEntity = orderRepository.findById(id).orElseThrow();
        orderEntity.setConfirmed(true);
        orderEntity.setParcelNumber(UUID.randomUUID().toString());
        var savedEntity = orderRepository.save(orderEntity);
        OrderDTO confirmedOrderDTO = orderMapper.toDTO(savedEntity);
        orderProducer.sendOrder(confirmedOrderDTO);
        return confirmedOrderDTO;
    }

    private double calculatePrice(OrderDTO orderDTO) {
        var countryPrice = countryPriceService.findPriceByCountriesAndSize(orderDTO.getSenderLocationCountryId(), orderDTO.getRecipientLocationCountryId(), orderDTO.getPackageSizeId());
        if (countryPrice.isEmpty()) {
            throw new NoServiceException("There is no service between the origin and destination countries for this size of package!");
        }
        return countryPrice.get().getPrice();
    }

}
