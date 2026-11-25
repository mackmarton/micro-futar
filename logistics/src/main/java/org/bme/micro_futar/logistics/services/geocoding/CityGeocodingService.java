package org.bme.micro_futar.logistics.services.geocoding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.logistics.entities.LocationCity;
import org.bme.micro_futar.logistics.entities.LocationCountry;
import org.bme.micro_futar.logistics.exceptions.GeocodingException;
import org.bme.micro_futar.logistics.exceptions.GeocodingNotFoundException;
import org.bme.micro_futar.logistics.repositories.LocationCityRepository;
import org.bme.micro_futar.logistics.repositories.LocationCountryRepository;
import org.bme.micro_futar.logistics.services.LocationCityService;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * Orchestrates city writes with geocoding. Deliberately not transactional: the slow OpenRouteService call happens
 * first, the database transaction (in {@link LocationCityService}) only starts afterwards.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CityGeocodingService {

    public enum Outcome { GEOCODED, NOT_FOUND, FAILED, SKIPPED }

    private final LocationCityRepository cityRepository;
    private final LocationCountryRepository countryRepository;
    private final OrsGeocodingClient geocodingClient;
    private final LocationCityService cityService;

    public LocationCityDTO createCity(LocationCityDTO dto) {
        GeocodeResult geo = LocationCityService.hasGeo(dto) ? null : tryGeocode(dto.getName(), dto.getCountryId());
        return cityService.createCity(dto, geo);
    }

    public Optional<LocationCityDTO> updateCity(Long id, LocationCityDTO dto) {
        Optional<LocationCity> existing = cityRepository.findById(id);
        GeocodeResult geo = null;
        if (existing.isPresent()) {
            LocationCity city = existing.get();
            boolean identityChanged = !Objects.equals(city.getName(), dto.getName())
                    || !Objects.equals(city.getCountryId(), dto.getCountryId());
            boolean clientChangedGeo = LocationCityService.hasGeo(dto) && LocationCityService.geoDiffers(city, dto);
            if (identityChanged && !city.isGeoManual() && !clientChangedGeo) {
                geo = tryGeocode(dto.getName(), dto.getCountryId());
            }
        }
        return cityService.updateCity(id, dto, geo);
    }

    /** Explicit re-geocoding, also for manual cities (which become non-manual). Failures are reported to the caller. */
    public Optional<LocationCityDTO> regeocode(Long id) {
        Optional<LocationCity> city = cityRepository.findById(id);
        if (city.isEmpty()) {
            return Optional.empty();
        }
        String iso = isoCodeOf(city.get().getCountryId());
        if (iso == null) {
            throw new GeocodingNotFoundException("The country of the city has no ISO code");
        }
        GeocodeResult geo = geocodingClient.geocode(city.get().getName(), iso)
                .orElseThrow(() -> new GeocodingNotFoundException("No geocoding result for '" + city.get().getName() + "'"));
        return cityService.applyGeocode(id, geo, false);
    }

    /** One step of the bulk run: geocodes the city unless it became manual or got geo data meanwhile. */
    public Outcome geocodeMissing(LocationCity city) {
        String iso = isoCodeOf(city.getCountryId());
        if (iso == null) {
            log.warn("Skipping geocoding of city {} ({}): its country has no ISO code", city.getId(), city.getName());
            return Outcome.NOT_FOUND;
        }
        try {
            Optional<GeocodeResult> geo = geocodingClient.geocode(city.getName(), iso);
            if (geo.isEmpty()) {
                log.warn("No geocoding result for city {} ({})", city.getId(), city.getName());
                return Outcome.NOT_FOUND;
            }
            return cityService.applyGeocode(city.getId(), geo.get(), true).isPresent() ? Outcome.GEOCODED : Outcome.SKIPPED;
        } catch (RuntimeException e) {
            log.warn("Geocoding of city {} ({}) failed: {}", city.getId(), city.getName(), e.toString());
            return Outcome.FAILED;
        }
    }

    /** Best effort for city saves: a missing ISO code, no hit or an unreachable service leave the city without geo data. */
    private GeocodeResult tryGeocode(String cityName, Long countryId) {
        String iso = isoCodeOf(countryId);
        if (iso == null) {
            log.warn("City '{}' saved without geo data: country {} has no ISO code", cityName, countryId);
            return null;
        }
        try {
            Optional<GeocodeResult> geo = geocodingClient.geocode(cityName, iso);
            if (geo.isEmpty()) {
                log.warn("City '{}' saved without geo data: no geocoding result", cityName);
            }
            return geo.orElse(null);
        } catch (GeocodingException e) {
            log.warn("City '{}' saved without geo data: geocoder unavailable ({})", cityName, e.getMessage());
            return null;
        }
    }

    private String isoCodeOf(Long countryId) {
        if (countryId == null) {
            return null;
        }
        return countryRepository.findById(countryId)
                .map(LocationCountry::getIsoCode)
                .filter(code -> !code.isBlank())
                .orElse(null);
    }
}
