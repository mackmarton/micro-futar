package org.bme.micro_futar.courier.dtos;

import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;

/**
 * What a courier needs to carry out one of their assignments, and nothing more: never add emails, prices or payment
 * fields. The contact and address belong to the sender on a pickup leg and to the recipient on a delivery leg, and
 * are empty on a leg between depos.
 */
@Value
@Builder
public class MyAssignmentDTO {
    Long assignmentId;
    Long shipmentRouteId;
    AssignmentType assignmentType;
    boolean pickedUpForDelivery;
    boolean failed;
    ZonedDateTime fulfillmentTime;
    String parcelNumber;
    Long packageSizeId;
    String contactName;
    String contactPhone;
    String address;
    Double latitude;
    Double longitude;
}
