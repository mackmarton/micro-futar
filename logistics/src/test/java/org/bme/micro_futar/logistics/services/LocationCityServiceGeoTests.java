package org.bme.micro_futar.logistics.services;

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
class LocationCityServiceGeoTests {

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
        return LocationCityDTO.builder().countryId(1L).name("Budapest").build();
    }

    private static LocationCity existingWithGeo(boolean manual) {
        LocationCity city = new LocationCity();
        city.setId(5L);
        city.setCountryId(1L);
        city.setName("Budapest");
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
    void createWithGeocodeResultStoresItAsNotManualAndPublishes() {
        LocationCityDTO result = service.createCity(dto(), BUDAPEST);

        assertThat(result.getCenterLatitude()).isEqualTo(47.49);
        assertThat(result.getBboxMaxLongitude()).isEqualTo(19.3);
        assertThat(result.getGeocodedAt()).isNotNull();
        assertThat(result.isGeoManual()).isFalse();
        verify(kafkaProducerService).sendLocationCity(result);
    }

    @Test
    void createWithoutGeocodeResultSavesCityWithoutGeoData() {
        LocationCityDTO result = service.createCity(dto());

        assertThat(result.getCenterLatitude()).isNull();
        assertThat(result.getGeocodedAt()).isNull();
        assertThat(result.isGeoManual()).isFalse();
        verify(repository).save(any());
    }

    @Test
    void createIgnoresClientSuppliedGeoManualAndGeocodedAtButTreatsClientGeoAsManual() {
        LocationCityDTO request = dto();
        request.setGeocodedAt(ZonedDateTime.now());
        assertThat(service.createCity(request).isGeoManual()).isFalse(); // no geo values: flags are not trusted

        LocationCityDTO withGeo = dto();
        withGeo.setCenterLatitude(47.0);
        withGeo.setCenterLongitude(19.0);
        LocationCityDTO result = service.createCity(withGeo);
        assertThat(result.isGeoManual()).isTrue();
        assertThat(result.getGeocodedAt()).isNull();
    }

    @Test
    void manualChangeOnUpdateSetsGeoManual() {
        LocationCity existing = existingWithGeo(false);
        lenient().when(repository.findById(5L)).thenReturn(Optional.of(existing));
        LocationCityDTO request = dto();
        request.setCenterLatitude(47.1);
        request.setCenterLongitude(19.0);
        request.setBboxMinLatitude(46.0);
        request.setBboxMinLongitude(18.0);
        request.setBboxMaxLatitude(48.0);
        request.setBboxMaxLongitude(20.0);

        LocationCityDTO result = service.updateCity(5L, request).orElseThrow();

        assertThat(result.isGeoManual()).isTrue();
        assertThat(result.getCenterLatitude()).isEqualTo(47.1);
        assertThat(result.getGeocodedAt()).isEqualTo(existing.getGeocodedAt());
        verify(kafkaProducerService).sendLocationCity(result);
    }

    @Test
    void updateWithUnchangedOrAbsentGeoKeepsStoredGeoAndFlags() {
        LocationCity existing = existingWithGeo(true);
        lenient().when(repository.findById(5L)).thenReturn(Optional.of(existing));

        LocationCityDTO renamed = dto(); // no geo fields at all, like the old UI payload
        renamed.setName("Budapest II.");
        LocationCityDTO result = service.updateCity(5L, renamed).orElseThrow();

        assertThat(result.getName()).isEqualTo("Budapest II.");
        assertThat(result.getCenterLatitude()).isEqualTo(47.0);
        assertThat(result.getBboxMaxLatitude()).isEqualTo(48.0);
        assertThat(result.isGeoManual()).isTrue();
        assertThat(result.getGeocodedAt()).isEqualTo(existing.getGeocodedAt());
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
    void applyGeocodeOverwritesManualCityWhenForced() {
        lenient().when(repository.findById(5L)).thenReturn(Optional.of(existingWithGeo(true)));

        LocationCityDTO result = service.applyGeocode(5L, BUDAPEST, false).orElseThrow();

        assertThat(result.isGeoManual()).isFalse();
        assertThat(result.getCenterLatitude()).isEqualTo(47.49);
        verify(kafkaProducerService).sendLocationCity(result);
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
