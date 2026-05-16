package org.bme.micro_futar.courier.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.services.ShipmentRouteCourierService;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.bme.micro_futar.shared.exceptions.UnauthorizedException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/api/shipment-route-couriers")
@RequiredArgsConstructor
public class ShipmentRouteCourierController {

    private final ShipmentRouteCourierService shipmentRouteCourierService;

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentRouteCourierDTO> findById(@PathVariable Long id) {
        return shipmentRouteCourierService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ShipmentRouteCourierDTO>> findAllForCourierForCurrentDay(Authentication authentication) {
        List<ShipmentRouteCourierDTO> allForCourier = shipmentRouteCourierService.findAllForCourierForCurrentDay(authentication);
        return ResponseEntity.ok(allForCourier);
    }

    @GetMapping("/picked-up-assignments")
    public ResponseEntity<List<ShipmentRouteCourierDTO>> findAllPickedUpAssignmentsForCourierForCurrentDay(Authentication authentication) {
        List<ShipmentRouteCourierDTO> allForCourier = shipmentRouteCourierService.findAllPickedUpAssignmentsForCourierForCurrentDay(authentication);
        return ResponseEntity.ok(allForCourier);
    }

    @PostMapping("/pickup-all-deliveries-for-today")
    public ResponseEntity<Void> pickUpAllDeliveryShipmentsForCurrentDay(Authentication authentication) {
        shipmentRouteCourierService.pickUpAllDeliveryShipmentsForCurrentDay(authentication);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/pickup/{id}")
    public ResponseEntity<Void> pickUpParcel(@PathVariable Long id, Authentication authentication) {
        try {
            shipmentRouteCourierService.pickUpParcel(id, authentication);
        } catch (NoSuchElementException _) {
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedException _) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/fulfill")
    public ResponseEntity<Void> fulfillShipmentRouteAssignment(@PathVariable Long id) {
        return shipmentRouteCourierService.fulfillAssignment(id) ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();
    }

    @PostMapping("/fulfill-all-pickups-for-tocay")
    public ResponseEntity<Void> fulfillAllPickupsForCurrentDay(Authentication authentication) {
        shipmentRouteCourierService.fulfillAllPickupsForCurrentDay(authentication);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/fail")
    public ResponseEntity<Void> failShipmentRouteAssignment(@PathVariable Long id) {
        return shipmentRouteCourierService.failAssignment(id) ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();
    }
}

