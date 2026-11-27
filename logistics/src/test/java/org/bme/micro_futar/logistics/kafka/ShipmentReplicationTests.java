package org.bme.micro_futar.logistics.kafka;

import org.bme.micro_futar.logistics.IntegrationTest;
import org.bme.micro_futar.logistics.entities.Shipment;
import org.bme.micro_futar.logistics.entities.ShipmentPricing;
import org.bme.micro_futar.logistics.entities.ShipmentRoute;
import org.bme.micro_futar.logistics.repositories.CountryBaseFeeRepository;
import org.bme.micro_futar.logistics.repositories.DepoRepository;
import org.bme.micro_futar.logistics.repositories.DepoTransitRepository;
import org.bme.micro_futar.logistics.repositories.LocationCityRepository;
import org.bme.micro_futar.logistics.repositories.LocationCountryRepository;
import org.bme.micro_futar.logistics.repositories.LocationRegionRepository;
import org.bme.micro_futar.logistics.repositories.PackageSizeRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentPricingRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteRepository;
import org.bme.micro_futar.logistics.support.KafkaTestClient;
import org.bme.micro_futar.logistics.support.TestFixtures;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.dtos.ShipmentPricedEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/** Logistics keeps a copy of the shipments of orders, and plans and prices the confirmed ones. */
@IntegrationTest
class ShipmentReplicationTests {

    private static final long SHIPMENT_ID = 41L;

    @Autowired private KafkaTestClient kafka;
    @Autowired private LocationRegionRepository regions;
    @Autowired private LocationCountryRepository countries;
    @Autowired private LocationCityRepository cities;
    @Autowired private PackageSizeRepository packageSizes;
    @Autowired private DepoRepository depos;
    @Autowired private DepoTransitRepository transits;
    @Autowired private CountryBaseFeeRepository baseFees;
    @Autowired private ShipmentRepository shipments;
    @Autowired private ShipmentRouteRepository routes;
    @Autowired private ShipmentPricingRepository pricings;

    private ShipmentDTO shipment;
    private Long budapestDepo;
    private Long viennaDepo;

    /** A base fee of 1500 Ft in Hungary and a transit of 2790 Ft from Budapest to Vienna. */
    @BeforeEach
    void givenTheNetwork() {
        Long region = regions.save(TestFixtures.entity(TestFixtures.centralEurope().build())).getId();
        Long hungary = countries.save(TestFixtures.entity(TestFixtures.hungary(region).build())).getId();
        Long austria = countries.save(TestFixtures.entity(TestFixtures.austria(region).build())).getId();
        Long budapest = cities.save(TestFixtures.entity(TestFixtures.budapest(hungary).build())).getId();
        Long vienna = cities.save(TestFixtures.entity(TestFixtures.vienna(austria).build())).getId();
        Long small = packageSizes.save(TestFixtures.entity(TestFixtures.smallPackage().build())).getId();
        budapestDepo = depos.save(TestFixtures.entity(TestFixtures.budapestDepo(hungary, budapest))).getId();
        viennaDepo = depos.save(TestFixtures.entity(TestFixtures.viennaDepo(austria, vienna))).getId();
        transits.save(TestFixtures.entity(TestFixtures.transit(budapestDepo, viennaDepo, small)));
        baseFees.save(TestFixtures.entity(TestFixtures.baseFee(hungary, small)));

        shipment = TestFixtures.shipment(SHIPMENT_ID)
                .senderLocationCountryId(hungary)
                .senderLocationCityId(budapest)
                .recipientLocationCountryId(austria)
                .recipientLocationCityId(vienna)
                .packageSizeId(small)
                .build();
    }

    @Test
    void confirmedShipmentIsStoredPlannedAndPriced() {
        publish(shipment);

        assertThat(shipments.findById(SHIPMENT_ID)).get().extracting(Shipment::isConfirmed).isEqualTo(true);
        assertThat(storedRoute())
                .extracting(ShipmentRoute::getRoutePartNumber, ShipmentRoute::getOriginDepoId,
                        ShipmentRoute::getDestinationDepoId, ShipmentRoute::getOriginAddress,
                        ShipmentRoute::getDestinationAddress)
                .containsExactly(
                        tuple(0, null, budapestDepo, "Magyarország 1092 Budapest Ráday utca 31.", null),
                        tuple(1, budapestDepo, viennaDepo, null, null),
                        tuple(2, viennaDepo, null, null, "Ausztria 1070 Bécs Neubaugasse 25"));
        assertThat(pricings.findById(SHIPMENT_ID)).get().satisfies(pricing -> {
            assertThat(pricing.getFinalPrice()).isEqualByComparingTo("4290");
            assertThat(pricing.getCurrencyCode()).isEqualTo("HUF");
        });
        assertThat(kafka.received("shipment-route-topic", ShipmentRouteDTO.class)).hasSize(3);
        assertThat(kafka.received("shipment-priced-topic", ShipmentPricedEventDTO.class)).singleElement()
                .satisfies(priced -> {
                    assertThat(priced.getShipmentId()).isEqualTo(SHIPMENT_ID);
                    assertThat(priced.getFinalAmount()).isEqualByComparingTo("4290");
                    assertThat(priced.getCurrencyCode()).isEqualTo("HUF");
                });
    }

    @Test
    void unconfirmedShipmentIsOnlyStored() {
        shipment.setConfirmed(false);

        publish(shipment);

        assertThat(shipments.findById(SHIPMENT_ID)).isPresent();
        assertNothingPlannedOrPriced();
    }

    @Test
    void confirmationPlansThePreviouslyStoredShipment() {
        shipment.setConfirmed(false);
        publish(shipment);
        shipment.setConfirmed(true);

        publish(shipment);

        assertThat(routes.findByShipmentId(SHIPMENT_ID)).hasSize(3);
        assertThat(pricings.findById(SHIPMENT_ID)).isPresent();
    }

    /** Shipments from before online payment have nothing to capture. */
    @Test
    void shipmentWithoutAuthorizedMaximumIsPlannedButNotPriced() {
        shipment.setMaxPrice(null);

        publish(shipment);

        assertThat(routes.findByShipmentId(SHIPMENT_ID)).hasSize(3);
        assertThat(pricings.findAll()).isEmpty();
        assertThat(kafka.received("shipment-priced-topic")).isEmpty();
    }

    @Test
    void redeliveredShipmentIsPlannedAndPricedOnce() {
        publish(shipment);

        publish(shipment);

        assertThat(routes.findByShipmentId(SHIPMENT_ID)).hasSize(3);
        assertThat(kafka.received("shipment-route-topic")).hasSize(3);
        assertThat(kafka.received("shipment-priced-topic")).hasSize(1);
    }

    @Test
    void deliveryUpdateKeepsTheStoredFinalPrice() {
        publish(shipment);
        ShipmentPricing first = pricings.findById(SHIPMENT_ID).orElseThrow();
        shipment.setDelivered(true);
        shipment.setPrice(null);

        publish(shipment);

        assertThat(shipments.findById(SHIPMENT_ID)).get().extracting(Shipment::isDelivered).isEqualTo(true);
        assertThat(pricings.findById(SHIPMENT_ID)).get().isEqualTo(first);
        assertThat(kafka.received("shipment-priced-topic")).hasSize(1);
    }

    /** Orders is the only producer of the shipment topic. */
    @Test
    void logisticsNeverRepublishesTheShipment() {
        publish(shipment);

        assertThat(kafka.received("shipment-topic")).hasSize(1);
    }

    private void publish(ShipmentDTO message) {
        kafka.sendAndAwaitConsumption("shipment-topic", message.getId().toString(), message);
    }

    private List<ShipmentRoute> storedRoute() {
        return routes.findByShipmentId(SHIPMENT_ID).stream()
                .sorted(Comparator.comparing(ShipmentRoute::getRoutePartNumber))
                .toList();
    }

    private void assertNothingPlannedOrPriced() {
        assertThat(routes.findAll()).isEmpty();
        assertThat(pricings.findAll()).isEmpty();
        assertThat(kafka.received("shipment-route-topic")).isEmpty();
        assertThat(kafka.received("shipment-priced-topic")).isEmpty();
    }
}
