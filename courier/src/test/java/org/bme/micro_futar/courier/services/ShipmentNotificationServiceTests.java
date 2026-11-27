package org.bme.micro_futar.courier.services;

import org.bme.micro_futar.courier.kafka.KafkaProducerService;
import org.bme.micro_futar.courier.mappers.ShipmentMapper;
import org.bme.micro_futar.courier.repositories.ShipmentRepository;
import org.bme.micro_futar.courier.support.TestFixtures;
import org.bme.micro_futar.shared.exceptions.KafkaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentNotificationServiceTests {

    @Mock private ShipmentRepository shipmentRepository;
    @Mock private KafkaProducerService kafkaProducerService;

    @Test
    void failedNotificationNeverFailsTheCouriersAction() {
        var mapper = Mappers.getMapper(ShipmentMapper.class);
        var notifications = new ShipmentNotificationService(
                new ShipmentService(mapper, shipmentRepository), kafkaProducerService);
        when(shipmentRepository.findById(TestFixtures.SHIPMENT_ID))
                .thenReturn(Optional.of(mapper.toEntity(TestFixtures.shipment().build())));
        doThrow(new KafkaException("broker down")).when(kafkaProducerService).sendShipmentNotification(any());

        assertThatCode(() -> notifications.notifyDelivered(TestFixtures.deliveryLeg().build()))
                .doesNotThrowAnyException();
    }
}
