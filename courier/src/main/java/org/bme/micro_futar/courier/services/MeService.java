package org.bme.micro_futar.courier.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.courier.config.auth.VerifiedEmails;
import org.bme.micro_futar.courier.dtos.AssignmentType;
import org.bme.micro_futar.courier.dtos.MyAssignmentDTO;
import org.bme.micro_futar.courier.dtos.MyProfileDTO;
import org.bme.micro_futar.courier.dtos.MyVehicleDTO;
import org.bme.micro_futar.shared.dtos.CourierDTO;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Everything here is scoped to the courier identified by the caller's verified email. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeService {

    private final CourierService courierService;
    private final VehicleService vehicleService;
    private final ShipmentService shipmentService;
    private final ShipmentRouteService shipmentRouteService;
    private final ShipmentRouteCourierService shipmentRouteCourierService;

    public Optional<MyProfileDTO> findProfile(Authentication authentication) {
        return findCourier(authentication).map(this::toProfile);
    }

    /** Today's assignments of the courier, without the canceled ones. Empty optional: the caller is not a courier. */
    public Optional<List<MyAssignmentDTO>> findAssignments(Authentication authentication) {
        return findCourier(authentication).map(courier -> shipmentRouteCourierService
                .findAllForCourierForCurrentDay(authentication).stream()
                .map(this::toMyAssignment)
                .toList());
    }

    /** Empty both for unknown ids and for assignments of others, so the two cannot be told apart. */
    public Optional<MyAssignmentDTO> findAssignment(Long assignmentId, Authentication authentication) {
        return findCourier(authentication).flatMap(courier -> shipmentRouteCourierService
                .findAllForCourierForCurrentDay(authentication).stream()
                .filter(assignment -> Objects.equals(assignment.getId(), assignmentId))
                .findFirst()
                .map(this::toMyAssignment));
    }

    private Optional<CourierDTO> findCourier(Authentication authentication) {
        return courierService.findByEmail(VerifiedEmails.require(authentication));
    }

    private MyProfileDTO toProfile(CourierDTO courier) {
        MyVehicleDTO vehicle = Optional.ofNullable(courier.getVehicleId())
                .flatMap(vehicleService::findById)
                .map(v -> MyVehicleDTO.builder()
                        .registrationNumber(v.getRegistrationNumber())
                        .maximumPackableVolume(v.getMaximumPackableVolume())
                        .build())
                .orElse(null);
        return MyProfileDTO.builder()
                .id(courier.getId())
                .name(courier.getName())
                .email(courier.getEmail())
                .telephone(courier.getTelephone())
                .qualifiedFor(courier.getQualifiedFor())
                .courierType(courier.getCourierType())
                .depoId(courier.getDepoId())
                .vehicle(vehicle)
                .build();
    }

    private MyAssignmentDTO toMyAssignment(ShipmentRouteCourierDTO assignment) {
        var result = MyAssignmentDTO.builder()
                .assignmentId(assignment.getId())
                .shipmentRouteId(assignment.getShipmentRouteId())
                .pickedUpForDelivery(Boolean.TRUE.equals(assignment.getPickedUpForDelivery()))
                .failed(Boolean.TRUE.equals(assignment.getFailed()));

        Optional<ShipmentRouteDTO> route = Optional.ofNullable(assignment.getShipmentRouteId())
                .flatMap(shipmentRouteService::findById);
        if (route.isEmpty()) {
            return result.build();
        }
        result.fulfillmentTime(route.get().getFulfillmentTime());

        Optional<ShipmentDTO> shipment = Optional.ofNullable(route.get().getShipmentId())
                .flatMap(shipmentService::findById);
        shipment.ifPresent(s -> result.parcelNumber(s.getParcelNumber()).packageSizeId(s.getPackageSizeId()));

        if (hasText(route.get().getOriginAddress())) {
            result.assignmentType(AssignmentType.PICKUP).address(route.get().getOriginAddress());
            shipment.ifPresent(s -> result
                    .contactName(s.getSenderName())
                    .contactPhone(s.getSenderPhone())
                    .latitude(s.getSenderLatitude())
                    .longitude(s.getSenderLongitude()));
        } else if (hasText(route.get().getDestinationAddress())) {
            result.assignmentType(AssignmentType.DELIVERY).address(route.get().getDestinationAddress());
            shipment.ifPresent(s -> result
                    .contactName(s.getRecipientName())
                    .contactPhone(s.getRecipientPhone())
                    .latitude(s.getRecipientLatitude())
                    .longitude(s.getRecipientLongitude()));
        }
        return result.build();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
