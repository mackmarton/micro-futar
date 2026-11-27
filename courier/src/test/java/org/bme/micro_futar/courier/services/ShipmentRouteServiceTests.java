package org.bme.micro_futar.courier.services;

import org.bme.micro_futar.courier.kafka.KafkaProducerService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ShipmentRouteServiceTests {

    @Test
    void courierDoesNotWriteToTopicsItDoesNotOwn() {
        assertThat(Arrays.stream(KafkaProducerService.class.getDeclaredMethods()).map(Method::getName))
                .doesNotContain("sendShipment", "sendShipmentRoute", "sendShipmentRouteCourier");
        assertThat(Arrays.stream(ShipmentService.class.getDeclaredMethods()).map(Method::getName))
                .doesNotContain("save");
        assertThat(Arrays.stream(ShipmentRouteService.class.getDeclaredMethods()).map(Method::getName))
                .doesNotContain("save");
    }
}
