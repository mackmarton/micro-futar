package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.EnableTestContainers;
import org.bme.micro_futar.logistics.entities.CountryBaseFee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/** A soft-deleted base fee must not be found, so its (country, size) key is free again (needs Docker). */
@EnableTestContainers
@SpringBootTest
@Transactional
class CountryBaseFeeRepositoryTests {

    @Autowired private CountryBaseFeeRepository repository;

    private CountryBaseFee fee(double price) {
        CountryBaseFee fee = new CountryBaseFee();
        fee.setCountryId(1L);
        fee.setPackageSizeId(2L);
        fee.setPrice(price);
        return fee;
    }

    @Test
    void softDeletedFeeIsNotFoundByKey() {
        CountryBaseFee saved = repository.saveAndFlush(fee(100.0));
        assertThat(repository.findByCountryIdAndPackageSizeId(1L, 2L)).isPresent();

        repository.delete(saved);
        repository.flush();

        assertThat(repository.findByCountryIdAndPackageSizeId(1L, 2L)).isEmpty();
        repository.saveAndFlush(fee(200.0));
        assertThat(repository.findByCountryIdAndPackageSizeId(1L, 2L)).get().extracting(CountryBaseFee::getPrice).isEqualTo(200.0);
    }
}
