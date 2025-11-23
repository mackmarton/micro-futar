package org.bme.micro_futar.tracking.dtos;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class TrackingPartDTO {
    private String place;
    private ZonedDateTime time;
    private boolean destination = false;
}
