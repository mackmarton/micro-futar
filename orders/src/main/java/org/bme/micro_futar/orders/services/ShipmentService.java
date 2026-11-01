package org.bme.micro_futar.orders.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.orders.dtos.CreateShipmentResponseDTO;
import org.bme.micro_futar.orders.entities.Shipment;
import org.bme.micro_futar.orders.exceptions.PriceNotAvailableException;
import org.bme.micro_futar.orders.kafka.ShipmentNotificationProducer;
import org.bme.micro_futar.orders.kafka.ShipmentProducer;
import org.bme.micro_futar.orders.mappers.ShipmentMapper;
import org.bme.micro_futar.orders.repositories.ShipmentRepository;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
import org.bme.micro_futar.shared.dtos.PaymentRequestedEventDTO;
import org.bme.micro_futar.shared.dtos.PaymentStatusEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.dtos.ShipmentNotificationEventDTO;
import org.bme.micro_futar.shared.enums.NotificationType;
import org.bme.micro_futar.shared.enums.PaymentStatus;
import org.bme.micro_futar.shared.utils.PaymentTokens;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {

    private static final String PRICE_CURRENCY_CODE = "HUF";

    private final ShipmentRepository shipmentRepository;
    private final CountryPriceService countryPriceService;
    private final ShipmentMapper shipmentMapper;
    private final ShipmentProducer shipmentProducer;
    private final ShipmentNotificationProducer shipmentNotificationProducer;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Creates the shipment in {@link PaymentStatus#PENDING} state. It is handed to logistics only once the sender
     * authorized the maximum price at checkout (see {@link #applyPaymentStatus}).
     */
    @Transactional
    public CreateShipmentResponseDTO newShipment(ShipmentDTO shipmentDTO) {
        // Price and currency are decided here, never taken from the client.
        CountryPriceDTO countryPrice = countryPriceService.findPriceByCountriesAndSize(
                        shipmentDTO.getSenderLocationCountryId(), shipmentDTO.getRecipientLocationCountryId(),
                        shipmentDTO.getPackageSizeId())
                .filter(price -> !price.isDeleted())
                .orElseThrow(() -> new PriceNotAvailableException(shipmentDTO.getSenderLocationCountryId(),
                        shipmentDTO.getRecipientLocationCountryId(), shipmentDTO.getPackageSizeId()));

        shipmentDTO.setId(null);
        shipmentDTO.setConfirmed(false);
        shipmentDTO.setDelivered(false);
        shipmentDTO.setPrice(null);
        shipmentDTO.setMaxPrice(countryPrice.getMaxPrice());
        shipmentDTO.setCurrencyCode(PRICE_CURRENCY_CODE);
        shipmentDTO.setPaymentStatus(PaymentStatus.PENDING);
        shipmentDTO.setParcelNumber(UUID.randomUUID().toString());
        Shipment savedEntity = shipmentRepository.save(shipmentMapper.toEntity(shipmentDTO));
        ShipmentDTO savedShipmentDTO = shipmentMapper.toDTO(savedEntity);

        String paymentToken = PaymentTokens.generate();
        // Sent to Kafka by PaymentRequestProducer only after this transaction commits.
        eventPublisher.publishEvent(PaymentRequestedEventDTO.builder()
                .shipmentId(savedShipmentDTO.getId())
                .parcelNumber(savedShipmentDTO.getParcelNumber())
                .maxAmount(BigDecimal.valueOf(countryPrice.getMaxPrice()))
                .currencyCode(savedShipmentDTO.getCurrencyCode())
                .senderEmail(savedShipmentDTO.getSenderEmail())
                .paymentTokenHash(PaymentTokens.hash(paymentToken))
                .requestedAt(ZonedDateTime.now())
                .build());
        return new CreateShipmentResponseDTO(savedShipmentDTO, paymentToken);
    }

    /** Mirrors the payment state onto the shipment; redelivered or out-of-date events are ignored. */
    @Transactional
    public void applyPaymentStatus(PaymentStatusEventDTO event) {
        Shipment shipment = shipmentRepository.findById(event.getShipmentId())
                .orElseThrow(() -> new IllegalStateException("No shipment " + event.getShipmentId()));
        PaymentStatus current = shipment.getPaymentStatus();
        PaymentStatus next = event.getStatus();
        if (current == next) {
            return;
        }
        switch (next) {
            case AUTHORIZED -> {
                if (current != PaymentStatus.PENDING) {
                    log.info("Ignoring AUTHORIZED for shipment {} in payment status {}", shipment.getId(), current);
                    return;
                }
                shipment.setPaymentStatus(PaymentStatus.AUTHORIZED);
                shipment.setConfirmed(true);
                ShipmentDTO shipmentDTO = shipmentMapper.toDTO(shipmentRepository.save(shipment));
                // Only now does the shipment become real for logistics and the sender gets the confirmation.
                shipmentProducer.sendShipmentToTopic(shipmentDTO);
                shipmentNotificationProducer.sendNotificationToTopic(
                        ShipmentNotificationEventDTO.forShipment(NotificationType.SHIPMENT_CREATED, shipmentDTO));
            }
            case PAID -> {
                shipment.setPaymentStatus(PaymentStatus.PAID);
                shipment.setPrice(event.getAmount() != null ? event.getAmount().doubleValue() : null);
                shipmentRepository.save(shipment);
            }
            case EXPIRED, FAILED, CANCELED -> {
                if (current == PaymentStatus.PAID || current == PaymentStatus.REFUNDED) {
                    return;
                }
                if (current == PaymentStatus.AUTHORIZED) {
                    // TODO: logistics already planned this shipment; it has to be told to cancel it.
                    log.warn("Payment of shipment {} became {} after it was handed to logistics",
                            shipment.getId(), next);
                }
                shipment.setPaymentStatus(next);
                shipment.setConfirmed(false);
                shipmentRepository.save(shipment);
            }
            case REFUNDED -> {
                shipment.setPaymentStatus(PaymentStatus.REFUNDED);
                shipmentRepository.save(shipment);
            }
            case PENDING -> log.debug("Ignoring PENDING payment status for shipment {}", shipment.getId());
        }
    }

    @Transactional(readOnly = true)
    public List<ShipmentDTO> getShipmentsForUser(String senderEmail) {
        return shipmentRepository.findBySenderEmail(senderEmail).stream()
                .map(shipmentMapper::toDTO)
                .toList();
    }
}
