package org.bme.micro_futar.payments.services;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCaptureParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.payments.entities.Payment;
import org.bme.micro_futar.payments.exceptions.StripeOperationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Locale;

/** Thin wrapper around the Stripe API calls, so the payment logic can be unit tested without Stripe. */
@Component
@RequiredArgsConstructor
public class StripeGateway {

    /** Stripe's minimum is 30 minutes; after that the session expires and the shipment is cancelled. */
    static final Duration CHECKOUT_TTL = Duration.ofMinutes(60);
    static final String SHIPMENT_ID_METADATA = "shipmentId";

    private final StripeClient stripeClient;

    @Value("${url.client-ui}")
    private String clientUiUrl;

    public Session createCheckoutSession(Payment payment, ZonedDateTime expiresAt, String locale) {
        String shipmentId = payment.getShipmentId().toString();
        boolean english = "en".equalsIgnoreCase(locale);
        String resultUrl = clientUiUrl + (english ? "/en" : "") + "/portal/payment?shipmentId=" + shipmentId;
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setClientReferenceId(shipmentId)
                .putMetadata(SHIPMENT_ID_METADATA, shipmentId)
                .setCustomerEmail(payment.getSenderEmail())
                .setLocale(english ? SessionCreateParams.Locale.EN : SessionCreateParams.Locale.HU)
                .setExpiresAt(expiresAt.toEpochSecond())
                .setSuccessUrl(resultUrl + "&outcome=success")
                .setCancelUrl(resultUrl + "&outcome=cancel")
                .setPaymentIntentData(SessionCreateParams.PaymentIntentData.builder()
                        // Only authorize now; logistics sets the final price and we capture that later.
                        .setCaptureMethod(SessionCreateParams.PaymentIntentData.CaptureMethod.MANUAL)
                        .putMetadata(SHIPMENT_ID_METADATA, shipmentId)
                        .build())
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(payment.getCurrencyCode().toLowerCase(Locale.ROOT))
                                .setUnitAmount(StripeAmounts.toMinorUnits(payment.getMaxAmount(),
                                        payment.getCurrencyCode()))
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("micro-futár shipment " + payment.getParcelNumber())
                                        .build())
                                .build())
                        .build())
                .build();
        try {
            return stripeClient.v1().checkout().sessions().create(params);
        } catch (StripeException e) {
            throw new StripeOperationException("Failed to create checkout session for shipment " + shipmentId, e);
        }
    }

    /** Makes an abandoned session unusable, so the sender cannot authorize twice. */
    public void expireCheckoutSession(String checkoutSessionId) {
        try {
            stripeClient.v1().checkout().sessions().expire(checkoutSessionId);
        } catch (StripeException e) {
            throw new StripeOperationException("Failed to expire checkout session " + checkoutSessionId, e);
        }
    }

    /** Releases an authorization without charging the card. */
    public void cancelPaymentIntent(String paymentIntentId) {
        try {
            stripeClient.v1().paymentIntents().cancel(paymentIntentId);
        } catch (StripeException e) {
            throw new StripeOperationException("Failed to cancel payment intent " + paymentIntentId, e);
        }
    }

    public PaymentIntent capture(Payment payment, BigDecimal amount) {
        PaymentIntentCaptureParams params = PaymentIntentCaptureParams.builder()
                .setAmountToCapture(StripeAmounts.toMinorUnits(amount, payment.getCurrencyCode()))
                .build();
        RequestOptions options = RequestOptions.builder()
                .setIdempotencyKey("capture-shipment-" + payment.getShipmentId())
                .build();
        try {
            return stripeClient.v1().paymentIntents().capture(payment.getPaymentIntentId(), params, options);
        } catch (StripeException e) {
            throw new StripeOperationException("Failed to capture payment for shipment " + payment.getShipmentId(), e);
        }
    }
}
