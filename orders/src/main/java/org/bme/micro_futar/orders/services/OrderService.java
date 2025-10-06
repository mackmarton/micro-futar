package org.bme.micro_futar.orders.services;

import dtos.OrderDTO;
import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.exceptions.NoServiceException;
import org.bme.micro_futar.orders.mappers.OrderMapper;
import org.bme.micro_futar.orders.repositories.OrderRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CountryPriceService countryPriceService;
    private final OrderMapper orderMapper;

    public OrderDTO newOrder(OrderDTO orderDTO) {
        orderDTO.setPrice(calculatePrice(orderDTO));
        orderDTO.setConfirmed(false);
        return save(orderDTO);
    }

    private OrderDTO save(OrderDTO orderDTO){
        var orderEntity = orderMapper.toEntity(orderDTO);
        var savedEntity = orderRepository.save(orderEntity);
        return orderMapper.toDTO(savedEntity);
    }

    private double calculatePrice(OrderDTO orderDTO) {
        var countryPrice = countryPriceService.findPriceByCountriesAndSize(orderDTO.getSenderLocationCountryId(), orderDTO.getRecipientLocationCountryId(), orderDTO.getPackageSizeId());
        if (countryPrice.isEmpty()) {
            throw new NoServiceException("There is no service between the origin and destination countries for this size of package!");
        }
        return countryPrice.get().getPrice();
    }

}
