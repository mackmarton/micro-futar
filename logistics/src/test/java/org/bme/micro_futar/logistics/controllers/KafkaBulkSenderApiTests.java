package org.bme.micro_futar.logistics.controllers;

import org.bme.micro_futar.logistics.IntegrationTest;
import org.bme.micro_futar.logistics.entities.CountryPrice;
import org.bme.micro_futar.logistics.repositories.CountryPriceRepository;
import org.bme.micro_futar.logistics.repositories.CourierRepository;
import org.bme.micro_futar.logistics.repositories.CurrencyRepository;
import org.bme.micro_futar.logistics.repositories.DepoRepository;
import org.bme.micro_futar.logistics.repositories.LocationCityRepository;
import org.bme.micro_futar.logistics.repositories.LocationCountryRepository;
import org.bme.micro_futar.logistics.repositories.LocationRegionRepository;
import org.bme.micro_futar.logistics.repositories.PackageSizeRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteCourierRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteRepository;
import org.bme.micro_futar.logistics.repositories.VehicleRepository;
import org.bme.micro_futar.logistics.support.KafkaTestClient;
import org.bme.micro_futar.logistics.support.TestFixtures;
import org.bme.micro_futar.logistics.support.TestJwts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Re-publishing the current state, for a new or rebuilt consumer. */
@IntegrationTest
class KafkaBulkSenderApiTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private TestJwts testJwts;
    @Autowired private KafkaTestClient kafka;
    @Autowired private LocationRegionRepository regions;
    @Autowired private LocationCountryRepository countries;
    @Autowired private LocationCityRepository cities;
    @Autowired private CountryPriceRepository prices;
    @Autowired private PackageSizeRepository packageSizes;
    @Autowired private DepoRepository depos;
    @Autowired private CourierRepository couriers;
    @Autowired private VehicleRepository vehicles;
    @Autowired private ShipmentRouteRepository routes;
    @Autowired private ShipmentRouteCourierRepository assignments;
    @Autowired private CurrencyRepository currencies;

    @Test
    void sendAllPublishesEveryEntityTypeToItsTopic() throws Exception {
        CountryPrice price = new CountryPrice();
        price.setOriginCountryId(TestFixtures.HUNGARY_ID);
        price.setDestinationCountryId(TestFixtures.AUSTRIA_ID);
        price.setPackageSizeId(TestFixtures.SMALL_PACKAGE_ID);
        price.setMinPrice(3490.0);
        price.setMaxPrice(4990.0);
        Long region = regions.save(TestFixtures.entity(TestFixtures.centralEurope().build())).getId();
        Long country = countries.save(TestFixtures.entity(TestFixtures.hungary(region).build())).getId();
        Long city = cities.save(TestFixtures.entity(TestFixtures.budapest(country).build())).getId();
        // topic to the id of the one entity of its type; the seeded forint is the currency
        Map<String, Long> expected = Map.ofEntries(
                Map.entry("location-region-topic", region),
                Map.entry("location-country-topic", country),
                Map.entry("location-city-topic", city),
                Map.entry("country-price-topic", prices.save(price).getId()),
                Map.entry("package-size-topic",
                        packageSizes.save(TestFixtures.entity(TestFixtures.smallPackage().build())).getId()),
                Map.entry("depo-topic",
                        depos.save(TestFixtures.entity(TestFixtures.budapestDepo(null, null))).getId()),
                Map.entry("courier-topic",
                        couriers.save(TestFixtures.entity(TestFixtures.courier(null, null))).getId()),
                Map.entry("vehicle-topic", vehicles.save(TestFixtures.entity(TestFixtures.vehicle())).getId()),
                Map.entry("shipment-route-topic", routes.save(TestFixtures.route(1L, 0)).getId()),
                Map.entry("shipment-route-courier-topic",
                        assignments.save(TestFixtures.assignment(1L, LocalDate.now())).getId()),
                Map.entry("currency-topic", currencies.findAll().getFirst().getId()));

        mockMvc.perform(post("/api/kafka/bulk/send-all")
                        .with(testJwts.user(TestFixtures.ADMIN_EMAIL, "logistics-user", "admin")))
                .andExpect(status().isOk());

        await().untilAsserted(() -> expected.forEach((topic, id) ->
                assertThat(kafka.received(topic)).as(topic).extracting(record -> record.key())
                        .containsExactly(id.toString())));
    }

    @Test
    void bulkSendRequiresAdmin() throws Exception {
        mockMvc.perform(post("/api/kafka/bulk/send-all")
                        .with(testJwts.user(TestFixtures.DISPATCHER_EMAIL, "logistics-user")))
                .andExpect(status().isForbidden());
    }
}
