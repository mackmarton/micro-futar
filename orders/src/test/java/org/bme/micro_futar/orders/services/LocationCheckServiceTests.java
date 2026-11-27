package org.bme.micro_futar.orders.services;

import org.bme.micro_futar.orders.support.TestFixtures;
import org.bme.micro_futar.orders.entities.LocationCity;
import org.bme.micro_futar.orders.exceptions.LocationValidationException;
import org.bme.micro_futar.orders.exceptions.LocationValidationException.Code;
import org.bme.micro_futar.orders.exceptions.LocationValidationException.Party;
import org.bme.micro_futar.orders.repositories.LocationCityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationCheckServiceTests {

    private static final long COUNTRY = TestFixtures.HUNGARY_ID;
    private static final long CITY = 10L;

    @Mock
    private LocationCityRepository cityRepository;

    private LocationCheckService service;
    private LocationCity city;

    @BeforeEach
    void setUp() {
        service = new LocationCheckService(cityRepository, 3, 25);
        city = TestFixtures.entity(TestFixtures.budapest()
                .id(CITY)
                .centerLatitude(null).centerLongitude(null)
                .bboxMinLatitude(null).bboxMinLongitude(null)
                .bboxMaxLatitude(null).bboxMaxLongitude(null)
                .build());
        lenient().when(cityRepository.findById(CITY)).thenReturn(Optional.of(city));
    }

    private void withBbox() {
        city.setCenterLatitude(47.4979);
        city.setCenterLongitude(19.0402);
        city.setBboxMinLatitude(47.35);
        city.setBboxMinLongitude(18.9);
        city.setBboxMaxLatitude(47.65);
        city.setBboxMaxLongitude(19.3);
    }

    private void expectCode(Party party, double lat, double lon, Code code) {
        assertThatThrownBy(() -> service.check(party, COUNTRY, CITY, lat, lon))
                .isInstanceOfSatisfying(LocationValidationException.class, e -> {
                    assertThat(e.getCode()).isEqualTo(code);
                    assertThat(e.getParty()).isEqualTo(party);
                });
    }

    @Test
    void acceptsPointInsideBbox() {
        withBbox();
        assertThatCode(() -> service.check(Party.SENDER, COUNTRY, CITY, 47.5, 19.05)).doesNotThrowAnyException();
    }

    @Test
    void acceptsPointJustOutsideBboxWithinTolerance() {
        withBbox();
        assertThatCode(() -> service.check(Party.RECIPIENT, COUNTRY, CITY, 47.66, 19.1)).doesNotThrowAnyException();
    }

    @Test
    void rejectsFarPointForSenderAndRecipient() {
        withBbox();
        expectCode(Party.SENDER, 47.5316, 21.6273, Code.LOCATION_TOO_FAR_FROM_CITY); // Debrecen
        expectCode(Party.RECIPIENT, 47.5316, 21.6273, Code.LOCATION_TOO_FAR_FROM_CITY);
    }

    @Test
    void usesDefaultRadiusAroundCentreWithoutBbox() {
        city.setCenterLatitude(47.4979);
        city.setCenterLongitude(19.0402);
        assertThatCode(() -> service.check(Party.SENDER, COUNTRY, CITY, 47.60, 19.1)).doesNotThrowAnyException(); // ~12 km
        expectCode(Party.SENDER, 47.80, 19.04, Code.LOCATION_TOO_FAR_FROM_CITY); // ~33 km
    }

    @Test
    void skipsCheckWhenCityHasNoGeoData() {
        assertThatCode(() -> service.check(Party.SENDER, COUNTRY, CITY, 10.0, 10.0)).doesNotThrowAnyException();
        // second call is silent as well (warning is logged once per city)
        assertThatCode(() -> service.check(Party.RECIPIENT, COUNTRY, CITY, -10.0, -10.0)).doesNotThrowAnyException();
    }

    @Test
    void rejectsInvalidOrMissingCoordinates() {
        withBbox();
        expectCode(Party.SENDER, 91.0, 19.0, Code.INVALID_COORDINATES);
        expectCode(Party.RECIPIENT, 47.5, 181.0, Code.INVALID_COORDINATES);
        assertThatThrownBy(() -> service.check(Party.SENDER, COUNTRY, CITY, null, 19.0))
                .isInstanceOfSatisfying(LocationValidationException.class,
                        e -> assertThat(e.getCode()).isEqualTo(Code.INVALID_COORDINATES));
    }

    @Test
    void rejectsCityOfAnotherCountry() {
        withBbox();
        assertThatThrownBy(() -> service.check(Party.RECIPIENT, 2L, CITY, 47.5, 19.05))
                .isInstanceOfSatisfying(LocationValidationException.class, e -> {
                    assertThat(e.getCode()).isEqualTo(Code.CITY_COUNTRY_MISMATCH);
                    assertThat(e.getParty()).isEqualTo(Party.RECIPIENT);
                });
    }

    @Test
    void rejectsUnknownOrDeletedCity() {
        when(cityRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.check(Party.SENDER, COUNTRY, 99L, 47.5, 19.05))
                .isInstanceOfSatisfying(LocationValidationException.class,
                        e -> assertThat(e.getCode()).isEqualTo(Code.CITY_NOT_FOUND));
        city.setDeleted(true);
        expectCode(Party.SENDER, 47.5, 19.05, Code.CITY_NOT_FOUND);
    }
}
