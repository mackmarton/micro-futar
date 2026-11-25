package org.bme.micro_futar.logistics.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/** Progress of the bulk "geocode all cities without geo data" run. */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class GeocodeJobStatusDTO {

    public enum State { IDLE, RUNNING, DONE, FAILED }

    private State state;
    private int total;
    private int processed;
    private int geocoded;
    private int notFound;
    private int failed;
    private ZonedDateTime startedAt;
    private ZonedDateTime finishedAt;
}
