package org.bme.micro_futar.tracking.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bme.micro_futar.tracking.entities.LocationCity;
import org.bme.micro_futar.tracking.mappers.LocationCityMapper;
import org.bme.micro_futar.tracking.services.LocationCityService;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Logistics now publishes geo data on the city topic; tracking does not store it but must not choke on it. */
class LocationCityConsumerTests {

    // the same defaults Spring Boot applies to its auto-configured ObjectMapper
    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @Test
    void acceptsCityMessageWithGeoFieldsAndEvenUnknownOnes() {
        LocationCityService service = mock(LocationCityService.class);
        when(service.saveLocationCity(any())).thenReturn(new LocationCity());
        LocationCityConsumer consumer = new LocationCityConsumer(service, objectMapper);

        consumer.consumeLocationCity("""
                {"id":7,"countryId":1,"name":"Budapest","deleted":false,
                 "centerLatitude":47.49,"centerLongitude":19.04,
                 "bboxMinLatitude":47.35,"bboxMinLongitude":18.9,"bboxMaxLatitude":47.65,"bboxMaxLongitude":19.3,
                 "geocodedAt":"2026-11-25T10:00:00+01:00","geoManual":true,
                 "someFutureField":"x"}
                """);

        var captor = ArgumentCaptor.forClass(org.bme.micro_futar.shared.dtos.LocationCityDTO.class);
        verify(service).saveLocationCity(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Budapest");
        assertThat(captor.getValue().getCenterLatitude()).isEqualTo(47.49);
        assertThat(captor.getValue().isGeoManual()).isTrue();
    }

    @Test
    void mapperIgnoresTheGeoFieldsTrackingDoesNotStore() {
        var dto = org.bme.micro_futar.shared.dtos.LocationCityDTO.builder()
                .id(7L).countryId(1L).name("Budapest").centerLatitude(47.49).geoManual(true).build();

        LocationCity entity = Mappers.getMapper(LocationCityMapper.class).toEntity(dto);

        assertThat(entity.getId()).isEqualTo(7L);
        assertThat(entity.getName()).isEqualTo("Budapest");
    }
}
