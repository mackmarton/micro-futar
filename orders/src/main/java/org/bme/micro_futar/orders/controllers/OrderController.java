package org.bme.micro_futar.orders.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.services.OrderService;
import org.bme.micro_futar.shared.dtos.OrderDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("new")
    public ResponseEntity<OrderDTO> newOrder(@RequestBody OrderDTO orderDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.newOrder(orderDTO));
    }

    //ONLY SENDER
    @PostMapping("confirm/{id}")
    public ResponseEntity<OrderDTO>  confirmOrder(@PathVariable long id){
        return ResponseEntity.ok(orderService.confirm(id));
    }
}
