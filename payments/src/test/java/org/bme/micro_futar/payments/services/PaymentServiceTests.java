package org.bme.micro_futar.payments.services;

import com.stripe.model.checkout.Session;
import org.bme.micro_futar.payments.entities.Payment;
import org.bme.micro_futar.payments.repositories.PaymentRepository;
import org.bme.micro_futar.payments.support.TestFixtures;
import org.bme.micro_futar.shared.enums.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** The time windows and late events the integration tests do not reach. */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTests {

    @Mock private PaymentRepository paymentRepository;
    @Mock private StripeGateway stripeGateway;
    @Mock private ApplicationEventPublisher eventPublisher;

    @Test
    void startCheckoutExpiresSessionAboutToRunOutBeforeCreatingNewOne() {
        Payment payment = givenCheckout(TestFixtures.pendingPayment(), "cs_old", ZonedDateTime.now().plusMinutes(2));
        Session session = new Session();
        session.setId("cs_new");
        session.setUrl("https://checkout.stripe.com/c/pay/cs_new");
        when(stripeGateway.createCheckoutSession(eq(payment), any(), eq("hu"))).thenReturn(session);

        service().startCheckout(TestFixtures.SHIPMENT_ID, TestFixtures.PAYMENT_TOKEN, null, "hu");

        verify(stripeGateway).expireCheckoutSession("cs_old");
        assertThat(payment.getCheckoutSessionId()).isEqualTo("cs_new");
    }

    @Test
    void expiredSessionThatWasReplacedIsIgnored() {
        Payment payment = givenCheckout(TestFixtures.pendingPayment(), "cs_new", ZonedDateTime.now().minusMinutes(1));

        service().markExpired(TestFixtures.SHIPMENT_ID, "cs_old");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void manuallyExpiredSessionBeforeItsExpiryIsIgnored() {
        Payment payment = givenCheckout(TestFixtures.pendingPayment(), "cs_1", ZonedDateTime.now().plusMinutes(3));

        service().markExpired(TestFixtures.SHIPMENT_ID, "cs_1");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        verifyNoInteractions(eventPublisher);
    }

    /** The usual order: the capture already marked it paid when Stripe's success event arrives. */
    @Test
    void successOfAnAlreadyPaidPaymentIsNotAnnouncedAgain() {
        Payment payment = given(TestFixtures.authorizedPayment());
        payment.setStatus(PaymentStatus.PAID);

        service().markPaid(TestFixtures.SHIPMENT_ID, 429_000L);

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void cancellationOfAReleasedExtraAuthorizationIsIgnored() {
        Payment payment = given(TestFixtures.authorizedPayment());

        service().markCanceled(TestFixtures.SHIPMENT_ID, "pi_3QkTB2Lx2eZvKYlo0hYt6GsN");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void lateCancellationOfAPaidPaymentIsIgnored() {
        Payment payment = given(TestFixtures.authorizedPayment());
        payment.setStatus(PaymentStatus.PAID);

        service().markCanceled(TestFixtures.SHIPMENT_ID, payment.getPaymentIntentId());

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        verifyNoInteractions(eventPublisher);
    }

    private PaymentService service() {
        return new PaymentService(paymentRepository, new PaymentTokenHasher(), stripeGateway, eventPublisher);
    }

    private Payment given(Payment payment) {
        when(paymentRepository.findByShipmentId(TestFixtures.SHIPMENT_ID)).thenReturn(Optional.of(payment));
        return payment;
    }

    private Payment givenCheckout(Payment payment, String sessionId, ZonedDateTime expiresAt) {
        payment.setCheckoutSessionId(sessionId);
        payment.setCheckoutUrl("https://checkout.stripe.com/c/pay/" + sessionId);
        payment.setCheckoutExpiresAt(expiresAt);
        return given(payment);
    }
}
