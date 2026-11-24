package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.dtos.CountryBaseFeeDTO;
import org.bme.micro_futar.logistics.dtos.DepoTransitDTO;
import org.bme.micro_futar.logistics.exceptions.InvalidReferenceException;
import org.bme.micro_futar.logistics.repositories.CourierRepository;
import org.bme.micro_futar.logistics.repositories.CurrencyRepository;
import org.bme.micro_futar.logistics.repositories.DepoRepository;
import org.bme.micro_futar.logistics.repositories.LocationCityRepository;
import org.bme.micro_futar.logistics.repositories.LocationCountryRepository;
import org.bme.micro_futar.logistics.repositories.LocationRegionRepository;
import org.bme.micro_futar.logistics.repositories.PackageSizeRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteRepository;
import org.bme.micro_futar.logistics.repositories.VehicleRepository;
import org.bme.micro_futar.shared.dtos.CourierDTO;
import org.bme.micro_futar.shared.dtos.DepoDTO;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

/**
 * Checks references between entities that logistics owns. Entities reference each other by id only (no FKs, no JPA
 * associations), so writes arriving through the REST API are validated here. Unset (null) references are skipped.
 * Not for Kafka consumers: local copies arrive in arbitrary order.
 */
@Component
@RequiredArgsConstructor
public class ReferenceValidator {

    private final LocationRegionRepository locationRegionRepository;
    private final LocationCountryRepository locationCountryRepository;
    private final LocationCityRepository locationCityRepository;
    private final CurrencyRepository currencyRepository;
    private final DepoRepository depoRepository;
    private final PackageSizeRepository packageSizeRepository;
    private final CourierRepository courierRepository;
    private final VehicleRepository vehicleRepository;
    private final ShipmentRouteRepository shipmentRouteRepository;

    public void validate(LocationCountryDTO country) {
        requireExists("regionId", "LocationRegion", country.getRegionId(), locationRegionRepository::existsByIdAndDeletedFalse);
        requireExists("currencyCode", "Currency", country.getCurrencyCode(), currencyRepository::existsByCodeAndDeletedFalse);
    }

    public void validate(LocationCityDTO city) {
        requireExists("countryId", "LocationCountry", city.getCountryId(), locationCountryRepository::existsByIdAndDeletedFalse);
    }

    public void validate(DepoDTO depo) {
        requireExists("locationCountryId", "LocationCountry", depo.getLocationCountryId(), locationCountryRepository::existsByIdAndDeletedFalse);
        requireExists("locationCityId", "LocationCity", depo.getLocationCityId(), locationCityRepository::existsByIdAndDeletedFalse);
        if (depo.getLocationCountryId() != null && depo.getLocationCityId() != null
                && !locationCityRepository.existsByIdAndCountryIdAndDeletedFalse(depo.getLocationCityId(), depo.getLocationCountryId())) {
            throw new InvalidReferenceException("locationCityId",
                    "LocationCity " + depo.getLocationCityId() + " (locationCityId) does not belong to LocationCountry "
                            + depo.getLocationCountryId());
        }
    }

    public void validate(DepoTransitDTO transit) {
        requireExists("originDepoId", "Depo", transit.getOriginDepoId(), depoRepository::existsByIdAndDeletedFalse);
        requireExists("destinationDepoId", "Depo", transit.getDestinationDepoId(), depoRepository::existsByIdAndDeletedFalse);
        requireExists("packageSizeId", "PackageSize", transit.getPackageSizeId(), packageSizeRepository::existsByIdAndDeletedFalse);
        if (transit.getOriginDepoId() != null && transit.getOriginDepoId().equals(transit.getDestinationDepoId())) {
            throw new InvalidReferenceException("destinationDepoId",
                    "originDepoId and destinationDepoId must differ (both are " + transit.getOriginDepoId() + ")");
        }
    }

    public void validate(CountryBaseFeeDTO fee) {
        requireExists("countryId", "LocationCountry", fee.getCountryId(), locationCountryRepository::existsByIdAndDeletedFalse);
        requireExists("packageSizeId", "PackageSize", fee.getPackageSizeId(), packageSizeRepository::existsByIdAndDeletedFalse);
        if (fee.getCountryId() == null) {
            throw new InvalidReferenceException("countryId", "countryId is required");
        }
        if (fee.getPackageSizeId() == null) {
            throw new InvalidReferenceException("packageSizeId", "packageSizeId is required");
        }
        if (fee.getPrice() == null || fee.getPrice() < 0) {
            throw new IllegalArgumentException("price must be zero or positive");
        }
    }

    public void validate(CourierDTO courier) {
        requireExists("depoId", "Depo", courier.getDepoId(), depoRepository::existsByIdAndDeletedFalse);
        requireExists("vehicleId", "Vehicle", courier.getVehicleId(), vehicleRepository::existsByIdAndDeletedFalse);
    }

    public void validate(ShipmentRouteCourierDTO assignment) {
        requireExists("courierId", "Courier", assignment.getCourierId(), courierRepository::existsByIdAndDeletedFalse);
        // ShipmentRoute has no soft delete
        requireExists("shipmentRouteId", "ShipmentRoute", assignment.getShipmentRouteId(), shipmentRouteRepository::existsById);
    }

    private <T> void requireExists(String field, String entity, T reference, Predicate<T> exists) {
        if (reference != null && !exists.test(reference)) {
            throw new InvalidReferenceException(field,
                    entity + " " + reference + " (" + field + ") does not exist or has been deleted");
        }
    }
}
