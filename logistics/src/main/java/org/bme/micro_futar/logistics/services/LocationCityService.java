package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.entities.LocationCity;
import org.bme.micro_futar.logistics.kafka.KafkaProducerService;
import org.bme.micro_futar.logistics.mappers.LocationCityMapper;
import org.bme.micro_futar.logistics.repositories.LocationCityRepository;
import org.bme.micro_futar.logistics.services.geocoding.GeocodeResult;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationCityService {

    private final LocationCityRepository locationCityRepository;
    private final LocationCityMapper locationCityMapper;
    private final KafkaProducerService kafkaProducerService;
    private final ReferenceValidator referenceValidator;

    public List<LocationCityDTO> getAllCities() {
        return locationCityRepository.findAll().stream()
                .map(locationCityMapper::toDTO)
                .toList();
    }

    public Optional<LocationCityDTO> getCityById(Long id) {
        return locationCityRepository.findById(id)
                .map(locationCityMapper::toDTO);
    }

    public List<LocationCityDTO> getAllCitiesByCountryId(Long countryId) {
        return locationCityRepository.findAllByCountryId(countryId).stream()
                .map(locationCityMapper::toDTO)
                .toList();
    }

    @Transactional
    public LocationCityDTO createCity(LocationCityDTO locationCityDTO) {
        return createCity(locationCityDTO, null);
    }

    /**
     * @param geocoded geo data just fetched from the geocoder (outside of any transaction), or null; geo fields
     *                 sent by the client are treated as a manual setting
     */
    @Transactional
    public LocationCityDTO createCity(LocationCityDTO locationCityDTO, GeocodeResult geocoded) {
        referenceValidator.validate(locationCityDTO);
        LocationCity locationCity = locationCityMapper.toEntity(locationCityDTO);
        locationCity.setDeleted(false);
        if (geocoded != null) {
            applyGeocode(locationCity, geocoded);
        } else if (hasGeo(locationCityDTO)) {
            validateGeo(locationCityDTO);
            locationCity.setGeoManual(true);
            locationCity.setGeocodedAt(null);
        } else {
            clearGeo(locationCity);
        }
        LocationCity savedCity = locationCityRepository.save(locationCity);
        LocationCityDTO result = locationCityMapper.toDTO(savedCity);
        publishAfterCommit(result);
        return result;
    }

    @Transactional
    public Optional<LocationCityDTO> updateCity(Long id, LocationCityDTO locationCityDTO) {
        return updateCity(id, locationCityDTO, null);
    }

    /**
     * Geo fields in the DTO that differ from the stored ones make the city manual; a DTO without any geo field keeps
     * the stored geo data (clearing is done by re-geocoding).
     */
    @Transactional
    public Optional<LocationCityDTO> updateCity(Long id, LocationCityDTO locationCityDTO, GeocodeResult geocoded) {
        if (locationCityDTO.getId() != null && !locationCityDTO.getId().equals(id)) {
            throw new IllegalArgumentException("Path ID does not match DTO ID");
        }

        return locationCityRepository.findById(id)
                .map(existingCity -> {
                    referenceValidator.validate(locationCityDTO);
                    LocationCity updatedCity = locationCityMapper.toEntity(locationCityDTO);
                    updatedCity.setId(id);
                    updatedCity.setDeleted(false);
                    if (geocoded != null) {
                        applyGeocode(updatedCity, geocoded);
                    } else if (hasGeo(locationCityDTO) && geoDiffers(existingCity, locationCityDTO)) {
                        validateGeo(locationCityDTO);
                        updatedCity.setGeoManual(true);
                        updatedCity.setGeocodedAt(existingCity.getGeocodedAt());
                    } else {
                        copyGeo(existingCity, updatedCity);
                    }
                    LocationCity savedCity = locationCityRepository.save(updatedCity);
                    LocationCityDTO result = locationCityMapper.toDTO(savedCity);
                    publishAfterCommit(result);
                    return result;
                });
    }

    /**
     * Stores geocoder data on an existing city and publishes it.
     *
     * @param respectManual skip cities an administrator set by hand (bulk run); false for an explicit re-geocoding
     * @return empty when the city does not exist or was skipped
     */
    @Transactional
    public Optional<LocationCityDTO> applyGeocode(Long id, GeocodeResult geocoded, boolean respectManual) {
        return locationCityRepository.findById(id)
                .filter(city -> !(respectManual && city.isGeoManual()))
                .map(city -> {
                    applyGeocode(city, geocoded);
                    LocationCityDTO result = locationCityMapper.toDTO(locationCityRepository.save(city));
                    publishAfterCommit(result);
                    return result;
                });
    }

    @Transactional
    public boolean deleteCity(Long id) {
        return locationCityRepository.findById(id)
                .map(locationCity -> {
                    LocationCityDTO deletedLocationCity = locationCityMapper.toDTO(locationCity);
                    // Soft delete via @SQLDelete
                    locationCityRepository.delete(locationCity);
                    deletedLocationCity.setDeleted(true);
                    publishAfterCommit(deletedLocationCity);
                    return true;
                })
                .orElse(false);
    }

    private void publishAfterCommit(LocationCityDTO dto) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    kafkaProducerService.sendLocationCity(dto);
                }
            });
        } else {
            kafkaProducerService.sendLocationCity(dto);
        }
    }

    private static void applyGeocode(LocationCity city, GeocodeResult geo) {
        city.setCenterLatitude(geo.centerLatitude());
        city.setCenterLongitude(geo.centerLongitude());
        city.setBboxMinLatitude(geo.bboxMinLatitude());
        city.setBboxMinLongitude(geo.bboxMinLongitude());
        city.setBboxMaxLatitude(geo.bboxMaxLatitude());
        city.setBboxMaxLongitude(geo.bboxMaxLongitude());
        city.setGeocodedAt(ZonedDateTime.now());
        city.setGeoManual(false);
    }

    private static void clearGeo(LocationCity city) {
        city.setCenterLatitude(null);
        city.setCenterLongitude(null);
        city.setBboxMinLatitude(null);
        city.setBboxMinLongitude(null);
        city.setBboxMaxLatitude(null);
        city.setBboxMaxLongitude(null);
        city.setGeocodedAt(null);
        city.setGeoManual(false);
    }

    private static void copyGeo(LocationCity from, LocationCity to) {
        to.setCenterLatitude(from.getCenterLatitude());
        to.setCenterLongitude(from.getCenterLongitude());
        to.setBboxMinLatitude(from.getBboxMinLatitude());
        to.setBboxMinLongitude(from.getBboxMinLongitude());
        to.setBboxMaxLatitude(from.getBboxMaxLatitude());
        to.setBboxMaxLongitude(from.getBboxMaxLongitude());
        to.setGeocodedAt(from.getGeocodedAt());
        to.setGeoManual(from.isGeoManual());
    }

    /** True when the client sent any of the six geo values. */
    public static boolean hasGeo(LocationCityDTO dto) {
        return dto.getCenterLatitude() != null || dto.getCenterLongitude() != null
                || dto.getBboxMinLatitude() != null || dto.getBboxMinLongitude() != null
                || dto.getBboxMaxLatitude() != null || dto.getBboxMaxLongitude() != null;
    }

    /** True when the DTO carries geo values that differ from the stored ones. */
    public static boolean geoDiffers(LocationCity city, LocationCityDTO dto) {
        return !Objects.equals(city.getCenterLatitude(), dto.getCenterLatitude())
                || !Objects.equals(city.getCenterLongitude(), dto.getCenterLongitude())
                || !Objects.equals(city.getBboxMinLatitude(), dto.getBboxMinLatitude())
                || !Objects.equals(city.getBboxMinLongitude(), dto.getBboxMinLongitude())
                || !Objects.equals(city.getBboxMaxLatitude(), dto.getBboxMaxLatitude())
                || !Objects.equals(city.getBboxMaxLongitude(), dto.getBboxMaxLongitude());
    }

    /** Latitude -90..90, longitude -180..180, centre as a pair, bbox all-or-nothing with min <= max. */
    static void validateGeo(LocationCityDTO dto) {
        Double centerLat = dto.getCenterLatitude();
        Double centerLon = dto.getCenterLongitude();
        if ((centerLat == null) != (centerLon == null)) {
            throw new IllegalArgumentException("centerLatitude and centerLongitude must be set together");
        }
        requireRange("centerLatitude", centerLat, -90, 90);
        requireRange("centerLongitude", centerLon, -180, 180);

        Double minLat = dto.getBboxMinLatitude();
        Double minLon = dto.getBboxMinLongitude();
        Double maxLat = dto.getBboxMaxLatitude();
        Double maxLon = dto.getBboxMaxLongitude();
        int bboxValues = (minLat == null ? 0 : 1) + (minLon == null ? 0 : 1) + (maxLat == null ? 0 : 1) + (maxLon == null ? 0 : 1);
        if (bboxValues != 0 && bboxValues != 4) {
            throw new IllegalArgumentException("The four bounding box values must be set together");
        }
        requireRange("bboxMinLatitude", minLat, -90, 90);
        requireRange("bboxMaxLatitude", maxLat, -90, 90);
        requireRange("bboxMinLongitude", minLon, -180, 180);
        requireRange("bboxMaxLongitude", maxLon, -180, 180);
        if (bboxValues == 4 && (minLat > maxLat || minLon > maxLon)) {
            throw new IllegalArgumentException("Bounding box minimum values must not exceed the maximum values");
        }
    }

    private static void requireRange(String field, Double value, double min, double max) {
        if (value != null && (value.isNaN() || value < min || value > max)) {
            throw new IllegalArgumentException(field + " must be between " + min + " and " + max);
        }
    }
}
