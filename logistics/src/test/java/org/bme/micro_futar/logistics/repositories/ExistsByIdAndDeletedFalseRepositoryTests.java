package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.IntegrationTest;
import org.bme.micro_futar.logistics.entities.Courier;
import org.bme.micro_futar.logistics.entities.Currency;
import org.bme.micro_futar.logistics.entities.Depo;
import org.bme.micro_futar.logistics.entities.LocationCity;
import org.bme.micro_futar.logistics.entities.LocationCountry;
import org.bme.micro_futar.logistics.entities.LocationRegion;
import org.bme.micro_futar.logistics.entities.PackageSize;
import org.bme.micro_futar.logistics.entities.Vehicle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

/** Proves the existence checks used by ReferenceValidator treat soft-deleted rows as absent. */
@IntegrationTest
class ExistsByIdAndDeletedFalseRepositoryTests {

    @Autowired private LocationRegionRepository regionRepository;
    @Autowired private LocationCountryRepository countryRepository;
    @Autowired private LocationCityRepository cityRepository;
    @Autowired private CurrencyRepository currencyRepository;
    @Autowired private DepoRepository depoRepository;
    @Autowired private PackageSizeRepository packageSizeRepository;
    @Autowired private CourierRepository courierRepository;
    @Autowired private VehicleRepository vehicleRepository;

    private <T> void assertSoftDeleteIsHonoured(JpaRepository<T, Long> repository, T entity,
                                                Function<T, Long> idOf, Predicate<Long> exists) {
        T saved = repository.saveAndFlush(entity);
        Long id = idOf.apply(saved);
        assertThat(exists.test(id)).as("existing").isTrue();
        assertThat(exists.test(id + 1_000_000)).as("missing").isFalse();

        repository.delete(saved);
        repository.flush();
        assertThat(exists.test(id)).as("soft-deleted").isFalse();
    }

    @Test
    void softDeletedRegionDoesNotExist() {
        assertSoftDeleteIsHonoured(regionRepository, new LocationRegion(), LocationRegion::getId, regionRepository::existsByIdAndDeletedFalse);
    }

    @Test
    void softDeletedCountryDoesNotExist() {
        assertSoftDeleteIsHonoured(countryRepository, new LocationCountry(), LocationCountry::getId, countryRepository::existsByIdAndDeletedFalse);
    }

    @Test
    void softDeletedCityDoesNotExist() {
        assertSoftDeleteIsHonoured(cityRepository, new LocationCity(), LocationCity::getId, cityRepository::existsByIdAndDeletedFalse);
    }

    @Test
    void cityBelongsToCountryCheckIgnoresDeletedCity() {
        LocationCity city = new LocationCity();
        city.setCountryId(77L);
        LocationCity saved = cityRepository.saveAndFlush(city);
        assertThat(cityRepository.existsByIdAndCountryIdAndDeletedFalse(saved.getId(), 77L)).isTrue();
        assertThat(cityRepository.existsByIdAndCountryIdAndDeletedFalse(saved.getId(), 78L)).isFalse();
        cityRepository.delete(saved);
        cityRepository.flush();
        assertThat(cityRepository.existsByIdAndCountryIdAndDeletedFalse(saved.getId(), 77L)).isFalse();
    }

    @Test
    void softDeletedCurrencyCodeDoesNotExist() {
        Currency currency = new Currency();
        currency.setCode("ZZZ");
        Currency saved = currencyRepository.saveAndFlush(currency);
        assertThat(currencyRepository.existsByCodeAndDeletedFalse("ZZZ")).isTrue();
        assertThat(currencyRepository.existsByCodeAndDeletedFalse("YYY")).isFalse();
        currencyRepository.delete(saved);
        currencyRepository.flush();
        assertThat(currencyRepository.existsByCodeAndDeletedFalse("ZZZ")).isFalse();
    }

    @Test
    void softDeletedDepoDoesNotExist() {
        assertSoftDeleteIsHonoured(depoRepository, new Depo(), Depo::getId, depoRepository::existsByIdAndDeletedFalse);
    }

    @Test
    void softDeletedPackageSizeDoesNotExist() {
        assertSoftDeleteIsHonoured(packageSizeRepository, new PackageSize(), PackageSize::getId, packageSizeRepository::existsByIdAndDeletedFalse);
    }

    @Test
    void softDeletedCourierDoesNotExist() {
        assertSoftDeleteIsHonoured(courierRepository, new Courier(), Courier::getId, courierRepository::existsByIdAndDeletedFalse);
    }

    @Test
    void softDeletedVehicleDoesNotExist() {
        assertSoftDeleteIsHonoured(vehicleRepository, new Vehicle(), Vehicle::getId, vehicleRepository::existsByIdAndDeletedFalse);
    }
}
