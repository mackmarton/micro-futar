package org.bme.micro_futar.logistics.services.planners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.logistics.services.*;
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
public class ShipmentToCourierPlanningService {

    private final CourierService courierService;
    private final ShipmentRouteService shipmentRouteService;
    private final ShipmentRouteCourierService shipmentRouteCourierService;
    private final VehicleService vehicleService;
    private final ShipmentService shipmentService;
    private final PackageSizeService packageSizeService;

    private static final int MAX_PACKAGES_PER_DAY = 150;

    @Transactional
    public Map<String, Object> planShipmentsForDepo(Long depoId) {
        Date today = Date.valueOf(LocalDate.now());
        List<CourierDTO> deliveryCouriers = courierService.getCouriersByDepoIdAndType(depoId, CourierType.DELIVERY);

        if (deliveryCouriers.isEmpty()) {
            log.warn("No delivery couriers found for depo {}", depoId);
            return createResult(0, 0, 0, "No delivery couriers available");
        }

        List<ShipmentRouteDTO> deliveryRoutes = shipmentRouteService.getDeliveryRoutes(depoId);

        List<ShipmentRouteDTO> pickupRoutes = shipmentRouteService.getPickupRoutes(depoId);

        List<ShipmentRouteDTO> allRoutes = new ArrayList<>();
        allRoutes.addAll(deliveryRoutes);
        allRoutes.addAll(pickupRoutes);

        if (allRoutes.isEmpty()) {
            log.info("No pending shipment routes found for depo {}", depoId);
            return createResult(0, 0, deliveryCouriers.size(), "No pending shipments");
        }

        Map<Long, CourierCapacity> courierCapacities = new HashMap<>();
        for (CourierDTO courier : deliveryCouriers) {
            CourierCapacity capacity = calculateCourierCapacity(courier, today);
            if (capacity != null) {
                courierCapacities.put(courier.getId(), capacity);
            }
        }

        if (courierCapacities.isEmpty()) {
            log.warn("No couriers with available capacity for depo {}", depoId);
            return createResult(0, allRoutes.size(), deliveryCouriers.size(), "No couriers with available capacity");
        }

        // Assign shipments to couriers
        int assignedCount = assignShipmentsToCouriers(allRoutes, courierCapacities, today);

        return createResult(assignedCount, allRoutes.size(), deliveryCouriers.size(), "Planning completed successfully");
    }

    public Map<String, Object> planCrossDepoShipmentsForDepo(Long depoId) {
        Date today = Date.valueOf(LocalDate.now());
        List<CourierDTO> couriers = courierService.getCouriersByDepoIdAndType(depoId, CourierType.CROSS_DEPO);

        if (couriers.isEmpty()) {
            log.warn("No couriers found for depo {}", depoId);
            return createResult(0, 0, 0, "No delivery couriers available");
        }

        List<ShipmentRouteDTO> crossDepoRoutes = shipmentRouteService.getCrossDepoRoutes(depoId);

        if (crossDepoRoutes.isEmpty()) {
            log.info("No pending cross depo shipment routes found for depo {}", depoId);
            return createResult(0, 0, couriers.size(), "No pending shipments");
        }

        Map<Long, CourierCapacity> courierCapacities = new HashMap<>();
        for (CourierDTO courier : couriers) {
            CourierCapacity capacity = calculateCourierCapacity(courier, today);
            if (capacity != null) {
                courierCapacities.put(courier.getId(), capacity);
            }
        }

        if (courierCapacities.isEmpty()) {
            log.warn("No cross depo couriers with available capacity for depo {}", depoId);
            return createResult(0, crossDepoRoutes.size(), couriers.size(), "No couriers with available capacity");
        }

        int assignedCount = assignShipmentsToCouriers(crossDepoRoutes, courierCapacities, today);

        return createResult(assignedCount, crossDepoRoutes.size(), couriers.size(), "Planning completed successfully");
    }

    private CourierCapacity calculateCourierCapacity(CourierDTO courier, Date date) {
        if (courier.getVehicleId() == null) {
            log.warn("Courier {} has no vehicle assigned", courier.getId());
            return null;
        }

        Optional<VehicleDTO> vehicleOpt = vehicleService.getVehicleById(courier.getVehicleId());
        if (vehicleOpt.isEmpty()) {
            log.warn("Vehicle {} not found for courier {}", courier.getVehicleId(), courier.getId());
            return null;
        }

        VehicleDTO vehicle = vehicleOpt.get();
        if (vehicle.getMaximumPackableVolume() == null) {
            log.warn("Vehicle {} has no maximum packable volume defined", vehicle.getId());
            return null;
        }

        long alreadyAssignedCount = shipmentRouteCourierService.countByCourierIdAndDate(courier.getId(), date);
        List<ShipmentRouteCourierDTO> alreadyAssigned = shipmentRouteCourierService.findByCourierIdAndDateAssignedFor(courier.getId(), date);

        CourierCapacity capacity = new CourierCapacity();
        capacity.courierId = courier.getId();
        capacity.maxVolume = vehicle.getMaximumPackableVolume();
        capacity.remainingVolume = vehicle.getMaximumPackableVolume();

        // Subtract volume of already assigned packages
        for (ShipmentRouteCourierDTO assignedShipment : alreadyAssigned) {
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

    private int assignShipmentsToCouriers(List<ShipmentRouteDTO> routes, Map<Long, CourierCapacity> courierCapacities, Date date) {
        int assignedCount = 0;

        if (routes.size() > 1) {
            routes.sort((r1, r2) -> {
                double vol1 = getShipmentVolume(r1.getShipmentId());
                double vol2 = getShipmentVolume(r2.getShipmentId());
                return Double.compare(vol2, vol1);
            });
        }

        for (ShipmentRouteDTO route : routes) {
            double volume = getShipmentVolume(route.getShipmentId());

            CourierCapacity selectedCourier = findBestCourier(courierCapacities, volume);

            if (selectedCourier != null) {
                ShipmentRouteCourierDTO assignment = ShipmentRouteCourierDTO.builder()
                        .courierId(selectedCourier.courierId)
                        .shipmentRouteId(route.getId())
                        .dateAssignedFor(date)
                        .failed(false)
                        .build();

                shipmentRouteCourierService.save(assignment);

                selectedCourier.remainingVolume -= volume;
                selectedCourier.remainingPackages--;

                assignedCount++;
                log.debug("Assigned shipment route {} to courier {}", route.getId(), selectedCourier.courierId);
            } else {
                log.warn("Could not assign shipment route {} - no courier with sufficient capacity", route.getId());
            }
        }

        return assignedCount;
    }

    private CourierCapacity findBestCourier(Map<Long, CourierCapacity> courierCapacities, double requiredVolume) {
        CourierCapacity bestCourier = null;
        double smallestRemainingVolume = Double.MAX_VALUE;

        for (CourierCapacity capacity : courierCapacities.values()) {
            if ((capacity.remainingPackages > 0 && capacity.remainingVolume >= requiredVolume)
                    && (capacity.remainingVolume < smallestRemainingVolume)) {
                bestCourier = capacity;
                smallestRemainingVolume = capacity.remainingVolume;
            }
        }

        return bestCourier;
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

    private Map<String, Object> createResult(int assignedCount, int totalRoutes, int totalCouriers, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("assignedCount", assignedCount);
        result.put("totalPendingRoutes", totalRoutes);
        result.put("totalDeliveryCouriers", totalCouriers);
        result.put("message", message);
        return result;
    }

    private static class CourierCapacity {
        Long courierId;
        double maxVolume;
        double remainingVolume;
        int maxPackages;
        int remainingPackages;
    }
}
