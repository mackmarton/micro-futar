package org.bme.micro_futar.payments.services;

import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import org.bme.micro_futar.payments.entities.Payment;
import org.bme.micro_futar.payments.exceptions.PaymentNotFoundException;
import org.bme.micro_futar.payments.exceptions.PaymentNotReadyException;
import org.bme.micro_futar.payments.exceptions.PaymentStateException;
import org.bme.micro_futar.payments.repositories.PaymentRepository;
import org.bme.micro_futar.shared.dtos.PaymentRequestedEventDTO;
import org.bme.micro_futar.shared.dtos.PaymentStatusEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentPricedEventDTO;
import org.bme.micro_futar.shared.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTests {

    private static final Long SHIPMENT_ID = 42L;
    private static final String TOKEN = "token-of-the-sender";

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StripeGateway stripeGateway;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private final PaymentTokenHasher paymentTokenHasher = new PaymentTokenHasher();

    private PaymentService paymentService;
    private Payment payment;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, paymentTokenHasher, stripeGateway, eventPublisher);
        payment = new Payment();
        payment.setShipmentId(SHIPMENT_ID);
        payment.setParcelNumber("parcel-1");
        payment.setSenderEmail("john@example.com");
        payment.setPaymentTokenHash(paymentTokenHasher.hash(TOKEN));
        payment.setMaxAmount(new BigDecimal("3000"));
        payment.setCurrencyCode("HUF");
        payment.setStatus(PaymentStatus.PENDING);
    }

    @Test
    void registersPaymentRequestOnce() {
        PaymentRequestedEventDTO request = PaymentRequestedEventDTO.builder()
                .shipmentId(SHIPMENT_ID).maxAmount(new BigDecimal("3000")).currencyCode("HUF")
                .paymentTokenHash("hash").build();
        when(paymentRepository.existsByShipmentId(SHIPMENT_ID)).thenReturn(false, true);

        paymentService.registerPaymentRequest(request);
        paymentService.registerPaymentRequest(request);

        verify(paymentRepository, times(1)).save(argThat(p ->
                p.getStatus() == PaymentStatus.PENDING && p.getPaymentTokenHash().equals("hash")));
    }

    @Test
    void startCheckoutCreatesSessionForOwner() {
        when(paymentRepository.findByShipmentId(SHIPMENT_ID)).thenReturn(Optional.of(payment));
        Session session = new Session();
        session.setId("cs_1");
        session.setUrl("https://checkout.stripe.com/cs_1");
        when(stripeGateway.createCheckoutSession(eq(payment), any(), eq("hu"))).thenReturn(session);

        var response = paymentService.startCheckout(SHIPMENT_ID, TOKEN, null, "hu");

        assertThat(response.getCheckoutUrl()).isEqualTo("https://checkout.stripe.com/cs_1");
        assertThat(payment.getCheckoutSessionId()).isEqualTo("cs_1");
    }

    @Test
    void startCheckoutReusesOpenSession() {
        payment.setCheckoutSessionId("cs_1");
        payment.setCheckoutUrl("https://checkout.stripe.com/cs_1");
        payment.setCheckoutExpiresAt(ZonedDateTime.now().plusMinutes(30));
        when(paymentRepository.findByShipmentId(SHIPMENT_ID)).thenReturn(Optional.of(payment));

        var response = paymentService.startCheckout(SHIPMENT_ID, TOKEN, null, "hu");

        assertThat(response.getCheckoutUrl()).isEqualTo("https://checkout.stripe.com/cs_1");
        verifyNoInteractions(stripeGateway);
    }

    @Test
    void startCheckoutExpiresSessionAboutToRunOutBeforeCreatingNewOne() {
        payment.setCheckoutSessionId("cs_old");
        payment.setCheckoutUrl("https://checkout.stripe.com/cs_old");
        payment.setCheckoutExpiresAt(ZonedDateTime.now().plusMinutes(2));
        when(paymentRepository.findByShipmentId(SHIPMENT_ID)).thenReturn(Optional.of(payment));
        Session session = new Session();
        session.setId("cs_new");
        session.setUrl("https://checkout.stripe.com/cs_new");
        when(stripeGateway.createCheckoutSession(eq(payment), any(), eq("hu"))).thenReturn(session);

        paymentService.startCheckout(SHIPMENT_ID, TOKEN, null, "hu");

        verify(stripeGateway).expireCheckoutSession("cs_old");
        assertThat(payment.getCheckoutSessionId()).isEqualTo("cs_new");
    }

    @Test
    void startCheckoutRejectsWrongToken() {
        when(paymentRepository.findByShipmentId(SHIPMENT_ID)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.startCheckout(SHIPMENT_ID, "guessed", null, "hu"))
                .isInstanceOf(PaymentNotFoundException.class);
        verifyNoInteractions(stripeGateway);
    }

    @Test
    void loggedInSenderCanStartCheckoutWithoutToken() {
        when(paymentRepository.findByShipmentId(SHIPMENT_ID)).thenReturn(Optional.of(payment));
        Session session = new Session();
        session.setId("cs_1");
        session.setUrl("https://checkout.stripe.com/cs_1");
        when(stripeGateway.createCheckoutSession(eq(payment), any(), eq("en"))).thenReturn(session);

        var response = paymentService.startCheckout(SHIPMENT_ID, null, "John@Example.com", "en");

        assertThat(response.getCheckoutUrl()).isEqualTo("https://checkout.stripe.com/cs_1");
    }

    @Test
    void otherLoggedInUserNeedsToken() {
        when(paymentRepository.findByShipmentId(SHIPMENT_ID)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.getStatus(SHIPMENT_ID, null, "someone@else.com"))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void startCheckoutBeforePaymentRequestArrivedIsNotReady() {
        when(paymentRepository.findByShipmentId(SHIPMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.startCheckout(SHIPMENT_ID, TOKEN, null, "hu"))
                .isInstanceOf(PaymentNotReadyException.class);
    }

    @Test
    void startCheckoutRejectsAlreadyAuthorizedPayment() {
        payment.setStatus(PaymentStatus.AUTHORIZED);
        when(paymentRepository.findByShipmentId(SHIPMENT_ID)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.startCheckout(SHIPMENT_ID, TOKEN, null, "hu"))
                .isInstanceOf(PaymentStateException.class);
    }

    @Test
    void markAuthorizedPublishesStatus() {
        when(paymentRepository.findByShipmentId(SHIPMENT_ID)).thenReturn(Optional.of(payment));

        paymentService.markAuthorized(SHIPMENT_ID, "pi_1", 300_000L);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(payment.getPaymentIntentId()).isEqualTo("pi_1");
        verify(eventPublisher).publishEvent(argThat((PaymentStatusEventDTO e) ->
                e.getStatus() == PaymentStatus.AUTHORIZED && e.getAmount().compareTo(new BigDecimal("3000")) == 0));
    }

    @Test
    void secondAuthorizationIsReleased() {
        payment.setStatus(PaymentStatus.AUTHORIZED);
        payment.setPaymentIntentId("pi_1");
        when(paymentRepository.findByShipmentId(SHIPMENT_ID)).thenReturn(Optional.of(payment));

        paymentService.markAuthorized(SHIPMENT_ID, "pi_2", 300_000L);

        verify(stripeGateway).cancelPaymentIntent("pi_2");
        assertThat(payment.getPaymentIntentId()).isEqualTo("pi_1");
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void captureTakesFinalPrice() {
        payment.setStatus(PaymentStatus.AUTHORIZED);
        payment.setPaymentIntentId("pi_1");
        when(paymentRepository.findByShipmentId(SHIPMENT_ID)).thenReturn(Optional.of(payment));
        PaymentIntent captured = new PaymentIntent();
        captured.setAmountReceived(249_000L);
        when(stripeGateway.capture(payment, new BigDecimal("2490"))).thenReturn(captured);

        paymentService.capture(priced("2490"));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getCapturedAmount()).isEqualByComparingTo("2490");
    }

    @Test
    void captureNeverExceedsAuthorizedAmount() {
        payment.setStatus(PaymentStatus.AUTHORIZED);
        payment.setPaymentIntentId("pi_1");
        when(paymentRepository.findByShipmentId(SHIPMENT_ID)).thenReturn(Optional.of(payment));
        PaymentIntent captured = new PaymentIntent();
        captured.setAmountReceived(300_000L);
        when(stripeGateway.capture(payment, new BigDecimal("3000"))).thenReturn(captured);

        paymentService.capture(priced("3500"));

        verify(stripeGateway).capture(payment, new BigDecimal("3000"));
    }

    @Test
    void captureIsIdempotentOncePaid() {
        payment.setStatus(PaymentStatus.PAID);
        when(paymentRepository.findByShipmentId(SHIPMENT_ID)).thenReturn(Optional.of(payment));

        paymentService.capture(priced("2490"));

        verifyNoInteractions(stripeGateway);
    }

    @Test
    void expiredSessionThatWasReplacedIsIgnored() {
        payment.setCheckoutSessionId("cs_new");
        payment.setCheckoutExpiresAt(ZonedDateTime.now().minusMinutes(1));
        when(paymentRepository.findByShipmentId(SHIPMENT_ID)).thenReturn(Optional.of(payment));

        paymentService.markExpired(SHIPMENT_ID, "cs_old");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void manuallyExpiredSessionBeforeItsExpiryIsIgnored() {
        payment.setCheckoutSessionId("cs_1");
        payment.setCheckoutExpiresAt(ZonedDateTime.now().plusMinutes(3));
        when(paymentRepository.findByShipmentId(SHIPMENT_ID)).thenReturn(Optional.of(payment));

        paymentService.markExpired(SHIPMENT_ID, "cs_1");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void sessionReachingItsExpiryExpiresPayment() {
        payment.setCheckoutSessionId("cs_1");
        payment.setCheckoutExpiresAt(ZonedDateTime.now());
        when(paymentRepository.findByShipmentId(SHIPMENT_ID)).thenReturn(Optional.of(payment));

        paymentService.markExpired(SHIPMENT_ID, "cs_1");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.EXPIRED);
    }

    private static ShipmentPricedEventDTO priced(String amount) {
        return ShipmentPricedEventDTO.builder()
                .shipmentId(SHIPMENT_ID).finalAmount(new BigDecimal(amount)).currencyCode("HUF").build();
    }
}
