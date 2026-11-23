package org.bme.micro_futar.logistics.services;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;

/**
 * "Missing" here means the repository's existsBy...DeletedFalse check returns false, which is also what a soft-deleted
 * target yields; that the repositories really treat soft-deleted rows as absent is covered by
 * {@code ExistsByIdAndDeletedFalseRepositoryTests}.
 */
@ExtendWith(MockitoExtension.class)
class ReferenceValidatorTests {

    @Mock private LocationRegionRepository locationRegionRepository;
    @Mock private LocationCountryRepository locationCountryRepository;
    @Mock private LocationCityRepository locationCityRepository;
    @Mock private CurrencyRepository currencyRepository;
    @Mock private DepoRepository depoRepository;
    @Mock private PackageSizeRepository packageSizeRepository;
    @Mock private CourierRepository courierRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private ShipmentRouteRepository shipmentRouteRepository;

    @InjectMocks
    private ReferenceValidator validator;

    @BeforeEach
    void everythingExists() {
        lenient().when(locationRegionRepository.existsByIdAndDeletedFalse(1L)).thenReturn(true);
        lenient().when(locationCountryRepository.existsByIdAndDeletedFalse(2L)).thenReturn(true);
        lenient().when(locationCityRepository.existsByIdAndDeletedFalse(3L)).thenReturn(true);
        lenient().when(locationCityRepository.existsByIdAndCountryIdAndDeletedFalse(3L, 2L)).thenReturn(true);
        lenient().when(currencyRepository.existsByCodeAndDeletedFalse("HUF")).thenReturn(true);
        lenient().when(depoRepository.existsByIdAndDeletedFalse(4L)).thenReturn(true);
        lenient().when(depoRepository.existsByIdAndDeletedFalse(5L)).thenReturn(true);
        lenient().when(packageSizeRepository.existsByIdAndDeletedFalse(6L)).thenReturn(true);
        lenient().when(vehicleRepository.existsByIdAndDeletedFalse(7L)).thenReturn(true);
        lenient().when(courierRepository.existsByIdAndDeletedFalse(8L)).thenReturn(true);
        lenient().when(shipmentRouteRepository.existsById(9L)).thenReturn(true);
    }

    private static void assertRejected(Runnable validation, String field, String messagePart) {
        assertThatThrownBy(validation::run)
                .isInstanceOfSatisfying(InvalidReferenceException.class, e -> {
                    org.assertj.core.api.Assertions.assertThat(e.getField()).isEqualTo(field);
                    org.assertj.core.api.Assertions.assertThat(e.getMessage()).contains(messagePart);
                });
    }

    // ---- LocationCountry

    private LocationCountryDTO country(Long regionId, String currencyCode) {
        LocationCountryDTO dto = new LocationCountryDTO();
        dto.setRegionId(regionId);
        dto.setCurrencyCode(currencyCode);
        return dto;
    }

    @Test
    void countryWithValidReferencesIsAccepted() {
        assertThatCode(() -> validator.validate(country(1L, "HUF"))).doesNotThrowAnyException();
    }

    @Test
    void countryWithUnknownRegionIsRejected() {
        assertRejected(() -> validator.validate(country(99L, "HUF")), "regionId",
                "LocationRegion 99 (regionId) does not exist or has been deleted");
    }

    @Test
    void countryWithUnknownCurrencyIsRejected() {
        assertRejected(() -> validator.validate(country(1L, "XXX")), "currencyCode",
                "Currency XXX (currencyCode) does not exist or has been deleted");
    }

    // ---- LocationCity

    @Test
    void cityWithValidCountryIsAccepted() {
        LocationCityDTO dto = new LocationCityDTO();
        dto.setCountryId(2L);
        assertThatCode(() -> validator.validate(dto)).doesNotThrowAnyException();
    }

    @Test
    void cityWithUnknownCountryIsRejected() {
        LocationCityDTO dto = new LocationCityDTO();
        dto.setCountryId(99L);
        assertRejected(() -> validator.validate(dto), "countryId",
                "LocationCountry 99 (countryId) does not exist or has been deleted");
    }

    // ---- Depo

    private DepoDTO depo(Long countryId, Long cityId) {
        DepoDTO dto = new DepoDTO();
        dto.setLocationCountryId(countryId);
        dto.setLocationCityId(cityId);
        return dto;
    }

    @Test
    void depoWithValidReferencesIsAccepted() {
        assertThatCode(() -> validator.validate(depo(2L, 3L))).doesNotThrowAnyException();
    }

    @Test
    void depoWithUnknownCountryIsRejected() {
        assertRejected(() -> validator.validate(depo(99L, 3L)), "locationCountryId",
                "LocationCountry 99 (locationCountryId) does not exist or has been deleted");
    }

    @Test
    void depoWithUnknownCityIsRejected() {
        assertRejected(() -> validator.validate(depo(2L, 99L)), "locationCityId",
                "LocationCity 99 (locationCityId) does not exist or has been deleted");
    }

    @Test
    void depoWithCityFromAnotherCountryIsRejected() {
        lenient().when(locationCountryRepository.existsByIdAndDeletedFalse(10L)).thenReturn(true);
        assertRejected(() -> validator.validate(depo(10L, 3L)), "locationCityId",
                "does not belong to LocationCountry 10");
    }

    @Test
    void depoWithOnlyCityOrOnlyCountryIsAcceptedWithoutBelongsToCheck() {
        assertThatCode(() -> validator.validate(depo(null, 3L))).doesNotThrowAnyException();
        assertThatCode(() -> validator.validate(depo(2L, null))).doesNotThrowAnyException();
    }

    // ---- DepoTransit

    private DepoTransitDTO transit(Long origin, Long destination, Long packageSize) {
        DepoTransitDTO dto = new DepoTransitDTO();
        dto.setOriginDepoId(origin);
        dto.setDestinationDepoId(destination);
        dto.setPackageSizeId(packageSize);
        return dto;
    }

    @Test
    void transitWithValidReferencesIsAccepted() {
        assertThatCode(() -> validator.validate(transit(4L, 5L, 6L))).doesNotThrowAnyException();
    }

    @Test
    void transitWithUnknownOriginIsRejected() {
        assertRejected(() -> validator.validate(transit(99L, 5L, 6L)), "originDepoId",
                "Depo 99 (originDepoId) does not exist or has been deleted");
    }

    @Test
    void transitWithUnknownDestinationIsRejected() {
        assertRejected(() -> validator.validate(transit(4L, 99L, 6L)), "destinationDepoId",
                "Depo 99 (destinationDepoId) does not exist or has been deleted");
    }

    @Test
    void transitWithUnknownPackageSizeIsRejected() {
        assertRejected(() -> validator.validate(transit(4L, 5L, 99L)), "packageSizeId",
                "PackageSize 99 (packageSizeId) does not exist or has been deleted");
    }

    @Test
    void transitWithSameOriginAndDestinationIsRejected() {
        assertRejected(() -> validator.validate(transit(4L, 4L, 6L)), "destinationDepoId", "must differ");
    }

    // ---- Courier

    private CourierDTO courier(Long depoId, Long vehicleId) {
        CourierDTO dto = new CourierDTO();
        dto.setDepoId(depoId);
        dto.setVehicleId(vehicleId);
        return dto;
    }

    @Test
    void courierWithValidReferencesIsAccepted() {
        assertThatCode(() -> validator.validate(courier(4L, 7L))).doesNotThrowAnyException();
    }

    @Test
    void crossDepoCourierWithoutReferencesIsAccepted() {
        assertThatCode(() -> validator.validate(courier(null, null))).doesNotThrowAnyException();
    }

    @Test
    void courierWithUnknownDepoIsRejected() {
        assertRejected(() -> validator.validate(courier(99L, 7L)), "depoId",
                "Depo 99 (depoId) does not exist or has been deleted");
    }

    @Test
    void courierWithUnknownVehicleIsRejected() {
        assertRejected(() -> validator.validate(courier(4L, 99L)), "vehicleId",
                "Vehicle 99 (vehicleId) does not exist or has been deleted");
    }

    // ---- ShipmentRouteCourier

    private ShipmentRouteCourierDTO assignment(Long courierId, Long routeId) {
        return ShipmentRouteCourierDTO.builder().courierId(courierId).shipmentRouteId(routeId).build();
    }

    @Test
    void assignmentWithValidReferencesIsAccepted() {
        assertThatCode(() -> validator.validate(assignment(8L, 9L))).doesNotThrowAnyException();
    }

    @Test
    void assignmentWithUnknownCourierIsRejected() {
        assertRejected(() -> validator.validate(assignment(99L, 9L)), "courierId",
                "Courier 99 (courierId) does not exist or has been deleted");
    }

    @Test
    void assignmentWithUnknownShipmentRouteIsRejected() {
        assertRejected(() -> validator.validate(assignment(8L, 99L)), "shipmentRouteId",
                "ShipmentRoute 99 (shipmentRouteId) does not exist or has been deleted");
    }
}
