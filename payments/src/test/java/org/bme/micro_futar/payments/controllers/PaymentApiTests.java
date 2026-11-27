package org.bme.micro_futar.payments.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.checkout.Session;
import org.bme.micro_futar.payments.IntegrationTest;
import org.bme.micro_futar.payments.dtos.CheckoutRequestDTO;
import org.bme.micro_futar.payments.entities.Payment;
import org.bme.micro_futar.payments.exceptions.StripeOperationException;
import org.bme.micro_futar.payments.repositories.PaymentRepository;
import org.bme.micro_futar.payments.services.StripeGateway;
import org.bme.micro_futar.payments.support.TestFixtures;
import org.bme.micro_futar.payments.support.TestJwts;
import org.bme.micro_futar.shared.utils.PaymentTokens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class PaymentApiTests {

    private static final String CHECKOUT_URL = "https://checkout.stripe.com/c/pay/cs_test_a1Qm7";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TestJwts testJwts;
    @Autowired private PaymentRepository payments;
    @Autowired private StripeGateway stripeGateway;

    @Test
    void guestStartsCheckoutWithThePaymentToken() throws Exception {
        payments.save(TestFixtures.pendingPayment());
        givenStripeSession("hu");

        mockMvc.perform(checkout(TestFixtures.PAYMENT_TOKEN, "hu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkoutUrl").value(CHECKOUT_URL));

        assertThat(stored().getCheckoutSessionId()).isEqualTo("cs_test_a1Qm7");
    }

    @Test
    void verifiedSenderStartsCheckoutWithoutAToken() throws Exception {
        payments.save(TestFixtures.pendingPayment());
        givenStripeSession("en");

        mockMvc.perform(checkout(null, "en").with(testJwts.user("Kovacs.Eszter@Postafiok.hu")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkoutUrl").value(CHECKOUT_URL));
    }

    @Test
    void secondCheckoutReusesTheOpenSession() throws Exception {
        payments.save(TestFixtures.pendingPayment());
        givenStripeSession("hu");
        mockMvc.perform(checkout(TestFixtures.PAYMENT_TOKEN, "hu")).andExpect(status().isOk());

        mockMvc.perform(checkout(TestFixtures.PAYMENT_TOKEN, "hu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkoutUrl").value(CHECKOUT_URL));

        verify(stripeGateway, times(1)).createCheckoutSession(any(), any(), any());
    }

    @Test
    void wrongTokenIsRejected() throws Exception {
        payments.save(TestFixtures.pendingPayment());

        mockMvc.perform(checkout(PaymentTokens.generate(), "hu")).andExpect(status().isNotFound());

        verifyNoInteractions(stripeGateway);
    }

    @Test
    void tokenOfAnotherShipmentIsRejected() throws Exception {
        payments.save(TestFixtures.pendingPayment());
        String otherToken = PaymentTokens.generate();
        Payment other = TestFixtures.pendingPayment();
        other.setShipmentId(TestFixtures.SHIPMENT_ID + 1);
        other.setSenderEmail("nagy.balint@postafiok.hu");
        other.setPaymentTokenHash(PaymentTokens.hash(otherToken));
        payments.save(other);

        mockMvc.perform(checkout(otherToken, "hu")).andExpect(status().isNotFound());

        verifyNoInteractions(stripeGateway);
    }

    @Test
    void otherLoggedInUserIsRejected() throws Exception {
        payments.save(TestFixtures.pendingPayment());

        mockMvc.perform(checkout(null, "hu").with(testJwts.user("nagy.balint@postafiok.hu")))
                .andExpect(status().isNotFound());

        verifyNoInteractions(stripeGateway);
    }

    @Test
    void unverifiedEmailDoesNotProveOwnership() throws Exception {
        payments.save(TestFixtures.pendingPayment());

        mockMvc.perform(checkout(null, "hu").with(testJwts.unverifiedUser(TestFixtures.SENDER_EMAIL)))
                .andExpect(status().isNotFound());
        mockMvc.perform(statusRequest(null).with(testJwts.unverifiedUser(TestFixtures.SENDER_EMAIL)))
                .andExpect(status().isNotFound());

        verifyNoInteractions(stripeGateway);
    }

    @Test
    void unverifiedSenderCanStillUseThePaymentToken() throws Exception {
        payments.save(TestFixtures.pendingPayment());

        mockMvc.perform(statusRequest(TestFixtures.PAYMENT_TOKEN)
                        .with(testJwts.unverifiedUser(TestFixtures.SENDER_EMAIL)))
                .andExpect(status().isOk());
    }

    @Test
    void checkoutBeforeThePaymentRequestArrivedIsNotReady() throws Exception {
        mockMvc.perform(checkout(TestFixtures.PAYMENT_TOKEN, "hu")).andExpect(status().isConflict());

        verifyNoInteractions(stripeGateway);
    }

    @Test
    void authorizedPaymentCannotBeCheckedOutAgain() throws Exception {
        payments.save(TestFixtures.authorizedPayment());

        mockMvc.perform(checkout(TestFixtures.PAYMENT_TOKEN, "hu")).andExpect(status().isConflict());

        verifyNoInteractions(stripeGateway);
    }

    @Test
    void stripeFailureIsReportedAsBadGateway() throws Exception {
        payments.save(TestFixtures.pendingPayment());
        when(stripeGateway.createCheckoutSession(any(), any(), any()))
                .thenThrow(new StripeOperationException("Stripe is unreachable", null));

        mockMvc.perform(checkout(TestFixtures.PAYMENT_TOKEN, "hu")).andExpect(status().isBadGateway());

        assertThat(stored().getCheckoutSessionId()).isNull();
    }

    @Test
    void guestReadsTheStatusWithThePaymentToken() throws Exception {
        payments.save(TestFixtures.authorizedPayment());

        mockMvc.perform(statusRequest(TestFixtures.PAYMENT_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shipmentId").value(TestFixtures.SHIPMENT_ID))
                .andExpect(jsonPath("$.status").value("AUTHORIZED"))
                .andExpect(jsonPath("$.maxAmount").value(4990.0))
                .andExpect(jsonPath("$.currencyCode").value("HUF"));
    }

    @Test
    void verifiedSenderReadsTheStatusWithoutAToken() throws Exception {
        payments.save(TestFixtures.pendingPayment());

        mockMvc.perform(statusRequest(null).with(testJwts.user(TestFixtures.SENDER_EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void statusWithoutAnyProofIsNotFound() throws Exception {
        payments.save(TestFixtures.pendingPayment());

        mockMvc.perform(statusRequest(null)).andExpect(status().isNotFound());
    }

    @Test
    void malformedBearerTokenIsUnauthorized() throws Exception {
        payments.save(TestFixtures.pendingPayment());

        mockMvc.perform(statusRequest(TestFixtures.PAYMENT_TOKEN)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer lejart-token"))
                .andExpect(status().isUnauthorized());
    }

    private void givenStripeSession(String locale) {
        Session session = new Session();
        session.setId("cs_test_a1Qm7");
        session.setUrl(CHECKOUT_URL);
        when(stripeGateway.createCheckoutSession(any(), any(), eq(locale))).thenReturn(session);
    }

    private MockHttpServletRequestBuilder checkout(String paymentToken, String locale) throws Exception {
        var request = new CheckoutRequestDTO(TestFixtures.SHIPMENT_ID, paymentToken, locale);
        return post("/api/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request));
    }

    private static MockHttpServletRequestBuilder statusRequest(String paymentToken) {
        var request = get("/api/status/{shipmentId}", TestFixtures.SHIPMENT_ID);
        return paymentToken == null ? request : request.header(PaymentController.PAYMENT_TOKEN_HEADER, paymentToken);
    }

    private Payment stored() {
        return payments.findByShipmentId(TestFixtures.SHIPMENT_ID).orElseThrow();
    }
}
