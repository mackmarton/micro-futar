package org.bme.micro_futar.orders.controllers;

import org.bme.micro_futar.orders.IntegrationTest;
import org.bme.micro_futar.orders.support.KafkaTestClient;
import org.bme.micro_futar.orders.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The sender fills in the shipment form from these, so none of them asks for a token. */
@IntegrationTest
class ReferenceDataApiTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private KafkaTestClient kafka;

    @Test
    void regionsAreReadableWithoutToken() throws Exception {
        kafka.sendAndAwaitConsumption("location-region-topic", "1", TestFixtures.centralEurope().build());

        mockMvc.perform(get("/api/regions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Közép-Európa"));
        mockMvc.perform(get("/api/regions/{id}", TestFixtures.CENTRAL_EUROPE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Közép-Európa"));
    }

    @Test
    void countriesAreReadableWithoutToken() throws Exception {
        kafka.sendAndAwaitConsumption("location-country-topic", "1", TestFixtures.hungary().build());

        mockMvc.perform(get("/api/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Magyarország"));
        mockMvc.perform(get("/api/countries/{id}", TestFixtures.HUNGARY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currencyCode").value("HUF"));
    }

    @Test
    void citiesAreListedByCountry() throws Exception {
        kafka.sendAndAwaitConsumption("location-city-topic", "1", TestFixtures.budapest().build());
        kafka.sendAndAwaitConsumption("location-city-topic", "2", TestFixtures.vienna().build());

        mockMvc.perform(get("/api/countries/{id}/cities", TestFixtures.AUSTRIA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Bécs"));
        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/api/cities/{id}", TestFixtures.BUDAPEST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Budapest"));
    }

    @Test
    void packageSizesAreReadableWithoutToken() throws Exception {
        kafka.sendAndAwaitConsumption("package-size-topic", "1", TestFixtures.smallPackage().build());

        mockMvc.perform(get("/api/package-sizes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("S"));
        mockMvc.perform(get("/api/package-sizes/{id}", TestFixtures.SMALL_PACKAGE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxLength").value(35.0));
    }

    @Test
    void countryPricesCanBeFilteredByRoute() throws Exception {
        kafka.sendAndAwaitConsumption("country-price-topic", "1", TestFixtures.countryPrice().build());
        kafka.sendAndAwaitConsumption("country-price-topic", "2", TestFixtures.countryPrice()
                .id(2L)
                .originCountryId(TestFixtures.AUSTRIA_ID)
                .destinationCountryId(TestFixtures.HUNGARY_ID)
                .build());

        mockMvc.perform(get("/api/country-prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/api/country-prices")
                        .param("originCountryId", String.valueOf(TestFixtures.AUSTRIA_ID))
                        .param("destinationCountryId", String.valueOf(TestFixtures.HUNGARY_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2));
        mockMvc.perform(get("/api/country-prices/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxPrice").value(4990.0));
    }

    @Test
    void currenciesAreReadableWithoutToken() throws Exception {
        kafka.sendAndAwaitConsumption("currency-topic", "1", TestFixtures.forint().build());

        mockMvc.perform(get("/api/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("HUF"));
        mockMvc.perform(get("/api/currencies/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("Ft"));
    }

    @Test
    void deletedEntryIsHiddenFromTheListButStillReadableById() throws Exception {
        kafka.sendAndAwaitConsumption("location-city-topic", "1", TestFixtures.budapest().deleted(true).build());

        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        // Older shipments still refer to it.
        mockMvc.perform(get("/api/cities/{id}", TestFixtures.BUDAPEST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"regions", "countries", "cities", "package-sizes", "country-prices", "currencies"})
    void unknownIdIsNotFound(String resource) throws Exception {
        mockMvc.perform(get("/api/{resource}/{id}", resource, 404_404))
                .andExpect(status().isNotFound());
    }
}
