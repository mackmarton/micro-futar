package org.bme.micro_futar.courier.support;

import org.bme.micro_futar.shared.dtos.CourierDTO;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.bme.micro_futar.shared.dtos.VehicleDTO;

/**
 * Brings the courier service into a state the way production does: by publishing on the replicated topics and waiting
 * until the application has consumed the message.
 */
public class CourierScenario {

    /** Ids of today's two assignments of {@link TestFixtures#courier()}. */
    public static final long PICKUP = 1L;
    public static final long DELIVERY = 2L;

    private final KafkaTestClient kafka;

    CourierScenario(KafkaTestClient kafka) {
        this.kafka = kafka;
    }

    public static ShipmentRouteCourierDTO.ShipmentRouteCourierDTOBuilder pickup() {
        return TestFixtures.assignment(PICKUP, TestFixtures.PICKUP_ROUTE_ID);
    }

    public static ShipmentRouteCourierDTO.ShipmentRouteCourierDTOBuilder delivery() {
        return TestFixtures.assignment(DELIVERY, TestFixtures.DELIVERY_ROUTE_ID);
    }

    public void publish(CourierDTO courier) {
        publish("courier-topic", courier.getId(), courier);
    }

    public void publish(VehicleDTO vehicle) {
        publish("vehicle-topic", vehicle.getId(), vehicle);
    }

    public void publish(ShipmentDTO shipment) {
        publish("shipment-topic", shipment.getId(), shipment);
    }

    public void publish(ShipmentRouteDTO route) {
        publish("shipment-route-topic", route.getId(), route);
    }

    public void publish(ShipmentRouteCourierDTO assignment) {
        publish("shipment-route-courier-topic", assignment.getId(), assignment);
    }

    private void publish(String topicKey, Long id, Object message) {
        kafka.sendAndAwaitConsumption(topicKey, id.toString(), message);
    }
}
