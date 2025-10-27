package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.dtos.DepoDTO;
import org.bme.micro_futar.logistics.dtos.ShipmentRouteDTO;
import org.bme.micro_futar.logistics.exceptions.NoRouteFoundException;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.bme.micro_futar.shared.dtos.OrderDTO;
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

    public void planRouteForOrder(OrderDTO order) {
        LocationCountryDTO senderCountry = locationCountryService.getCountryById(order.getSenderLocationCountryId()).orElseThrow();
        LocationCityDTO senderCity = locationCityService.getCityById(order.getSenderLocationCityId()).orElseThrow();
        String originAddress = senderCountry.getName() + " " + order.senderZip + " " + senderCity.getName() + " " + order.getSenderAddress();
        double[] originCoordinates = openRouteServiceFetcher.geocode(originAddress);

        List<DepoDTO> deposInOriginCountry = depoService.getAllDeposByCountryId(senderCountry.getId());
        List<double[]> depoCoordinatesInOriginCountry = new ArrayList<>();
        for (var depo : deposInOriginCountry) {
            depoCoordinatesInOriginCountry.add(new double[]{depo.getLongitude(), depo.getLatitude()});
        }
        DepoDTO originDepo = deposInOriginCountry.get(openRouteServiceFetcher.getClosestIndexByDuration(originCoordinates, depoCoordinatesInOriginCountry));

        LocationCountryDTO destinationCountry = locationCountryService.getCountryById(order.getRecipientLocationCountryId()).orElseThrow();
        LocationCityDTO destinationCity = locationCityService.getCityById(order.getRecipientLocationCityId()).orElseThrow();
        String destinationAddress = destinationCountry.getName() + " " + order.recipientZip + " " + destinationCity.getName() + " " + order.getRecipientAddress();
        double[] destinationCoordinates = openRouteServiceFetcher.geocode(destinationAddress);

        List<DepoDTO> deposInDestinationCountry = depoService.getAllDeposByCountryId(destinationCountry.getId());
        List<double[]> depoCoordinatesInDestinationCountry = new ArrayList<>();
        for (var depo : deposInDestinationCountry) {
            depoCoordinatesInDestinationCountry.add(new double[]{depo.getLongitude(), depo.getLatitude()});
        }
        DepoDTO destinationDepo = deposInDestinationCountry.get(openRouteServiceFetcher.getClosestIndexByDuration(destinationCoordinates, depoCoordinatesInDestinationCountry));

        List<Long> cheapestRoute = depoRouteFinderService.findCheapestRoute(
                originDepo.getId(),
                destinationDepo.getId(),
                order.getPackageSizeId()
        );

        if (cheapestRoute.isEmpty()) {
            throw new NoRouteFoundException("No route found between origin depo " + originDepo.getId() + " and destination depo " + destinationDepo.getId());
        }

        int routePartNumber = 0;
        List<ShipmentRouteDTO> shipmentRouteList = new ArrayList<>();
        shipmentRouteList.add(ShipmentRouteDTO.builder()
                .orderId(order.getId())
                .routePartNumber(routePartNumber++)
                .originAddress(originAddress)
                .destinationDepoId(originDepo.getId())
                .build());
        for (int i = 1; i < cheapestRoute.size(); i++) {
            Long currentDepoId = cheapestRoute.get(i);
            Long previousDepoId = cheapestRoute.get(i - 1);
            shipmentRouteList.add(ShipmentRouteDTO.builder()
                    .orderId(order.getId())
                    .routePartNumber(routePartNumber++)
                    .originDepoId(previousDepoId)
                    .destinationDepoId(currentDepoId)
                    .build());
        }
        shipmentRouteList.add(ShipmentRouteDTO.builder()
                .orderId(order.getId())
                .routePartNumber(routePartNumber)
                .originDepoId(destinationDepo.getId())
                .destinationAddress(destinationAddress)
                .build());

        shipmentRouteService.saveAll(shipmentRouteList);
    }
}
