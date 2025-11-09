package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.exceptions.NoRouteFoundException;
import org.bme.micro_futar.shared.dtos.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipmentRoutePlanner {

    private final DepoService depoService;
    private final LocationCityService locationCityService;
    private final LocationCountryService locationCountryService;
    private final OpenRouteServiceFetcher openRouteServiceFetcher;
    private final DepoRouteFinderService depoRouteFinderService;
    private final ShipmentRouteService shipmentRouteService;

    public void planRouteForShipment(ShipmentDTO shipment) {
        DepoDTO originDepo = findClosestDepo(
                shipment.getSenderLocationCountryId(),
                shipment.getSenderLocationCityId(),
                shipment.getSenderZip(),
                shipment.getSenderAddress()
        );

        DepoDTO destinationDepo = findClosestDepo(
                shipment.getRecipientLocationCountryId(),
                shipment.getRecipientLocationCityId(),
                shipment.getRecipientZip(),
                shipment.getRecipientAddress()
        );

        List<Long> cheapestRoute = findCheapestDepoRoute(
                originDepo.getId(),
                destinationDepo.getId(),
                shipment.getPackageSizeId()
        );

        List<ShipmentRouteDTO> shipmentRoutes = buildShipmentRoutes(
                shipment.getId(),
                originDepo,
                destinationDepo,
                cheapestRoute,
                buildAddress(shipment.getSenderLocationCountryId(), shipment.getSenderLocationCityId(),
                        shipment.getSenderZip(), shipment.getSenderAddress()),
                buildAddress(shipment.getRecipientLocationCountryId(), shipment.getRecipientLocationCityId(),
                        shipment.getRecipientZip(), shipment.getRecipientAddress())
        );

        shipmentRouteService.saveAll(shipmentRoutes);
    }

    private DepoDTO findClosestDepo(Long countryId, Long cityId, String zip, String address) {
        LocationCountryDTO country = locationCountryService.getCountryById(countryId)
                .orElseThrow(() -> new IllegalArgumentException("Country not found with id: " + countryId));
        LocationCityDTO city = locationCityService.getCityById(cityId)
                .orElseThrow(() -> new IllegalArgumentException("City not found with id: " + cityId));

        String fullAddress = buildAddress(country, city, zip, address);
        double[] coordinates = openRouteServiceFetcher.geocode(fullAddress);

        List<DepoDTO> deposInCountry = depoService.getAllDeposByCountryId(countryId);
        if (deposInCountry.isEmpty()) {
            throw new IllegalStateException("No depos found in country with id: " + countryId);
        }

        List<double[]> depoCoordinates = extractDepoCoordinates(deposInCountry);
        int closestDepoIndex = openRouteServiceFetcher.getClosestIndexByDuration(coordinates, depoCoordinates);

        return deposInCountry.get(closestDepoIndex);
    }

    private String buildAddress(Long countryId, Long cityId, String zip, String address) {
        LocationCountryDTO country = locationCountryService.getCountryById(countryId)
                .orElseThrow(() -> new IllegalArgumentException("Country not found with id: " + countryId));
        LocationCityDTO city = locationCityService.getCityById(cityId)
                .orElseThrow(() -> new IllegalArgumentException("City not found with id: " + cityId));
        return buildAddress(country, city, zip, address);
    }

    private String buildAddress(LocationCountryDTO country, LocationCityDTO city, String zip, String address) {
        return String.format("%s %s %s %s", country.getName(), zip, city.getName(), address);
    }

    private List<double[]> extractDepoCoordinates(List<DepoDTO> depos) {
        List<double[]> coordinates = new ArrayList<>();
        for (DepoDTO depo : depos) {
            coordinates.add(new double[]{depo.getLongitude(), depo.getLatitude()});
        }
        return coordinates;
    }

    private List<Long> findCheapestDepoRoute(Long originDepoId, Long destinationDepoId, Long packageSizeId) {
        List<Long> route = depoRouteFinderService.findCheapestRoute(
                originDepoId,
                destinationDepoId,
                packageSizeId
        );

        if (route.isEmpty()) {
            throw new NoRouteFoundException(
                    String.format("No route found between origin depo %d and destination depo %d", originDepoId, destinationDepoId));
        }

        return route;
    }

    private List<ShipmentRouteDTO> buildShipmentRoutes(Long shipmentId, DepoDTO originDepo, DepoDTO destinationDepo, List<Long> depoRoute,
                                                       String originAddress, String destinationAddress) {
        List<ShipmentRouteDTO> shipmentRoutes = new ArrayList<>();
        int routePartNumber = 0;

        shipmentRoutes.add(ShipmentRouteDTO.builder()
                .shipmentId(shipmentId)
                .routePartNumber(routePartNumber++)
                .originAddress(originAddress)
                .destinationDepoId(originDepo.getId())
                .build());

        for (int i = 1; i < depoRoute.size(); i++) {
            shipmentRoutes.add(ShipmentRouteDTO.builder()
                    .shipmentId(shipmentId)
                    .routePartNumber(routePartNumber++)
                    .originDepoId(depoRoute.get(i - 1))
                    .destinationDepoId(depoRoute.get(i))
                    .build());
        }

        shipmentRoutes.add(ShipmentRouteDTO.builder()
                .shipmentId(shipmentId)
                .routePartNumber(routePartNumber)
                .originDepoId(destinationDepo.getId())
                .destinationAddress(destinationAddress)
                .build());

        return shipmentRoutes;
    }
}
