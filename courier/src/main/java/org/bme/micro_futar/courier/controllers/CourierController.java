package org.bme.micro_futar.courier.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.services.CourierService;
import org.bme.micro_futar.shared.dtos.CourierDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/couriers")
@RequiredArgsConstructor
public class CourierController {

    private final CourierService courierService;

    @GetMapping
    public ResponseEntity<List<CourierDTO>> findAll() {
        log.debug("Finding all couriers");
        List<CourierDTO> couriers = courierService.findAll();
        return ResponseEntity.ok(couriers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourierDTO> findById(@PathVariable Long id) {
        log.debug("Finding courier by id: {}", id);
        return courierService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}