package org.bme.micro_futar.courier.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.bme.micro_futar.shared.exceptions.KafkaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.shipment-route-topic}")
    private String shipmentRouteTopic;
    @Value("${kafka.topics.shipment-route-courier-topic}")
    private String shipmentRouteCourierTopic;

    @Transactional
    public void sendShipmentRoute(ShipmentRouteDTO shipmentRouteDTO) {
        log.info("Sending shipment route message to topic {}: {}", shipmentRouteTopic, shipmentRouteDTO);
        try {
            kafkaTemplate.send(shipmentRouteTopic, shipmentRouteDTO.getId().toString(), shipmentRouteDTO)
                    .whenComplete((_, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent shipment route with ID: {} to topic: {}",
                                    shipmentRouteDTO.getId(), shipmentRouteTopic);
                        } else {
                            log.error("Failed to send shipment route with ID: {} to topic: {}",
                                    shipmentRouteDTO.getId(), shipmentRouteTopic, ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending shipment route message to Kafka", e);
            throw new KafkaException("Failed to send shipment route message", e);
        }
    }

    @Transactional
    public void sendShipmentRouteCourier(ShipmentRouteCourierDTO shipmentRouteCourierDTO) {
        log.info("Sending shipment route courier message to topic {}: {}", shipmentRouteCourierTopic, shipmentRouteCourierDTO);
        try {
            kafkaTemplate.send(shipmentRouteCourierTopic, shipmentRouteCourierDTO.getId().toString(), shipmentRouteCourierDTO)
                    .whenComplete((_, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent shipment route courier with ID: {} to topic: {}",
                                    shipmentRouteCourierDTO.getId(), shipmentRouteCourierTopic);
                        } else {
                            log.error("Failed to send shipment route courier with ID: {} to topic: {}",
                                    shipmentRouteCourierDTO.getId(), shipmentRouteCourierTopic, ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending shipment route courier message to Kafka", e);
            throw new KafkaException("Failed to send shipment route courier message", e);
        }
    }
}
