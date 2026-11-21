package org.bme.micro_futar.courier.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bme.micro_futar.shared.dtos.AssignmentStatusEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentDeliveredEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteFulfilledEventDTO;
import org.bme.micro_futar.shared.enums.AssignmentStatus;
import org.bme.micro_futar.shared.exceptions.KafkaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** The events carrying courier-only facts must fail loudly when the broker does not acknowledge them. */
@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTests {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private KafkaProducerService producer;

    @BeforeEach
    void setUp() {
        producer = new KafkaProducerService(kafkaTemplate, new ObjectMapper().findAndRegisterModules());
        ReflectionTestUtils.setField(producer, "shipmentDeliveredTopic", "shipment-delivered");
        ReflectionTestUtils.setField(producer, "shipmentRouteFulfilledTopic", "shipment-route-fulfilled");
        ReflectionTestUtils.setField(producer, "assignmentStatusTopic", "assignment-status");
        ReflectionTestUtils.setField(producer, "sendTimeoutSeconds", 1L);
    }

    @Test
    void acknowledgedSendsReturnNormally() {
        when(kafkaTemplate.send(any(String.class), any(String.class), any(String.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        producer.sendAssignmentStatus(AssignmentStatusEventDTO.builder().assignmentId(5L).shipmentRouteId(3L)
                .status(AssignmentStatus.PICKED_UP).occurredAt(ZonedDateTime.now()).build());

        verify(kafkaTemplate).send(eq("assignment-status"), eq("5"), any(String.class));
    }

    @Test
    void brokerFailureIsThrownForEveryFactEvent() {
        when(kafkaTemplate.send(any(String.class), any(String.class), any(String.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("broker down")));

        assertThatThrownBy(() -> producer.sendAssignmentStatus(AssignmentStatusEventDTO.builder()
                .assignmentId(5L).status(AssignmentStatus.FAILED).build())).isInstanceOf(KafkaException.class);
        assertThatThrownBy(() -> producer.sendShipmentRouteFulfilled(ShipmentRouteFulfilledEventDTO.builder()
                .shipmentRouteId(3L).build())).isInstanceOf(KafkaException.class);
        assertThatThrownBy(() -> producer.sendShipmentDelivered(ShipmentDeliveredEventDTO.builder()
                .shipmentId(7L).build())).isInstanceOf(KafkaException.class);
    }

    @Test
    void unacknowledgedSendTimesOut() {
        when(kafkaTemplate.send(any(String.class), any(String.class), any(String.class)))
                .thenReturn(new CompletableFuture<>());

        assertThatThrownBy(() -> producer.sendShipmentDelivered(ShipmentDeliveredEventDTO.builder()
                .shipmentId(7L).build())).isInstanceOf(KafkaException.class);
    }
}
