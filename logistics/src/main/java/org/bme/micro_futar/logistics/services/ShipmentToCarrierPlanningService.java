package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.shared.dtos.*;
import org.bme.micro_futar.shared.enums.CourierType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentToCarrierPlanningService {

    private final CourierService courierService;
    private final ShipmentRouteService shipmentRouteService;
    private final ShipmentRouteCarrierService shipmentRouteCarrierService;
    private final VehicleService vehicleService;
    private final ShipmentService shipmentService;
    private final PackageSizeService packageSizeService;

    private static final int MAX_PACKAGES_PER_DAY = 150;

    @Transactional
    public Map<String, Object> planShipmentsForDepo(Long depoId) {
        Date today = Date.valueOf(LocalDate.now());

        List<CourierDTO> deliveryCarriers = courierService.getCouriersByDepoIdAndType(depoId, CourierType.DELIVERY);

        if (deliveryCarriers.isEmpty()) {
            log.warn("No delivery carriers found for depo {}", depoId);
            return createResult(0, 0, 0, "No delivery carriers available");
        }

        List<ShipmentRouteDTO> deliveryRoutes = shipmentRouteService.getDeliveryRoutes(depoId);

        List<ShipmentRouteDTO> pickupRoutes = shipmentRouteService.getPickupRoutes(depoId);

        List<ShipmentRouteDTO> allRoutes = new ArrayList<>();
        allRoutes.addAll(deliveryRoutes);
        allRoutes.addAll(pickupRoutes);

        if (allRoutes.isEmpty()) {
            log.info("No pending shipment routes found for depo {}", depoId);
            return createResult(0, 0, deliveryCarriers.size(), "No pending shipments");
        }

        Map<Long, CarrierCapacity> carrierCapacities = new HashMap<>();
        for (CourierDTO carrier : deliveryCarriers) {
            CarrierCapacity capacity = calculateCarrierCapacity(carrier, today);
            if (capacity != null) {
                carrierCapacities.put(carrier.getId(), capacity);
            }
        }

        if (carrierCapacities.isEmpty()) {
            log.warn("No carriers with available capacity for depo {}", depoId);
            return createResult(0, allRoutes.size(), deliveryCarriers.size(), "No carriers with available capacity");
        }

        // Assign shipments to carriers
        int assignedCount = assignShipmentsToCarriers(allRoutes, carrierCapacities, today);

        return createResult(assignedCount, allRoutes.size(), deliveryCarriers.size(), "Planning completed successfully");
    }

    private CarrierCapacity calculateCarrierCapacity(CourierDTO carrier, Date date) {
        if (carrier.getVehicleId() == null) {
            log.warn("Carrier {} has no vehicle assigned", carrier.getId());
            return null;
        }

        Optional<VehicleDTO> vehicleOpt = vehicleService.getVehicleById(carrier.getVehicleId());
        if (vehicleOpt.isEmpty()) {
            log.warn("Vehicle {} not found for carrier {}", carrier.getVehicleId(), carrier.getId());
            return null;
        }

        VehicleDTO vehicle = vehicleOpt.get();
        if (vehicle.getMaximumPackableVolume() == null) {
            log.warn("Vehicle {} has no maximum packable volume defined", vehicle.getId());
            return null;
        }

        long alreadyAssignedCount = shipmentRouteCarrierService.countByCarrierIdAndDate(carrier.getId(), date);
        List<ShipmentRouteCarrierDTO> alreadyAssigned = shipmentRouteCarrierService.findByCarrierIdAndDateAssignedFor(carrier.getId(), date);

        CarrierCapacity capacity = new CarrierCapacity();
        capacity.carrierId = carrier.getId();
        capacity.maxVolume = vehicle.getMaximumPackableVolume();
        capacity.remainingVolume = vehicle.getMaximumPackableVolume();

        // Subtract volume of already assigned packages
        for (ShipmentRouteCarrierDTO assignedShipment : alreadyAssigned) {
            Optional<ShipmentRouteDTO> routeOpt = shipmentRouteService.getShipmentRouteById(assignedShipment.getShipmentRouteId());
            if (routeOpt.isPresent()) {
                double volume = getShipmentVolume(routeOpt.get().getShipmentId());
                capacity.remainingVolume -= volume;
            }
        }

        capacity.maxPackages = MAX_PACKAGES_PER_DAY;
        capacity.remainingPackages = (int) (MAX_PACKAGES_PER_DAY - alreadyAssignedCount);

        return capacity;
    }

    private int assignShipmentsToCarriers(List<ShipmentRouteDTO> routes, Map<Long, CarrierCapacity> carrierCapacities, Date date) {
        int assignedCount = 0;

        routes.sort((r1, r2) -> {
            double vol1 = getShipmentVolume(r1.getShipmentId());
            double vol2 = getShipmentVolume(r2.getShipmentId());
            return Double.compare(vol2, vol1);
        });

        for (ShipmentRouteDTO route : routes) {
            double volume = getShipmentVolume(route.getShipmentId());

            CarrierCapacity selectedCarrier = findBestCarrier(carrierCapacities, volume);

            if (selectedCarrier != null) {
                ShipmentRouteCarrierDTO assignment = ShipmentRouteCarrierDTO.builder()
                        .carrierId(selectedCarrier.carrierId)
                        .shipmentRouteId(route.getId())
                        .dateAssignedFor(date)
                        .failed(false)
                        .build();

                shipmentRouteCarrierService.save(assignment);

                selectedCarrier.remainingVolume -= volume;
                selectedCarrier.remainingPackages--;

                assignedCount++;
                log.debug("Assigned shipment route {} to carrier {}", route.getId(), selectedCarrier.carrierId);
            } else {
                log.warn("Could not assign shipment route {} - no carrier with sufficient capacity", route.getId());
            }
        }

        return assignedCount;
    }

    private CarrierCapacity findBestCarrier(Map<Long, CarrierCapacity> carrierCapacities, double requiredVolume) {
        CarrierCapacity bestCarrier = null;
        double smallestRemainingVolume = Double.MAX_VALUE;

        for (CarrierCapacity capacity : carrierCapacities.values()) {
            if ((capacity.remainingPackages > 0 && capacity.remainingVolume >= requiredVolume)
                    && (capacity.remainingVolume < smallestRemainingVolume)) {
                bestCarrier = capacity;
                smallestRemainingVolume = capacity.remainingVolume;
            }
        }

        return bestCarrier;
    }

    private double getShipmentVolume(Long shipmentId) {
        ShipmentDTO shipment = shipmentService.getShipmentById(shipmentId);
        if (shipment == null) {
            log.warn("Shipment {} not found", shipmentId);
            return 0.0;
        }

        if (shipment.getPackageSizeId() == null) {
            log.warn("Shipment {} has no package size", shipmentId);
            return 0.0;
        }

        Optional<PackageSizeDTO> packageSizeOpt = packageSizeService.getPackageSizeById(shipment.getPackageSizeId());
        if (packageSizeOpt.isEmpty()) {
            log.warn("Package size {} not found", shipment.getPackageSizeId());
            return 0.0;
        }

        PackageSizeDTO packageSize = packageSizeOpt.get();
        if (packageSize.getMaxLength() == null) {
            log.warn("Package size {} has no max length defined", packageSize.getId());
            return 0.0;
        }

        return packageSize.getMaxLength() * packageSize.getMaxLength() * packageSize.getMaxLength();
    }

    private Map<String, Object> createResult(int assignedCount, int totalRoutes, int totalCarriers, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("assignedCount", assignedCount);
        result.put("totalPendingRoutes", totalRoutes);
        result.put("totalDeliveryCarriers", totalCarriers);
        result.put("message", message);
        return result;
    }

    private static class CarrierCapacity {
        Long carrierId;
        double maxVolume;
        double remainingVolume;
        int maxPackages;
        int remainingPackages;
    }
}
