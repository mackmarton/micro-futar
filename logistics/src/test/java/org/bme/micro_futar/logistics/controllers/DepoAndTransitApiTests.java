package org.bme.micro_futar.logistics.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bme.micro_futar.logistics.IntegrationTest;
import org.bme.micro_futar.logistics.dtos.DepoTransitDTO;
import org.bme.micro_futar.logistics.entities.CountryPrice;
import org.bme.micro_futar.logistics.repositories.CountryPriceRepository;
import org.bme.micro_futar.logistics.repositories.DepoRepository;
import org.bme.micro_futar.logistics.repositories.DepoTransitRepository;
import org.bme.micro_futar.logistics.repositories.LocationCityRepository;
import org.bme.micro_futar.logistics.repositories.LocationCountryRepository;
import org.bme.micro_futar.logistics.repositories.LocationRegionRepository;
import org.bme.micro_futar.logistics.repositories.PackageSizeRepository;
import org.bme.micro_futar.logistics.support.KafkaTestClient;
import org.bme.micro_futar.logistics.support.TestFixtures;
import org.bme.micro_futar.logistics.support.TestJwts;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
import org.bme.micro_futar.shared.dtos.DepoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Depos and the transits between them; every change of either recalculates the country prices. */
@IntegrationTest
class DepoAndTransitApiTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TestJwts testJwts;
    @Autowired private KafkaTestClient kafka;
    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private LocationRegionRepository regions;
    @Autowired private LocationCountryRepository countries;
    @Autowired private LocationCityRepository cities;
    @Autowired private PackageSizeRepository packageSizes;
    @Autowired private DepoRepository depos;
    @Autowired private DepoTransitRepository transits;
    @Autowired private CountryPriceRepository prices;

    private Long hungary;
    private Long austria;
    private Long budapest;
    private Long vienna;
    private Long small;

    @BeforeEach
    void givenCountriesCitiesAndAPackageSize() {
        Long region = regions.save(TestFixtures.entity(TestFixtures.centralEurope().build())).getId();
        hungary = countries.save(TestFixtures.entity(TestFixtures.hungary(region).build())).getId();
        austria = countries.save(TestFixtures.entity(TestFixtures.austria(region).build())).getId();
        budapest = cities.save(TestFixtures.entity(TestFixtures.budapest(hungary).build())).getId();
        vienna = cities.save(TestFixtures.entity(TestFixtures.vienna(austria).build())).getId();
        small = packageSizes.save(TestFixtures.entity(TestFixtures.smallPackage().build())).getId();
    }

    @Test
    void depoIsStoredAndAnnouncedThroughItsLifecycle() throws Exception {
        DepoDTO request = TestFixtures.budapestDepo(hungary, budapest);
        Long id = read(send(post("/api/depos"), request).andExpect(status().isCreated()), DepoDTO.class).getId();
        request.setName("Budapest Kőbányai depó");
        send(put("/api/depos/{id}", id), request).andExpect(status().isOk());
        assertThat(depos.findById(id)).get().extracting("name").isEqualTo("Budapest Kőbányai depó");

        send(delete("/api/depos/{id}", id), null).andExpect(status().isNoContent());

        assertThat(depos.findById(id)).isEmpty();
        await().untilAsserted(() -> assertThat(kafka.received("depo-topic", DepoDTO.class))
                .extracting(DepoDTO::getName, DepoDTO::isDeleted)
                .containsExactly(
                        tuple("Budapest Dél depó", false),
                        tuple("Budapest Kőbányai depó", false),
                        tuple("Budapest Kőbányai depó", true)));
    }

    /** Transits are private to logistics, the others see them through the prices. */
    @Test
    void transitIsStoredAndPricesFollowItsLifecycle() throws Exception {
        Long from = givenDepo(TestFixtures.budapestDepo(hungary, budapest));
        Long to = givenDepo(TestFixtures.viennaDepo(austria, vienna));
        DepoTransitDTO request = TestFixtures.transit(from, to, small);

        Long id = read(send(post("/api/depo-transits"), request).andExpect(status().isCreated()), DepoTransitDTO.class)
                .getId();
        assertThat(priceFromHungaryToAustria()).get().extracting(CountryPrice::getMaxPrice).isEqualTo(2790.0);

        request.setPrice(3190.0);
        send(put("/api/depo-transits/{id}", id), request).andExpect(status().isOk());
        assertThat(transits.findById(id)).get().extracting("price").isEqualTo(3190.0);
        assertThat(priceFromHungaryToAustria()).get().extracting(CountryPrice::getMaxPrice).isEqualTo(3190.0);

        send(delete("/api/depo-transits/{id}", id), null).andExpect(status().isNoContent());
        assertThat(transits.findById(id)).isEmpty();
        send(delete("/api/depo-transits/{id}", id), null).andExpect(status().isNotFound());
        await().untilAsserted(() -> assertThat(announcedFromHungaryToAustria())
                .extracting(CountryPriceDTO::getMaxPrice, CountryPriceDTO::isDeleted)
                .containsExactly(tuple(2790.0, false), tuple(3190.0, false), tuple(3190.0, true)));
    }

    @Test
    void depoWithCityOfAnotherCountryIsNeitherStoredNorAnnounced() throws Exception {
        Long id = givenDepo(TestFixtures.budapestDepo(hungary, budapest));
        DepoDTO invalid = TestFixtures.budapestDepo(hungary, vienna);

        send(post("/api/depos"), invalid)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("locationCityId"));
        send(put("/api/depos/{id}", id), invalid).andExpect(status().isBadRequest());

        assertThat(depos.findAll()).singleElement().extracting("locationCityId").isEqualTo(budapest);
        assertThat(prices.findAll()).isEmpty();
        kafkaTemplate.flush();
        assertThat(kafka.received("depo-topic")).isEmpty();
        assertThat(kafka.received("country-price-topic")).isEmpty();
    }

    @Test
    void transitWithUnknownDepoIsNotStoredAndPricesAreNotRecalculated() throws Exception {
        Long from = givenDepo(TestFixtures.budapestDepo(hungary, budapest));
        Long to = givenDepo(TestFixtures.viennaDepo(austria, vienna));
        Long id = transits.save(TestFixtures.entity(TestFixtures.transit(from, to, small))).getId();
        DepoTransitDTO invalid = TestFixtures.transit(from, to + 1_000, small);

        send(post("/api/depo-transits"), invalid)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("destinationDepoId"));
        send(put("/api/depo-transits/{id}", id), invalid).andExpect(status().isBadRequest());

        assertThat(transits.findAll()).singleElement().extracting("destinationDepoId").isEqualTo(to);
        // a recalculation would have created the prices of the two depos
        assertThat(prices.findAll()).isEmpty();
        kafkaTemplate.flush();
        assertThat(kafka.received("country-price-topic")).isEmpty();
    }

    @Test
    void updateOfUnknownDepoOrTransitIsNotFoundWithoutValidation() throws Exception {
        send(put("/api/depos/{id}", 404_404), TestFixtures.budapestDepo(505_505L, 505_505L))
                .andExpect(status().isNotFound());
        send(put("/api/depo-transits/{id}", 404_404), TestFixtures.transit(505_505L, 505_505L, 505_505L))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletingTheLastDepoOfACountryRemovesItsPrices() throws Exception {
        Long from = givenDepo(TestFixtures.budapestDepo(hungary, budapest));
        Long to = givenDepo(TestFixtures.viennaDepo(austria, vienna));
        send(post("/api/depo-transits"), TestFixtures.transit(from, to, small)).andExpect(status().isCreated());
        send(post("/api/depo-transits"), TestFixtures.transit(to, from, small)).andExpect(status().isCreated());
        assertThat(priceFromHungaryToAustria()).isPresent();

        send(delete("/api/depos/{id}", to), null).andExpect(status().isNoContent());

        // only the domestic price of the country that still has a depo stays
        assertThat(prices.findAll()).singleElement()
                .extracting(CountryPrice::getOriginCountryId, CountryPrice::getDestinationCountryId)
                .containsExactly(hungary, hungary);
        await().untilAsserted(() -> assertThat(kafka.received("country-price-topic", CountryPriceDTO.class))
                .filteredOn(CountryPriceDTO::isDeleted)
                .extracting(CountryPriceDTO::getOriginCountryId, CountryPriceDTO::getDestinationCountryId)
                .containsExactlyInAnyOrder(
                        tuple(hungary, austria), tuple(austria, hungary), tuple(austria, austria)));
    }

    private Long givenDepo(DepoDTO depo) {
        return depos.save(TestFixtures.entity(depo)).getId();
    }

    private Optional<CountryPrice> priceFromHungaryToAustria() {
        return prices.findByOriginCountryIdAndDestinationCountryIdAndPackageSizeId(hungary, austria, small);
    }

    private List<CountryPriceDTO> announcedFromHungaryToAustria() {
        return kafka.received("country-price-topic", CountryPriceDTO.class).stream()
                .filter(price -> price.getOriginCountryId().equals(hungary)
                        && price.getDestinationCountryId().equals(austria))
                .toList();
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
