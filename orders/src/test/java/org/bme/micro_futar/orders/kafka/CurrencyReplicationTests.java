package org.bme.micro_futar.orders.kafka;

import org.bme.micro_futar.orders.IntegrationTest;
import org.bme.micro_futar.orders.entities.Currency;
import org.bme.micro_futar.orders.repositories.CurrencyRepository;
import org.bme.micro_futar.orders.support.KafkaTestClient;
import org.bme.micro_futar.orders.support.TestFixtures;
import org.bme.micro_futar.shared.dtos.CurrencyDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class CurrencyReplicationTests {

    @Autowired private KafkaTestClient kafka;
    @Autowired private CurrencyRepository currencies;

    @Test
    void newCurrencyIsStored() {
        publish(TestFixtures.forint().build());

        assertThat(currencies.findById(1L)).get().satisfies(currency -> {
            assertThat(currency.getCode()).isEqualTo("HUF");
            assertThat(currency.getSymbol()).isEqualTo("Ft");
        });
    }

    @Test
    void changedCurrencyOverwritesTheLocalCopy() {
        publish(TestFixtures.forint().build());

        publish(TestFixtures.forint().name("Forint").build());

        assertThat(currencies.findAll()).singleElement()
                .extracting(Currency::getName).isEqualTo("Forint");
    }

    @Test
    void deletedCurrencyIsSoftDeleted() {
        publish(TestFixtures.forint().build());

        publish(TestFixtures.forint().deleted(true).build());

        assertThat(currencies.findAll()).isEmpty();
        assertThat(currencies.findById(1L)).get().extracting(Currency::isDeleted).isEqualTo(true);
    }

    @Test
    void redeliveredCurrencyKeepsASingleCopy() {
        publish(TestFixtures.forint().build());

        publish(TestFixtures.forint().build());

        assertThat(currencies.findAll()).singleElement()
                .extracting(Currency::getCode).isEqualTo("HUF");
    }

    private void publish(CurrencyDTO message) {
        kafka.sendAndAwaitConsumption("currency-topic", message.getId().toString(), message);
    }
}
