package org.bme.micro_futar.shared.dtos;

import lombok.*;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationCityDTO {
    private Long id;
    @NonNull
    private Long countryId;
    @NonNull
    private String name;
    private boolean deleted;
    /** City centre as found by the geocoder or set by an admin; null when unknown. */
    private Double centerLatitude;
    private Double centerLongitude;
    /** Bounding box of the city; all four are set together or not at all. */
    private Double bboxMinLatitude;
    private Double bboxMinLongitude;
    private Double bboxMaxLatitude;
    private Double bboxMaxLongitude;
    /** When logistics last received geo data from the geocoder. */
    private ZonedDateTime geocodedAt;
    /** True when an administrator set the geo data by hand; bulk geocoding never overwrites it. */
    private boolean geoManual;
}
