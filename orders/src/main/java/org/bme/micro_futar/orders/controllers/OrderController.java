package org.bme.micro_futar.orders.controllers;

import dtos.OrderDTO;
import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.services.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("new")
    public ResponseEntity<OrderDTO> newOrder(@RequestBody OrderDTO orderDTO){
        return ResponseEntity.ok(orderService.newOrder(orderDTO));
    }
}
