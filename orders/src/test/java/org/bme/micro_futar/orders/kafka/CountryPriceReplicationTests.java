package org.bme.micro_futar.orders.kafka;

import org.bme.micro_futar.orders.IntegrationTest;
import org.bme.micro_futar.orders.entities.CountryPrice;
import org.bme.micro_futar.orders.repositories.CountryPriceRepository;
import org.bme.micro_futar.orders.support.KafkaTestClient;
import org.bme.micro_futar.orders.support.TestFixtures;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class CountryPriceReplicationTests {

    @Autowired private KafkaTestClient kafka;
    @Autowired private CountryPriceRepository prices;

    @Test
    void newPriceIsStored() {
        publish(TestFixtures.countryPrice().build());

        assertThat(prices.findById(1L)).get().satisfies(price -> {
            assertThat(price.getOriginCountryId()).isEqualTo(TestFixtures.HUNGARY_ID);
            assertThat(price.getDestinationCountryId()).isEqualTo(TestFixtures.AUSTRIA_ID);
            assertThat(price.getMaxPrice()).isEqualTo(4990.0);
        });
    }

    @Test
    void changedPriceOverwritesTheLocalCopy() {
        publish(TestFixtures.countryPrice().build());

        publish(TestFixtures.countryPrice().minPrice(3790.0).maxPrice(5390.0).build());

        assertThat(prices.findAll()).singleElement().satisfies(price -> {
            assertThat(price.getMinPrice()).isEqualTo(3790.0);
            assertThat(price.getMaxPrice()).isEqualTo(5390.0);
        });
    }

    @Test
    void deletedPriceIsSoftDeleted() {
        publish(TestFixtures.countryPrice().build());

        publish(TestFixtures.countryPrice().deleted(true).build());

        assertThat(prices.findAll()).isEmpty();
        assertThat(prices.findById(1L)).get().extracting(CountryPrice::isDeleted).isEqualTo(true);
    }

    @Test
    void redeliveredPriceKeepsASingleCopy() {
        publish(TestFixtures.countryPrice().build());

        publish(TestFixtures.countryPrice().build());

        assertThat(prices.findAll()).singleElement()
                .extracting(CountryPrice::getMaxPrice).isEqualTo(4990.0);
    }

    private void publish(CountryPriceDTO message) {
        kafka.sendAndAwaitConsumption("country-price-topic", message.getId().toString(), message);
    }
}
