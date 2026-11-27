package org.bme.micro_futar.tracking.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.shared.dtos.*;
import org.bme.micro_futar.tracking.dtos.TrackingDTO;
import org.bme.micro_futar.tracking.dtos.TrackingPartDTO;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingService {
    private final ShipmentService shipmentService;
    private final ShipmentRouteService shipmentRouteService;
    private final DepoService depoService;
    private final LocationCountryService locationCountryService;
    private final LocationCityService locationCityService;

    public Optional<TrackingDTO> trackPackage(String parcelNumber) {
        Optional<ShipmentDTO> optionalShipmentDTO = shipmentService.findByParcelNumber(parcelNumber);
        if (optionalShipmentDTO.isEmpty()) {
            return Optional.empty();
        }
        TrackingDTO trackingDTO = new TrackingDTO();
        Map<Integer, TrackingPartDTO> trackingParts = trackingDTO.getTrackingParts();
        ShipmentDTO shipmentDTO = optionalShipmentDTO.get();
        trackingDTO.setCanceled(shipmentDTO.isCanceled());
        List<ShipmentRouteDTO> fulfilledShipmentRoutesForShipment = shipmentRouteService.findAllFulfilledByShipmentId(shipmentDTO.getId());
        for (int i = 0; i < fulfilledShipmentRoutesForShipment.size(); i++) {
            var shipmentRoute = fulfilledShipmentRoutesForShipment.get(i);
            TrackingPartDTO trackingPartDTO = new TrackingPartDTO();
            trackingPartDTO.setDestination(shipmentRoute.getDestinationAddress() != null);
            trackingPartDTO.setTime(shipmentRoute.getFulfillmentTime().toInstant().atZone(ZoneOffset.UTC));
            trackingPartDTO.setPlace(getPlace(shipmentRoute));
            trackingParts.put(i + 1, trackingPartDTO);
        }
        return Optional.of(trackingDTO);
    }

    /** Null while the master data of the depot has not arrived yet; it is replicated on separate topics. */
    private String getPlace(ShipmentRouteDTO shipmentRoute) {
        if (shipmentRoute.getDestinationAddress() != null) {
            return shipmentRoute.getDestinationAddress();
        }
        return depoService.findById(shipmentRoute.getDestinationDepoId())
                .flatMap(this::getPlace)
                .orElse(null);
    }

    private Optional<String> getPlace(DepoDTO depo) {
        return locationCityService.getCityById(depo.getLocationCityId())
                .flatMap(city -> locationCountryService.getCountryById(depo.getLocationCountryId())
                        .map(country -> city.getName() + ", " + country.getName()));
    }
}
