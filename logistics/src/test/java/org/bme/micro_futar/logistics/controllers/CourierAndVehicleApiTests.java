package org.bme.micro_futar.logistics.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bme.micro_futar.logistics.IntegrationTest;
import org.bme.micro_futar.logistics.entities.ShipmentRoute;
import org.bme.micro_futar.logistics.exceptions.InvalidReferenceException;
import org.bme.micro_futar.logistics.mappers.ShipmentMapper;
import org.bme.micro_futar.logistics.repositories.CourierRepository;
import org.bme.micro_futar.logistics.repositories.DepoRepository;
import org.bme.micro_futar.logistics.repositories.PackageSizeRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteCourierRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteRepository;
import org.bme.micro_futar.logistics.repositories.VehicleRepository;
import org.bme.micro_futar.logistics.services.ShipmentRouteCourierService;
import org.bme.micro_futar.logistics.support.KafkaTestClient;
import org.bme.micro_futar.logistics.support.TestFixtures;
import org.bme.micro_futar.logistics.support.TestJwts;
import org.bme.micro_futar.shared.dtos.CourierDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.bme.micro_futar.shared.dtos.VehicleDTO;
import org.bme.micro_futar.shared.enums.CourierType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Couriers, their vehicles, and the planning that assigns legs to them. */
@IntegrationTest
class CourierAndVehicleApiTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TestJwts testJwts;
    @Autowired private KafkaTestClient kafka;
    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private VehicleRepository vehicles;
    @Autowired private CourierRepository couriers;
    @Autowired private DepoRepository depos;
    @Autowired private PackageSizeRepository packageSizes;
    @Autowired private ShipmentRepository shipments;
    @Autowired private ShipmentMapper shipmentMapper;
    @Autowired private ShipmentRouteRepository routes;
    @Autowired private ShipmentRouteCourierRepository assignments;
    @Autowired private ShipmentRouteCourierService assignmentService;

    @Test
    void vehicleIsStoredAndAnnouncedThroughItsLifecycle() throws Exception {
        VehicleDTO request = TestFixtures.vehicle();
        Long id = read(send(post("/api/vehicles"), request).andExpect(status().isCreated()), VehicleDTO.class).getId();
        request.setRegistrationNumber("AAMT-915");
        send(put("/api/vehicles/{id}", id), request).andExpect(status().isOk());
        assertThat(vehicles.findById(id)).get().extracting("registrationNumber").isEqualTo("AAMT-915");

        send(delete("/api/vehicles/{id}", id), null).andExpect(status().isNoContent());

        assertThat(vehicles.findById(id)).isEmpty();
        await().untilAsserted(() -> assertThat(kafka.received("vehicle-topic", VehicleDTO.class))
                .extracting(VehicleDTO::getRegistrationNumber, VehicleDTO::isDeleted)
                .containsExactly(tuple("AEKR-482", false), tuple("AAMT-915", false), tuple("AAMT-915", true)));
    }

    @Test
    void courierIsStoredAndAnnouncedThroughItsLifecycle() throws Exception {
        CourierDTO request = TestFixtures.courier(givenDepo(), givenVan());
        Long id = read(send(post("/api/couriers"), request).andExpect(status().isCreated()), CourierDTO.class).getId();
        request.setTelephone("+36704418296");
        send(put("/api/couriers/{id}", id), request).andExpect(status().isOk());
        assertThat(couriers.findById(id)).get().extracting("telephone").isEqualTo("+36704418296");

        send(delete("/api/couriers/{id}", id), null).andExpect(status().isNoContent());

        assertThat(couriers.findById(id)).isEmpty();
        await().untilAsserted(() -> assertThat(kafka.received("courier-topic", CourierDTO.class))
                .extracting(CourierDTO::getTelephone, CourierDTO::isDeleted)
                .containsExactly(
                        tuple("+36209184736", false), tuple("+36704418296", false), tuple("+36704418296", true)));
    }

    @Test
    void courierWithUnknownDepoIsRejectedNamingTheField() throws Exception {
        Long depo = givenDepo();
        Long van = givenVan();
        Long id = couriers.save(TestFixtures.entity(TestFixtures.courier(depo, van))).getId();
        CourierDTO invalid = TestFixtures.courier(depo + 1_000, van);

        send(post("/api/couriers"), invalid)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("depoId"))
                .andExpect(jsonPath("$.detail")
                        .value("Depo " + (depo + 1_000) + " (depoId) does not exist or has been deleted"));
        send(put("/api/couriers/{id}", id), invalid).andExpect(status().isBadRequest());

        assertThat(couriers.findAll()).singleElement().extracting("depoId").isEqualTo(depo);
        kafkaTemplate.flush();
        assertThat(kafka.received("courier-topic")).isEmpty();
    }

    @Test
    void updateOfUnknownCourierIsNotFoundWithoutValidation() throws Exception {
        send(put("/api/couriers/{id}", 404_404), TestFixtures.courier(505_505L, 505_505L))
                .andExpect(status().isNotFound());
    }

    /** Assignments have no write endpoint, so the planning's way in is called directly. */
    @Test
    void assignmentWithUnknownCourierIsNeitherStoredNorAnnounced() {
        ShipmentRoute route = routes.save(TestFixtures.route(1L, 0));
        ShipmentRouteCourierDTO invalid = ShipmentRouteCourierDTO.builder()
                .courierId(404_404L)
                .shipmentRouteId(route.getId())
                .dateAssignedFor(LocalDate.now())
                .build();

        assertThatThrownBy(() -> assignmentService.save(invalid)).isInstanceOf(InvalidReferenceException.class);

        assertThat(assignments.findAll()).isEmpty();
        kafkaTemplate.flush();
        assertThat(kafka.received("shipment-route-courier-topic")).isEmpty();
    }

    @Test
    void planningAssignsTheOpenLegAndAnnouncesTheAssignment() throws Exception {
        Long depo = givenDepo();
        Long courier = couriers.save(TestFixtures.entity(TestFixtures.courier(depo, givenVan()))).getId();
        givenShipment(1L);
        ShipmentRoute pickup = TestFixtures.route(1L, 0);
        pickup.setOriginAddress("Magyarország 1092 Budapest Ráday utca 31.");
        pickup.setDestinationDepoId(depo);
        Long leg = routes.save(pickup).getId();

        send(post("/api/planning/depo/{depoId}/assign-shipments", depo), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedCount").value(1));

        assertThat(assignments.findAll()).singleElement().satisfies(assignment -> {
            assertThat(assignment.getCourierId()).isEqualTo(courier);
            assertThat(assignment.getShipmentRouteId()).isEqualTo(leg);
            assertThat(assignment.getDateAssignedFor()).isEqualTo(LocalDate.now());
        });
        await().untilAsserted(() -> assertThat(
                kafka.received("shipment-route-courier-topic", ShipmentRouteCourierDTO.class))
                .singleElement().extracting(ShipmentRouteCourierDTO::getShipmentRouteId).isEqualTo(leg));
    }

    @Test
    void crossDepoPlanningAssignsTheLegBetweenDepos() throws Exception {
        Long depo = givenDepo();
        CourierDTO trucker = TestFixtures.courier(depo, givenVan());
        trucker.setCourierType(CourierType.CROSS_DEPO);
        Long courier = couriers.save(TestFixtures.entity(trucker)).getId();
        givenShipment(1L);
        ShipmentRoute pickup = TestFixtures.route(1L, 0);
        pickup.setDestinationDepoId(depo);
        pickup.setFulfillmentTime(ZonedDateTime.now());
        routes.save(pickup);
        ShipmentRoute crossDepo = TestFixtures.route(1L, 1);
        crossDepo.setOriginDepoId(depo);
        crossDepo.setDestinationDepoId(depo + 1_000);
        Long leg = routes.save(crossDepo).getId();

        send(post("/api/planning/depo/{depoId}/assign-cross-depo-shipments", depo), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedCount").value(1));

        assertThat(assignments.findAll()).singleElement()
                .extracting("courierId", "shipmentRouteId").containsExactly(courier, leg);
    }

    private Long givenDepo() {
        return depos.save(TestFixtures.entity(TestFixtures.budapestDepo(null, null))).getId();
    }

    private Long givenVan() {
        return vehicles.save(TestFixtures.entity(TestFixtures.vehicle())).getId();
    }

    private void givenShipment(long id) {
        Long small = packageSizes.save(TestFixtures.entity(TestFixtures.smallPackage().build())).getId();
        shipments.save(shipmentMapper.toEntity(TestFixtures.shipment(id).packageSizeId(small).build()));
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
