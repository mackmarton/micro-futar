package org.bme.micro_futar.orders.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.orders.dtos.CreateShipmentResponseDTO;
import org.bme.micro_futar.orders.entities.Shipment;
import org.bme.micro_futar.orders.exceptions.LocationValidationException.Party;
import org.bme.micro_futar.orders.exceptions.PriceNotAvailableException;
import org.bme.micro_futar.orders.kafka.ShipmentNotificationProducer;
import org.bme.micro_futar.orders.kafka.ShipmentProducer;
import org.bme.micro_futar.orders.mappers.ShipmentMapper;
import org.bme.micro_futar.orders.repositories.ShipmentRepository;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
import org.bme.micro_futar.shared.dtos.PaymentRequestedEventDTO;
import org.bme.micro_futar.shared.dtos.PaymentStatusEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.dtos.ShipmentDeliveredEventDTO;
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
import java.util.Optional;
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
    private final LocationCheckService locationCheckService;

    /**
     * Creates the shipment in {@link PaymentStatus#PENDING} state. It is handed to logistics only once the sender
     * authorized the maximum price at checkout (see {@link #applyPaymentStatus}).
     */
    @Transactional
    public CreateShipmentResponseDTO newShipment(ShipmentDTO shipmentDTO) {
        // Coordinates come from the browser (a draggable map marker), so they must fit the chosen city.
        locationCheckService.check(Party.SENDER, shipmentDTO.getSenderLocationCountryId(),
                shipmentDTO.getSenderLocationCityId(), shipmentDTO.getSenderLatitude(), shipmentDTO.getSenderLongitude());
        locationCheckService.check(Party.RECIPIENT, shipmentDTO.getRecipientLocationCountryId(),
                shipmentDTO.getRecipientLocationCityId(), shipmentDTO.getRecipientLatitude(), shipmentDTO.getRecipientLongitude());

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
                publishShipment(shipmentDTO);
                shipmentNotificationProducer.sendNotificationToTopic(
                        ShipmentNotificationEventDTO.forShipment(NotificationType.SHIPMENT_CREATED, shipmentDTO));
            }
            case PAID -> {
                shipment.setPaymentStatus(PaymentStatus.PAID);
                shipment.setPrice(event.getAmount() != null ? event.getAmount().doubleValue() : null);
                // The captured amount is the authoritative price; the other services get it from this republish.
                publishShipment(shipmentMapper.toDTO(shipmentRepository.save(shipment)));
            }
            case EXPIRED, FAILED, CANCELED -> {
                if (current == PaymentStatus.PAID || current == PaymentStatus.REFUNDED) {
                    return;
                }
                boolean handedToLogistics = current == PaymentStatus.AUTHORIZED && !shipment.isCanceled();
                shipment.setPaymentStatus(next);
                shipment.setConfirmed(false);
                if (handedToLogistics) {
                    // Logistics already received this shipment as confirmed; it has to learn that it is canceled.
                    log.warn("Payment of shipment {} became {} after it was handed to logistics; canceling it",
                            shipment.getId(), next);
                    shipment.setCanceled(true);
                    publishShipment(shipmentMapper.toDTO(shipmentRepository.save(shipment)));
                } else {
                    shipmentRepository.save(shipment);
                }
            }
            case REFUNDED -> {
                shipment.setPaymentStatus(PaymentStatus.REFUNDED);
                shipmentRepository.save(shipment);
            }
            case PENDING -> log.debug("Ignoring PENDING payment status for shipment {}", shipment.getId());
        }
    }

    @Transactional
    public void applyDelivered(ShipmentDeliveredEventDTO event) {
        Optional<Shipment> found = shipmentRepository.findById(event.getShipmentId());
        if (found.isEmpty()) {
            log.warn("Ignoring delivered event for unknown shipment {}", event.getShipmentId());
            return;
        }
        Shipment shipment = found.get();
        if (shipment.isDelivered()) {
            return;
        }
        shipment.setDelivered(true);
        publishShipment(shipmentMapper.toDTO(shipmentRepository.save(shipment)));
    }

    private void publishShipment(ShipmentDTO shipmentDTO) {
        eventPublisher.publishEvent(shipmentDTO);
    }

    @Transactional(readOnly = true)
    public int republishAllShipments() {
        List<Shipment> shipments = shipmentRepository.findAll();
        shipments.stream().map(shipmentMapper::toDTO).forEach(shipmentProducer::sendShipmentToTopic);
        return shipments.size();
    }

    @Transactional(readOnly = true)
    public List<ShipmentDTO> getShipmentsForUser(String senderEmail) {
        return shipmentRepository.findBySenderEmail(senderEmail).stream()
                .map(shipmentMapper::toDTO)
                .toList();
    }
}
