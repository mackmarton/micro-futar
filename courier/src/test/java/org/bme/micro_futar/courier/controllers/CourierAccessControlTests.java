package org.bme.micro_futar.courier.controllers;

import org.bme.micro_futar.courier.EnableTestContainers;
import org.bme.micro_futar.courier.config.auth.JwtAuthConverter;
import org.bme.micro_futar.courier.entities.*;
import org.bme.micro_futar.courier.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@EnableTestContainers
class CourierAccessControlTests {

    private static final String OWN_EMAIL = "own@example.com";
    private static final long OWN_COURIER = 9001L;
    private static final long OTHER_COURIER = 9002L;

    private static final long OWN_PICKUP = 9101L;
    private static final long OWN_DELIVERY = 9102L;
    private static final long OTHERS = 9103L;
    private static final long OWN_YESTERDAY = 9104L;
    private static final long OWN_CANCELED = 9105L;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtAuthConverter jwtAuthConverter;
    @Autowired
    private AssignmentProgressRepository assignmentProgressRepository;
    @Autowired
    private CourierRepository courierRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private ShipmentRepository shipmentRepository;
    @Autowired
    private ShipmentRouteRepository shipmentRouteRepository;
    @Autowired
    private ShipmentRouteCourierRepository shipmentRouteCourierRepository;

    @BeforeEach
    void setUp() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(9201L);
        vehicle.setRegistrationNumber("ABC-123");
        vehicle.setMaximumPackableVolume(10.0);
        vehicleRepository.save(vehicle);

        saveCourier(OWN_COURIER, OWN_EMAIL, vehicle.getId());
        saveCourier(OTHER_COURIER, "other@example.com", null);

        Shipment shipment = new Shipment();
        shipment.setId(9301L);
        shipment.setSenderName("Sender Sam");
        shipment.setSenderEmail("sender@example.com");
        shipment.setSenderPhone("+36 1 111");
        shipment.setSenderLatitude(47.1);
        shipment.setSenderLongitude(19.1);
        shipment.setRecipientName("Recipient Rita");
        shipment.setRecipientEmail("recipient@example.com");
        shipment.setRecipientPhone("+36 2 222");
        shipment.setRecipientLatitude(47.2);
        shipment.setRecipientLongitude(19.2);
        shipment.setSenderLocationCountryId(1L);
        shipment.setSenderLocationCityId(1L);
        shipment.setSenderZip("1111");
        shipment.setSenderAddress("Sender street 1");
        shipment.setRecipientLocationCountryId("1");
        shipment.setRecipientLocationCityId("1");
        shipment.setRecipientZip("2222");
        shipment.setRecipientAddress("Recipient street 2");
        shipment.setPackageSizeId(1L);
        shipment.setParcelNumber("MF-1");
        shipment.setPrice(1000.0);
        shipment.setCurrencyCode("HUF");
        shipmentRepository.save(shipment);

        saveRoute(9401L, "Sender street 1", null);
        saveRoute(9402L, null, "Recipient street 2");

        saveAssignment(OWN_PICKUP, OWN_COURIER, 9401L, LocalDate.now(), false);
        saveAssignment(OWN_DELIVERY, OWN_COURIER, 9402L, LocalDate.now(), false);
        saveAssignment(OTHERS, OTHER_COURIER, 9402L, LocalDate.now(), false);
        saveAssignment(OWN_YESTERDAY, OWN_COURIER, 9401L, LocalDate.now().minusDays(1), false);
        saveAssignment(OWN_CANCELED, OWN_COURIER, 9401L, LocalDate.now(), true);
    }

    @Test
    void adminRoleOfAnotherPortalGrantsNothing() throws Exception {
        mockMvc.perform(get("/api/package-sizes").with(token(OWN_EMAIL, "logistics-admin")))
                .andExpect(status().isForbidden());
    }

    @Test
    void commonAdminRoleGrantsAccess() throws Exception {
        mockMvc.perform(get("/api/package-sizes").with(token(OWN_EMAIL, "admin")))
                .andExpect(status().isOk());
    }

    @Test
    void courierKeepsAccessToPackageSizes() throws Exception {
        mockMvc.perform(get("/api/package-sizes").with(token(OWN_EMAIL, "courier-user")))
                .andExpect(status().isOk());
    }

    @Test
    void meReturnsOwnRecordWithVehicle() throws Exception {
        mockMvc.perform(get("/api/me").with(token(OWN_EMAIL, "courier-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(OWN_COURIER))
                .andExpect(jsonPath("$.email").value(OWN_EMAIL))
                .andExpect(jsonPath("$.vehicle.registrationNumber").value("ABC-123"));
    }

    @Test
    void callerWithoutCourierRecordGetsNotFound() throws Exception {
        mockMvc.perform(get("/api/me").with(token("nobody@example.com", "courier-user")))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/me/assignments").with(token("nobody@example.com", "courier-user")))
                .andExpect(status().isNotFound());
    }

    @Test
    void meRequiresVerifiedEmail() throws Exception {
        mockMvc.perform(get("/api/me/assignments").with(token(OWN_EMAIL, false, "courier-user")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("EMAIL_NOT_VERIFIED"));
    }

    @Test
    void listsOnlyOwnActiveAssignmentsOfToday() throws Exception {
        mockMvc.perform(get("/api/me/assignments").with(token(OWN_EMAIL, "courier-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].assignmentId").value(
                        org.hamcrest.Matchers.containsInAnyOrder((int) OWN_PICKUP, (int) OWN_DELIVERY)));
    }

    @ParameterizedTest
    @ValueSource(longs = {OTHERS, OWN_YESTERDAY, OWN_CANCELED, 999999L})
    void assignmentThatIsNotOwnAndActiveIsNotFound(long assignmentId) throws Exception {
        mockMvc.perform(get("/api/me/assignments/" + assignmentId).with(token(OWN_EMAIL, "courier-user")))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/shipment-route-couriers/pickup/9103",
            "/api/shipment-route-couriers/9103/fulfill",
            "/api/shipment-route-couriers/9103/fail",
            "/api/shipment-route-couriers/pickup/999999",
            "/api/shipment-route-couriers/999999/fulfill",
            "/api/shipment-route-couriers/999999/fail"
    })
    void actingOnAnAssignmentThatIsNotOwnIsNotFound(String url) throws Exception {
        mockMvc.perform(post(url).with(token(OWN_EMAIL, "courier-user")))
                .andExpect(status().isNotFound());
        assertFalse(assignmentProgressRepository.existsById(OTHERS));
    }

    @Test
    void deliveryExposesOnlyTheRecipient() throws Exception {
        mockMvc.perform(get("/api/me/assignments/" + OWN_DELIVERY).with(token(OWN_EMAIL, "courier-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignmentType").value("DELIVERY"))
                .andExpect(jsonPath("$.contactName").value("Recipient Rita"))
                .andExpect(jsonPath("$.contactPhone").value("+36 2 222"))
                .andExpect(jsonPath("$.address").value("Recipient street 2"))
                .andExpect(jsonPath("$.parcelNumber").value("MF-1"))
                .andExpect(jsonPath("$.packageSizeId").value(1))
                .andExpect(jsonPath("$.senderName").doesNotExist())
                .andExpect(jsonPath("$.senderPhone").doesNotExist())
                .andExpect(jsonPath("$.senderEmail").doesNotExist())
                .andExpect(jsonPath("$.price").doesNotExist())
                .andExpect(jsonPath("$.maxPrice").doesNotExist())
                .andExpect(jsonPath("$.currencyCode").doesNotExist())
                .andExpect(jsonPath("$.paymentStatus").doesNotExist())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Sender Sam"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("@"))));
    }

    @Test
    void pickupExposesOnlyTheSenderWithoutEmail() throws Exception {
        mockMvc.perform(get("/api/me/assignments/" + OWN_PICKUP).with(token(OWN_EMAIL, "courier-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignmentType").value("PICKUP"))
                .andExpect(jsonPath("$.contactName").value("Sender Sam"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Rita"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("@"))));
    }

    private RequestPostProcessor token(String email, String role) {
        return token(email, true, role);
    }

    /** Authorities come from the real converter, so the mapping of the Keycloak client roles is tested too. */
    private RequestPostProcessor token(String email, boolean verified, String role) {
        return jwt()
                .jwt(token -> token
                        .claim("preferred_username", email)
                        .claim("email", email)
                        .claim("email_verified", verified)
                        .claim("resource_access", Map.of("api-gateway-client", Map.of("roles", List.of(role)))))
                .authorities(token -> jwtAuthConverter.convert(token).getAuthorities());
    }

    private void saveCourier(long id, String email, Long vehicleId) {
        Courier courier = new Courier();
        courier.setId(id);
        courier.setName("Courier " + id);
        courier.setEmail(email);
        courier.setVehicleId(vehicleId);
        courierRepository.save(courier);
    }

    private void saveRoute(long id, String originAddress, String destinationAddress) {
        ShipmentRoute route = new ShipmentRoute();
        route.setId(id);
        route.setShipmentId(9301L);
        route.setOriginAddress(originAddress);
        route.setDestinationAddress(destinationAddress);
        shipmentRouteRepository.save(route);
    }

    private void saveAssignment(long id, long courierId, long routeId, LocalDate date, boolean canceled) {
        ShipmentRouteCourier assignment = new ShipmentRouteCourier();
        assignment.setId(id);
        assignment.setCourierId(courierId);
        assignment.setShipmentRouteId(routeId);
        assignment.setDateAssignedFor(date);
        assignment.setCanceled(canceled);
        shipmentRouteCourierRepository.save(assignment);
    }
}
