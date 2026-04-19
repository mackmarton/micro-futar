package org.bme.micro_futar.courier.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
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

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.shipment-topic}")
    private String shipmentTopic;
    @Value("${kafka.topics.shipment-route-topic}")
    private String shipmentRouteTopic;
    @Value("${kafka.topics.shipment-route-courier-topic}")
    private String shipmentRouteCourierTopic;

    @Transactional
    public void sendShipment(ShipmentDTO shipmentDTO) {
        log.info("Sending shipment message to topic {}: {}", shipmentTopic, shipmentDTO);
        try {
            String jsonMessage = objectMapper.writeValueAsString(shipmentDTO);
            kafkaTemplate.send(shipmentTopic, shipmentDTO.getId().toString(), jsonMessage)
                    .whenComplete((_, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent shipment with ID: {} to topic: {}",
                                    shipmentDTO.getId(), shipmentTopic);
                        } else {
                            log.error("Failed to send shipment with ID: {} to topic: {}",
                                    shipmentDTO.getId(), shipmentTopic, ex);
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("Error serializing shipment message to JSON", e);
            throw new KafkaException("Failed to serialize shipment message", e);
        } catch (Exception e) {
            log.error("Error sending shipment message to Kafka", e);
            throw new KafkaException("Failed to send shipment message", e);
        }
    }

    @Transactional
    public void sendShipmentRoute(ShipmentRouteDTO shipmentRouteDTO) {
        log.info("Sending shipment route message to topic {}: {}", shipmentRouteTopic, shipmentRouteDTO);
        try {
            String jsonMessage = objectMapper.writeValueAsString(shipmentRouteDTO);
            kafkaTemplate.send(shipmentRouteTopic, shipmentRouteDTO.getId().toString(), jsonMessage)
                    .whenComplete((_, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent shipment route with ID: {} to topic: {}",
                                    shipmentRouteDTO.getId(), shipmentRouteTopic);
                        } else {
                            log.error("Failed to send shipment route with ID: {} to topic: {}",
                                    shipmentRouteDTO.getId(), shipmentRouteTopic, ex);
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("Error serializing shipment route message to JSON", e);
            throw new KafkaException("Failed to serialize shipment route message", e);
        } catch (Exception e) {
            log.error("Error sending shipment route message to Kafka", e);
            throw new KafkaException("Failed to send shipment route message", e);
        }
    }

    @Transactional
    public void sendShipmentRouteCourier(ShipmentRouteCourierDTO shipmentRouteCourierDTO) {
        log.info("Sending shipment route courier message to topic {}: {}", shipmentRouteCourierTopic, shipmentRouteCourierDTO);
        try {
            String jsonMessage = objectMapper.writeValueAsString(shipmentRouteCourierDTO);
            kafkaTemplate.send(shipmentRouteCourierTopic, shipmentRouteCourierDTO.getId().toString(), jsonMessage)
                    .whenComplete((_, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent shipment route courier with ID: {} to topic: {}",
                                    shipmentRouteCourierDTO.getId(), shipmentRouteCourierTopic);
                        } else {
                            log.error("Failed to send shipment route courier with ID: {} to topic: {}",
                                    shipmentRouteCourierDTO.getId(), shipmentRouteCourierTopic, ex);
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("Error serializing shipment route courier message to JSON", e);
            throw new KafkaException("Failed to serialize shipment route courier message", e);
        } catch (Exception e) {
            log.error("Error sending shipment route courier message to Kafka", e);
            throw new KafkaException("Failed to send shipment route courier message", e);
        }
    }
}
