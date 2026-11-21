package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.entities.ShipmentPricing;
import org.bme.micro_futar.logistics.mappers.ShipmentMapper;
import org.bme.micro_futar.logistics.repositories.ShipmentPricingRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRepository;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteRepository;
import org.bme.micro_futar.logistics.services.planners.ShipmentRoutePlanner;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.dtos.ShipmentPricedEventDTO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentMapper shipmentMapper;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentRouteRepository shipmentRouteRepository;
    private final ShipmentPricingRepository shipmentPricingRepository;
    private final ShipmentRoutePlanner shipmentRoutePlanner;
    private final ShipmentPricingService shipmentPricingService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void processShipment(ShipmentDTO shipmentDTO) {
        shipmentRepository.save(shipmentMapper.toEntity(shipmentDTO));

        if (!shipmentDTO.isConfirmed() || shipmentRouteRepository.existsByShipmentId(shipmentDTO.getId())) {
            return;
        }

        double routePrice = shipmentRoutePlanner.planRouteForShipment(shipmentDTO);

        // Shipments from before online payment have no authorized maximum, so there is nothing to capture.
        if (shipmentDTO.getMaxPrice() != null) {
            BigDecimal finalPrice = shipmentPricingService.finalPrice(shipmentDTO, routePrice);
            ZonedDateTime pricedAt = ZonedDateTime.now();
            ShipmentPricing pricing = new ShipmentPricing();
            pricing.setShipmentId(shipmentDTO.getId());
            pricing.setFinalPrice(finalPrice);
            pricing.setCurrencyCode(shipmentDTO.getCurrencyCode());
            pricing.setPricedAt(pricedAt);
            shipmentPricingRepository.save(pricing);
            // Sent to Kafka by KafkaProducerService only after this transaction commits.
            eventPublisher.publishEvent(ShipmentPricedEventDTO.builder()
                    .shipmentId(shipmentDTO.getId())
                    .finalAmount(finalPrice)
                    .currencyCode(shipmentDTO.getCurrencyCode())
                    .pricedAt(pricedAt)
                    .build());
        }
    }

    public ShipmentDTO getShipmentById(Long id) {
        return shipmentRepository.findById(id)
                .map(shipmentMapper::toDTO)
                .orElse(null);
    }
}
