package org.bme.micro_futar.logistics.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bme.micro_futar.logistics.IntegrationTest;
import org.bme.micro_futar.logistics.dtos.CountryBaseFeeDTO;
import org.bme.micro_futar.logistics.entities.CountryPrice;
import org.bme.micro_futar.logistics.repositories.CountryBaseFeeRepository;
import org.bme.micro_futar.logistics.repositories.CountryPriceRepository;
import org.bme.micro_futar.logistics.repositories.CurrencyRepository;
import org.bme.micro_futar.logistics.repositories.DepoRepository;
import org.bme.micro_futar.logistics.repositories.LocationCountryRepository;
import org.bme.micro_futar.logistics.repositories.LocationRegionRepository;
import org.bme.micro_futar.logistics.repositories.PackageSizeRepository;
import org.bme.micro_futar.logistics.support.KafkaTestClient;
import org.bme.micro_futar.logistics.support.TestFixtures;
import org.bme.micro_futar.logistics.support.TestJwts;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
import org.bme.micro_futar.shared.dtos.CurrencyDTO;
import org.bme.micro_futar.shared.dtos.PackageSizeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** What the price of a shipment is built from: base fees, country prices, package sizes and currencies. */
@IntegrationTest
class PriceAndBaseFeeApiTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TestJwts testJwts;
    @Autowired private KafkaTestClient kafka;
    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private LocationRegionRepository regions;
    @Autowired private LocationCountryRepository countries;
    @Autowired private PackageSizeRepository packageSizes;
    @Autowired private DepoRepository depos;
    @Autowired private CountryBaseFeeRepository baseFees;
    @Autowired private CountryPriceRepository prices;
    @Autowired private CurrencyRepository currencies;

    private Long hungary;
    private Long small;

    /** One depo in Hungary: the domestic price is the base fee alone. */
    @BeforeEach
    void givenADepoInHungary() {
        Long region = regions.save(TestFixtures.entity(TestFixtures.centralEurope().build())).getId();
        hungary = countries.save(TestFixtures.entity(TestFixtures.hungary(region).build())).getId();
        small = packageSizes.save(TestFixtures.entity(TestFixtures.smallPackage().build())).getId();
        depos.save(TestFixtures.entity(TestFixtures.budapestDepo(hungary, null)));
    }

    @Test
    void everyBaseFeeChangeRecalculatesThePrices() throws Exception {
        CountryBaseFeeDTO request = TestFixtures.baseFee(hungary, small);

        Long id = create(request);
        assertThat(domesticPrice().getMaxPrice()).isEqualTo(1500.0);

        request.setPrice(1800.0);
        send(put("/api/country-base-fees/{id}", id), request).andExpect(status().isOk());
        assertThat(domesticPrice().getMaxPrice()).isEqualTo(1800.0);

        send(delete("/api/country-base-fees/{id}", id), null).andExpect(status().isNoContent());
        assertThat(baseFees.findById(id)).isEmpty();
        assertThat(domesticPrice().getMaxPrice()).isZero();
        await().untilAsserted(() -> assertThat(kafka.received("country-price-topic", CountryPriceDTO.class))
                .extracting(CountryPriceDTO::getMinPrice).containsExactly(1500.0, 1800.0, 0.0));
    }

    @Test
    void secondBaseFeeForTheSameCountryAndSizeIsAConflict() throws Exception {
        baseFees.save(TestFixtures.entity(TestFixtures.baseFee(hungary, small)));

        send(post("/api/country-base-fees"), TestFixtures.baseFee(hungary, small)).andExpect(status().isConflict());

        assertThat(baseFees.findAll()).hasSize(1);
        assertNoPriceWasRecalculated();
    }

    @Test
    void updateMayKeepItsOwnCountryAndSizeButNotTakeAnothers() throws Exception {
        Long large = packageSizes.save(TestFixtures.entity(TestFixtures.smallPackage().name("L").build())).getId();
        Long smallFee = create(TestFixtures.baseFee(hungary, small));
        Long largeFee = create(TestFixtures.baseFee(hungary, large));

        send(put("/api/country-base-fees/{id}", smallFee), TestFixtures.baseFee(hungary, small))
                .andExpect(status().isOk());
        send(put("/api/country-base-fees/{id}", largeFee), TestFixtures.baseFee(hungary, small))
                .andExpect(status().isConflict());

        assertThat(baseFees.findById(largeFee)).get().extracting("packageSizeId").isEqualTo(large);
    }

    @Test
    void unknownBaseFeeIsNotFoundAndChangesNoPrice() throws Exception {
        send(put("/api/country-base-fees/{id}", 404_404), TestFixtures.baseFee(hungary, small))
                .andExpect(status().isNotFound());
        send(delete("/api/country-base-fees/{id}", 404_404), null).andExpect(status().isNotFound());

        assertNoPriceWasRecalculated();
    }

    @Test
    void baseFeeWithUnknownCountryIsRejected() throws Exception {
        send(post("/api/country-base-fees"), TestFixtures.baseFee(hungary + 1_000, small))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("countryId"));

        assertThat(baseFees.findAll()).isEmpty();
        assertNoPriceWasRecalculated();
    }

    @Test
    void recalculationAnnouncesPricesAndRequiresAdmin() throws Exception {
        baseFees.save(TestFixtures.entity(TestFixtures.baseFee(hungary, small)));

        send(post("/api/country-prices"), null).andExpect(status().isForbidden());
        assertThat(prices.findAll()).isEmpty();

        mockMvc.perform(post("/api/country-prices")
                        .with(testJwts.user(TestFixtures.ADMIN_EMAIL, "logistics-user", "admin")))
                .andExpect(status().isOk());

        assertThat(domesticPrice().getMaxPrice()).isEqualTo(1500.0);
        await().untilAsserted(() -> assertThat(kafka.received("country-price-topic", CountryPriceDTO.class))
                .singleElement().extracting(CountryPriceDTO::getMaxPrice).isEqualTo(1500.0));
    }

    @Test
    void packageSizeIsStoredAndAnnouncedThroughItsLifecycle() throws Exception {
        PackageSizeDTO request = TestFixtures.smallPackage().name("M").maxLength(60.0).build();
        Long id = read(send(post("/api/package-sizes"), request).andExpect(status().isCreated()), PackageSizeDTO.class)
                .getId();
        request.setMaxLength(65.0);
        send(put("/api/package-sizes/{id}", id), request).andExpect(status().isOk());
        assertThat(packageSizes.findById(id)).get().extracting("maxLength").isEqualTo(65.0);

        send(delete("/api/package-sizes/{id}", id), null).andExpect(status().isNoContent());

        assertThat(packageSizes.findById(id)).isEmpty();
        await().untilAsserted(() -> assertThat(kafka.received("package-size-topic", PackageSizeDTO.class))
                .extracting(PackageSizeDTO::getMaxLength, PackageSizeDTO::isDeleted)
                .containsExactly(tuple(60.0, false), tuple(65.0, false), tuple(65.0, true)));
    }

    @Test
    void currencyIsStoredAndAnnouncedThroughItsLifecycle() throws Exception {
        CurrencyDTO request = TestFixtures.euro().build();
        Long id = read(send(post("/api/currencies"), request).andExpect(status().isCreated()), CurrencyDTO.class)
                .getId();
        request.setName("Euro");
        send(put("/api/currencies/{id}", id), request).andExpect(status().isOk());
        assertThat(currencies.findById(id)).get().extracting("name").isEqualTo("Euro");

        send(delete("/api/currencies/{id}", id), null).andExpect(status().isNoContent());

        assertThat(currencies.findById(id)).isEmpty();
        await().untilAsserted(() -> assertThat(kafka.received("currency-topic", CurrencyDTO.class))
                .extracting(CurrencyDTO::getName, CurrencyDTO::isDeleted)
                .containsExactly(tuple("Euró", false), tuple("Euro", false), tuple("Euro", true)));
    }

    @Test
    void deletedCurrencyCodeCanBeCreatedAgain() throws Exception {
        CurrencyDTO request = TestFixtures.euro().build();
        Long id = read(send(post("/api/currencies"), request).andExpect(status().isCreated()), CurrencyDTO.class)
                .getId();
        send(post("/api/currencies"), request).andExpect(status().isBadRequest());
        send(delete("/api/currencies/{id}", id), null).andExpect(status().isNoContent());

        Long again = read(send(post("/api/currencies"), request).andExpect(status().isCreated()), CurrencyDTO.class)
                .getId();

        // the code is unique, so the soft-deleted row comes back
        assertThat(again).isEqualTo(id);
        assertThat(currencies.findById(id)).isPresent();
    }

    private Long create(CountryBaseFeeDTO fee) throws Exception {
        return read(send(post("/api/country-base-fees"), fee).andExpect(status().isCreated()), CountryBaseFeeDTO.class)
                .getId();
    }

    private CountryPrice domesticPrice() {
        return prices.findByOriginCountryIdAndDestinationCountryIdAndPackageSizeId(hungary, hungary, small)
                .orElseThrow();
    }

    /** A recalculation would have created the domestic price. */
    private void assertNoPriceWasRecalculated() {
        assertThat(prices.findAll()).isEmpty();
        kafkaTemplate.flush();
        assertThat(kafka.received("country-price-topic")).isEmpty();
    }

    private ResultActions send(MockHttpServletRequestBuilder request, Object body) throws Exception {
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body));
        }
        return mockMvc.perform(request.with(testJwts.user(TestFixtures.DISPATCHER_EMAIL, "logistics-user")));
    }

    private <T> T read(ResultActions result, Class<T> type) throws Exception {
        return objectMapper.readValue(result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8), type);
    }
}
