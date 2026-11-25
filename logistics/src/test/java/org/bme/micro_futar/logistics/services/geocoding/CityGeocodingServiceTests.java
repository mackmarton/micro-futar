package org.bme.micro_futar.logistics.services.geocoding;

import org.bme.micro_futar.logistics.entities.LocationCity;
import org.bme.micro_futar.logistics.entities.LocationCountry;
import org.bme.micro_futar.logistics.exceptions.GeocodingException;
import org.bme.micro_futar.logistics.exceptions.GeocodingNotFoundException;
import org.bme.micro_futar.logistics.repositories.LocationCityRepository;
import org.bme.micro_futar.logistics.repositories.LocationCountryRepository;
import org.bme.micro_futar.logistics.services.LocationCityService;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CityGeocodingServiceTests {

    private static final GeocodeResult GEO = new GeocodeResult(47.49, 19.04, 47.35, 18.9, 47.65, 19.3);

    @Mock private LocationCityRepository cityRepository;
    @Mock private LocationCountryRepository countryRepository;
    @Mock private OrsGeocodingClient client;
    @Mock private LocationCityService cityService;

    private CityGeocodingService service;

    @BeforeEach
    void setUp() {
        service = new CityGeocodingService(cityRepository, countryRepository, client, cityService);
        LocationCountry hungary = new LocationCountry();
        hungary.setId(1L);
        hungary.setIsoCode("HU");
        lenient().when(countryRepository.findById(1L)).thenReturn(Optional.of(hungary));
        LocationCountry noIso = new LocationCountry();
        noIso.setId(2L);
        lenient().when(countryRepository.findById(2L)).thenReturn(Optional.of(noIso));
    }

    private static LocationCityDTO dto(String name, long countryId) {
        return LocationCityDTO.builder().countryId(countryId).name(name).build();
    }

    private static LocationCity city(long id, String name, boolean manual) {
        LocationCity city = new LocationCity();
        city.setId(id);
        city.setName(name);
        city.setCountryId(1L);
        city.setGeoManual(manual);
        return city;
    }

    @Test
    void createGeocodesBeforeSaving() {
        LocationCityDTO request = dto("Szeged", 1L);
        when(client.geocode("Szeged", "HU")).thenReturn(Optional.of(GEO));

        service.createCity(request);

        verify(cityService).createCity(request, GEO);
    }

    @Test
    void createStillSavesWhenOrsIsDown() {
        LocationCityDTO request = dto("Szeged", 1L);
        when(client.geocode("Szeged", "HU")).thenThrow(new GeocodingException("timeout", null));

        service.createCity(request);

        verify(cityService).createCity(request, null);
    }

    @Test
    void createSavesWithoutGeoDataWhenNothingIsFoundOrCountryHasNoIsoCode() {
        LocationCityDTO notFound = dto("Nowhere", 1L);
        when(client.geocode("Nowhere", "HU")).thenReturn(Optional.empty());
        service.createCity(notFound);
        verify(cityService).createCity(notFound, null);

        LocationCityDTO noIso = dto("Wien", 2L);
        service.createCity(noIso);
        verify(cityService).createCity(noIso, null);
        verify(client, never()).geocode(eq("Wien"), any());
    }

    @Test
    void createWithAdminSuppliedGeoSkipsTheGeocoder() {
        LocationCityDTO request = dto("Szeged", 1L);
        request.setCenterLatitude(46.25);
        request.setCenterLongitude(20.15);

        service.createCity(request);

        verifyNoInteractions(client);
        verify(cityService).createCity(request, null);
    }

    @Test
    void renamingARegularCityGeocodesAgain() {
        when(cityRepository.findById(5L)).thenReturn(Optional.of(city(5L, "Szeged", false)));
        when(client.geocode("Szegedin", "HU")).thenReturn(Optional.of(GEO));
        LocationCityDTO request = dto("Szegedin", 1L);

        service.updateCity(5L, request);

        verify(cityService).updateCity(5L, request, GEO);
    }

    @Test
    void updateWithoutNameOrCountryChangeDoesNotCallTheGeocoder() {
        when(cityRepository.findById(5L)).thenReturn(Optional.of(city(5L, "Szeged", false)));
        LocationCityDTO request = dto("Szeged", 1L);

        service.updateCity(5L, request);

        verifyNoInteractions(client);
        verify(cityService).updateCity(5L, request, null);
    }

    @Test
    void renamingAManualCityKeepsItsGeoData() {
        when(cityRepository.findById(5L)).thenReturn(Optional.of(city(5L, "Szeged", true)));
        LocationCityDTO request = dto("Szegedin", 1L);

        service.updateCity(5L, request);

        verifyNoInteractions(client);
        verify(cityService).updateCity(5L, request, null);
    }

    @Test
    void regeocodeRestoresManualCityAndReportsFailures() {
        LocationCity manual = city(5L, "Szeged", true);
        when(cityRepository.findById(5L)).thenReturn(Optional.of(manual));
        when(client.geocode("Szeged", "HU")).thenReturn(Optional.of(GEO));
        LocationCityDTO updated = dto("Szeged", 1L);
        when(cityService.applyGeocode(5L, GEO, false)).thenReturn(Optional.of(updated));

        assertThat(service.regeocode(5L)).contains(updated);
        verify(cityService).applyGeocode(5L, GEO, false);
    }

    @Test
    void regeocodeOfMissingCityIsEmptyAndNoHitIsAnError() {
        when(cityRepository.findById(9L)).thenReturn(Optional.empty());
        assertThat(service.regeocode(9L)).isEmpty();

        when(cityRepository.findById(5L)).thenReturn(Optional.of(city(5L, "Nowhere", false)));
        when(client.geocode("Nowhere", "HU")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.regeocode(5L)).isInstanceOf(GeocodingNotFoundException.class);
    }

    @Test
    void regeocodePropagatesOrsFailureSoTheAdminSeesIt() {
        when(cityRepository.findById(5L)).thenReturn(Optional.of(city(5L, "Szeged", false)));
        when(client.geocode("Szeged", "HU")).thenThrow(new GeocodingException("timeout", null));
        assertThatThrownBy(() -> service.regeocode(5L)).isInstanceOf(GeocodingException.class);
    }

    @Test
    void geocodeMissingReportsOutcomes() {
        LocationCity ok = city(1L, "Szeged", false);
        LocationCity none = city(2L, "Nowhere", false);
        LocationCity down = city(3L, "Pécs", false);
        LocationCity manualNow = city(4L, "Győr", false);
        when(client.geocode("Szeged", "HU")).thenReturn(Optional.of(GEO));
        when(client.geocode("Nowhere", "HU")).thenReturn(Optional.empty());
        when(client.geocode("Pécs", "HU")).thenThrow(new GeocodingException("quota", null));
        when(client.geocode("Győr", "HU")).thenReturn(Optional.of(GEO));
        when(cityService.applyGeocode(1L, GEO, true)).thenReturn(Optional.of(dto("Szeged", 1L)));
        when(cityService.applyGeocode(4L, GEO, true)).thenReturn(Optional.empty());

        assertThat(service.geocodeMissing(ok)).isEqualTo(CityGeocodingService.Outcome.GEOCODED);
        assertThat(service.geocodeMissing(none)).isEqualTo(CityGeocodingService.Outcome.NOT_FOUND);
        assertThat(service.geocodeMissing(down)).isEqualTo(CityGeocodingService.Outcome.FAILED);
        assertThat(service.geocodeMissing(manualNow)).isEqualTo(CityGeocodingService.Outcome.SKIPPED);
    }
}
