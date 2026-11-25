package org.bme.micro_futar.logistics.services.geocoding;

import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.logistics.dtos.GeocodeJobStatusDTO;
import org.bme.micro_futar.logistics.dtos.GeocodeJobStatusDTO.State;
import org.bme.micro_futar.logistics.entities.LocationCity;
import org.bme.micro_futar.logistics.exceptions.GeocodeJobRunningException;
import org.bme.micro_futar.logistics.repositories.LocationCityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jakarta.annotation.PreDestroy;

/**
 * Background run that geocodes every city without geo data, one at a time with a pause between the calls (the
 * OpenRouteService geocoding quota is per minute). Only one run at a time; the state lives in memory.
 */
@Slf4j
@Component
public class CityGeocodingJob {

    private final CityGeocodingService geocodingService;
    private final LocationCityRepository cityRepository;
    private final long delayMs;
    private final Executor executor;
    private final ExecutorService ownedExecutor;

    private volatile GeocodeJobStatusDTO status = GeocodeJobStatusDTO.builder().state(State.IDLE).build();

    @Autowired
    public CityGeocodingJob(CityGeocodingService geocodingService, LocationCityRepository cityRepository,
                            @Value("${open-route-service.geocode-delay-ms:700}") long delayMs) {
        this(geocodingService, cityRepository, delayMs, null);
    }

    /** Tests pass a direct executor to run the job synchronously. */
    CityGeocodingJob(CityGeocodingService geocodingService, LocationCityRepository cityRepository, long delayMs,
                     Executor executor) {
        this.geocodingService = geocodingService;
        this.cityRepository = cityRepository;
        this.delayMs = delayMs;
        this.ownedExecutor = executor == null ? Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "city-geocoding");
            thread.setDaemon(true);
            return thread;
        }) : null;
        this.executor = executor == null ? ownedExecutor : executor;
    }

    public GeocodeJobStatusDTO getStatus() {
        return status;
    }

    /** @throws GeocodeJobRunningException when a run is in progress */
    public synchronized GeocodeJobStatusDTO start() {
        if (status.getState() == State.RUNNING) {
            throw new GeocodeJobRunningException();
        }
        List<LocationCity> pending = cityRepository.findAllByCenterLatitudeIsNullAndGeoManualFalse();
        status = GeocodeJobStatusDTO.builder()
                .state(State.RUNNING)
                .total(pending.size())
                .startedAt(ZonedDateTime.now())
                .build();
        executor.execute(() -> run(pending));
        return status;
    }

    private void run(List<LocationCity> pending) {
        try {
            for (int i = 0; i < pending.size(); i++) {
                CityGeocodingService.Outcome outcome = geocodingService.geocodeMissing(pending.get(i));
                GeocodeJobStatusDTO current = status;
                status = current.toBuilder()
                        .processed(current.getProcessed() + 1)
                        .geocoded(current.getGeocoded() + (outcome == CityGeocodingService.Outcome.GEOCODED ? 1 : 0))
                        .notFound(current.getNotFound() + (outcome == CityGeocodingService.Outcome.NOT_FOUND ? 1 : 0))
                        .failed(current.getFailed() + (outcome == CityGeocodingService.Outcome.FAILED ? 1 : 0))
                        .build();
                if (delayMs > 0 && i < pending.size() - 1) {
                    Thread.sleep(delayMs);
                }
            }
            status = status.toBuilder().state(State.DONE).finishedAt(ZonedDateTime.now()).build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            status = status.toBuilder().state(State.FAILED).finishedAt(ZonedDateTime.now()).build();
        } catch (RuntimeException e) {
            log.error("Bulk city geocoding aborted", e);
            status = status.toBuilder().state(State.FAILED).finishedAt(ZonedDateTime.now()).build();
        }
    }

    @PreDestroy
    void shutdown() {
        if (ownedExecutor != null) {
            ownedExecutor.shutdownNow();
        }
    }
}
