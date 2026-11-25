package org.bme.micro_futar.orders.services;

import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.orders.entities.LocationCity;
import org.bme.micro_futar.orders.exceptions.LocationValidationException;
import org.bme.micro_futar.orders.exceptions.LocationValidationException.Code;
import org.bme.micro_futar.orders.exceptions.LocationValidationException.Party;
import org.bme.micro_futar.orders.repositories.LocationCityRepository;
import org.bme.micro_futar.orders.util.GeoDistance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Checks that a shipment party's coordinates lie in the city they are given with. Works on the local copy of the
 * city (kept up to date over Kafka), so there is no external call.
 */
@Slf4j
@Service
public class LocationCheckService {

    private final LocationCityRepository locationCityRepository;
    private final double toleranceKm;
    private final double defaultRadiusKm;
    private final Set<Long> citiesWarnedAbout = ConcurrentHashMap.newKeySet();

    public LocationCheckService(LocationCityRepository locationCityRepository,
                                @Value("${orders.location-check.tolerance-km:3}") double toleranceKm,
                                @Value("${orders.location-check.default-radius-km:25}") double defaultRadiusKm) {
        this.locationCityRepository = locationCityRepository;
        this.toleranceKm = toleranceKm;
        this.defaultRadiusKm = defaultRadiusKm;
    }

    /** @throws LocationValidationException when the location is not acceptable */
    public void check(Party party, Long countryId, Long cityId, Double latitude, Double longitude) {
        if (!GeoDistance.isValidCoordinate(latitude, longitude)) {
            throw new LocationValidationException(Code.INVALID_COORDINATES, party,
                    party + " coordinates are missing or out of range");
        }
        LocationCity city = cityId == null ? null : locationCityRepository.findById(cityId)
                .filter(found -> !found.isDeleted())
                .orElse(null);
        if (city == null) {
            throw new LocationValidationException(Code.CITY_NOT_FOUND, party, party + " city " + cityId + " does not exist");
        }
        if (!Objects.equals(city.getCountryId(), countryId)) {
            throw new LocationValidationException(Code.CITY_COUNTRY_MISMATCH, party,
                    party + " city " + cityId + " does not belong to country " + countryId);
        }

        if (city.getBboxMinLatitude() != null && city.getBboxMinLongitude() != null
                && city.getBboxMaxLatitude() != null && city.getBboxMaxLongitude() != null) {
            if (!GeoDistance.isWithinBbox(latitude, longitude,
                    city.getBboxMinLatitude(), city.getBboxMinLongitude(),
                    city.getBboxMaxLatitude(), city.getBboxMaxLongitude(), toleranceKm)) {
                throw tooFar(party, city);
            }
        } else if (city.getCenterLatitude() != null && city.getCenterLongitude() != null) {
            if (GeoDistance.haversineKm(latitude, longitude, city.getCenterLatitude(), city.getCenterLongitude())
                    > defaultRadiusKm) {
                throw tooFar(party, city);
            }
        } else if (citiesWarnedAbout.add(city.getId())) {
            log.warn("City {} ({}) has no geo data, location check is skipped for it", city.getId(), city.getName());
        }
    }

    private static LocationValidationException tooFar(Party party, LocationCity city) {
        return new LocationValidationException(Code.LOCATION_TOO_FAR_FROM_CITY, party,
                party + " location is too far from city " + city.getName());
    }
}
