package org.bme.micro_futar.logistics.services.geocoding;

import org.bme.micro_futar.logistics.dtos.GeocodeJobStatusDTO;
import org.bme.micro_futar.logistics.dtos.GeocodeJobStatusDTO.State;
import org.bme.micro_futar.logistics.entities.LocationCity;
import org.bme.micro_futar.logistics.exceptions.GeocodeJobRunningException;
import org.bme.micro_futar.logistics.repositories.LocationCityRepository;
import org.bme.micro_futar.logistics.services.geocoding.CityGeocodingService.Outcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CityGeocodingJobTests {

    @Mock private CityGeocodingService geocodingService;
    @Mock private LocationCityRepository cityRepository;

    private static LocationCity city(long id) {
        LocationCity city = new LocationCity();
        city.setId(id);
        return city;
    }

    @Test
    void geocodesAllPendingCitiesAndCountsOutcomes() {
        List<LocationCity> pending = List.of(city(1), city(2), city(3), city(4));
        when(cityRepository.findAllByCenterLatitudeIsNullAndGeoManualFalse()).thenReturn(pending);
        when(geocodingService.geocodeMissing(pending.get(0))).thenReturn(Outcome.GEOCODED);
        when(geocodingService.geocodeMissing(pending.get(1))).thenReturn(Outcome.NOT_FOUND);
        when(geocodingService.geocodeMissing(pending.get(2))).thenReturn(Outcome.FAILED);
        when(geocodingService.geocodeMissing(pending.get(3))).thenReturn(Outcome.SKIPPED); // became manual meanwhile

        CityGeocodingJob job = new CityGeocodingJob(geocodingService, cityRepository, 0, Runnable::run);
        job.start();

        GeocodeJobStatusDTO status = job.getStatus();
        assertThat(status.getState()).isEqualTo(State.DONE);
        assertThat(status.getTotal()).isEqualTo(4);
        assertThat(status.getProcessed()).isEqualTo(4);
        assertThat(status.getGeocoded()).isEqualTo(1);
        assertThat(status.getNotFound()).isEqualTo(1);
        assertThat(status.getFailed()).isEqualTo(1);
        assertThat(status.getFinishedAt()).isNotNull();
    }

    @Test
    void rejectsASecondRunWhileOneIsInProgress() {
        when(cityRepository.findAllByCenterLatitudeIsNullAndGeoManualFalse()).thenReturn(List.of(city(1)));
        List<Runnable> queued = new ArrayList<>();
        CityGeocodingJob job = new CityGeocodingJob(geocodingService, cityRepository, 0, queued::add);

        assertThat(job.start().getState()).isEqualTo(State.RUNNING);
        assertThatThrownBy(job::start).isInstanceOf(GeocodeJobRunningException.class);

        queued.getFirst().run();
        assertThat(job.getStatus().getState()).isEqualTo(State.DONE);
        assertThat(job.start().getState()).isEqualTo(State.RUNNING); // can run again afterwards
    }

    @Test
    void unexpectedErrorMarksTheRunFailed() {
        List<LocationCity> pending = List.of(city(1));
        when(cityRepository.findAllByCenterLatitudeIsNullAndGeoManualFalse()).thenReturn(pending);
        when(geocodingService.geocodeMissing(pending.getFirst())).thenThrow(new IllegalStateException("boom"));

        CityGeocodingJob job = new CityGeocodingJob(geocodingService, cityRepository, 0, Runnable::run);
        job.start();

        assertThat(job.getStatus().getState()).isEqualTo(State.FAILED);
    }

    @Test
    void idleBeforeTheFirstRun() {
        CityGeocodingJob job = new CityGeocodingJob(geocodingService, cityRepository, 0, Runnable::run);
        assertThat(job.getStatus().getState()).isEqualTo(State.IDLE);
    }
}
