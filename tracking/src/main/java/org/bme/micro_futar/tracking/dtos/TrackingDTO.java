package org.bme.micro_futar.tracking.dtos;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class TrackingDTO {
    private final Map<Integer, TrackingPartDTO> trackingParts = new HashMap<>();
}
