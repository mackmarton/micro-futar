package org.bme.micro_futar.logistics.services;

import org.bme.micro_futar.logistics.support.TestFixtures;
import org.bme.micro_futar.logistics.entities.LocationCity;
import org.bme.micro_futar.logistics.kafka.KafkaProducerService;
import org.bme.micro_futar.logistics.mappers.LocationCityMapper;
import org.bme.micro_futar.logistics.repositories.LocationCityRepository;
import org.bme.micro_futar.logistics.services.geocoding.GeocodeResult;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocationCityServiceTests {

    private static final GeocodeResult BUDAPEST = new GeocodeResult(47.49, 19.04, 47.35, 18.9, 47.65, 19.3);

    @Mock private LocationCityRepository repository;
    @Mock private KafkaProducerService kafkaProducerService;
    @Mock private ReferenceValidator referenceValidator;

    private LocationCityService service;

    @BeforeEach
    void setUp() {
        lenient().when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        service = new LocationCityService(repository, Mappers.getMapper(LocationCityMapper.class),
                kafkaProducerService, referenceValidator);
    }

    private static LocationCityDTO dto() {
        return TestFixtures.budapest(TestFixtures.HUNGARY_ID).build();
    }

    private static LocationCity existingWithGeo(boolean manual) {
        LocationCity city = TestFixtures.entity(dto());
        city.setId(5L);
        city.setCenterLatitude(47.0);
        city.setCenterLongitude(19.0);
        city.setBboxMinLatitude(46.0);
        city.setBboxMinLongitude(18.0);
        city.setBboxMaxLatitude(48.0);
        city.setBboxMaxLongitude(20.0);
        city.setGeocodedAt(ZonedDateTime.now().minusDays(1));
        city.setGeoManual(manual);
        return city;
    }

    @Test
    void updateWithGeocodeResultResetsManualFlag() {
        lenient().when(repository.findById(5L)).thenReturn(Optional.of(existingWithGeo(true)));

        LocationCityDTO result = service.updateCity(5L, dto(), BUDAPEST).orElseThrow();

        assertThat(result.isGeoManual()).isFalse();
        assertThat(result.getCenterLatitude()).isEqualTo(47.49);
    }

    @Test
    void applyGeocodeSkipsManualCitiesWhenRespectingTheFlag() {
        lenient().when(repository.findById(5L)).thenReturn(Optional.of(existingWithGeo(true)));

        assertThat(service.applyGeocode(5L, BUDAPEST, true)).isEmpty();
        verify(repository, never()).save(any());
        verify(kafkaProducerService, never()).sendLocationCity(any());
    }

    @Test
    void validatesRanges() {
        LocationCityDTO request = dto();
        request.setCenterLatitude(91.0);
        request.setCenterLongitude(19.0);
        assertThatThrownBy(() -> service.createCity(request)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("centerLatitude");

        LocationCityDTO lon = dto();
        lon.setCenterLatitude(47.0);
        lon.setCenterLongitude(-180.5);
        assertThatThrownBy(() -> service.createCity(lon)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("centerLongitude");
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsMinGreaterThanMaxAndIncompleteBbox() {
        LocationCityDTO swapped = dto();
        swapped.setBboxMinLatitude(48.0);
        swapped.setBboxMinLongitude(18.0);
        swapped.setBboxMaxLatitude(47.0);
        swapped.setBboxMaxLongitude(20.0);
        assertThatThrownBy(() -> service.createCity(swapped)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minimum");

        LocationCityDTO partial = dto();
        partial.setBboxMinLatitude(46.0);
        assertThatThrownBy(() -> service.createCity(partial)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("together");

        LocationCityDTO halfCenter = dto();
        halfCenter.setCenterLatitude(47.0);
        assertThatThrownBy(() -> service.createCity(halfCenter)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("together");
    }
}
