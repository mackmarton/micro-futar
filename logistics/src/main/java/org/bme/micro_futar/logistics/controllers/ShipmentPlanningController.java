package org.bme.micro_futar.logistics.controllers;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.services.ShipmentToCarrierPlanningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/planning")
@RequiredArgsConstructor
public class ShipmentPlanningController {

    private final ShipmentToCarrierPlanningService planningService;

    @PostMapping("/depo/{depoId}/assign-shipments")
    public ResponseEntity<Map<String, Object>> planShipmentsForDepo(@PathVariable Long depoId) {
        Map<String, Object> result = planningService.planShipmentsForDepo(depoId);
        return ResponseEntity.ok(result);
    }
}
