package org.bme.micro_futar.logistics.services;

import org.bme.micro_futar.logistics.dtos.DepoTransitDTO;
import org.bme.micro_futar.logistics.entities.DepoTransit;
import org.bme.micro_futar.logistics.mappers.DepoTransitMapper;
import org.bme.micro_futar.logistics.repositories.DepoTransitRepository;
import org.bme.micro_futar.logistics.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DepoRouteFinderServiceTests {

    private static final long BUDAPEST = 1L;
    private static final long GYOR = 2L;
    private static final long VIENNA = 3L;
    private static final long GRAZ = 4L;
    private static final long SIZE = TestFixtures.SMALL_PACKAGE_ID;

    private final DepoTransitRepository transits = mock(DepoTransitRepository.class);
    private final DepoRouteFinderService finder = new DepoRouteFinderService(
            new DepoTransitService(Mappers.getMapper(DepoTransitMapper.class), transits, null, null));

    @Test
    void directTransitIsTheRoute() {
        givenTransits(transit(BUDAPEST, VIENNA, 2790.0));

        assertThat(finder.findCheapestRoute(BUDAPEST, VIENNA, SIZE)).containsExactly(BUDAPEST, VIENNA);
        assertThat(finder.findCheapestRoutePrice(BUDAPEST, VIENNA, SIZE)).isEqualTo(2790.0);
    }

    @Test
    void routeLeadsThroughAnIntermediateDepo() {
        givenTransits(transit(BUDAPEST, GYOR, 900.0), transit(GYOR, VIENNA, 1200.0), transit(VIENNA, GRAZ, 1100.0));

        assertThat(finder.findCheapestRoute(BUDAPEST, GRAZ, SIZE)).containsExactly(BUDAPEST, GYOR, VIENNA, GRAZ);
        assertThat(finder.findCheapestRoutePrice(BUDAPEST, GRAZ, SIZE)).isEqualTo(3200.0);
    }

    @Test
    void cheaperDetourBeatsTheDirectTransit() {
        givenTransits(transit(BUDAPEST, VIENNA, 2790.0), transit(BUDAPEST, GYOR, 900.0), transit(GYOR, VIENNA, 1200.0));

        assertThat(finder.findCheapestRoute(BUDAPEST, VIENNA, SIZE)).containsExactly(BUDAPEST, GYOR, VIENNA);
        assertThat(finder.findCheapestRoutePrice(BUDAPEST, VIENNA, SIZE)).isEqualTo(2100.0);
    }

    @Test
    void unreachableDestinationHasNoRoute() {
        givenTransits(transit(BUDAPEST, GYOR, 900.0), transit(VIENNA, GRAZ, 1100.0));

        assertThat(finder.findCheapestTransits(BUDAPEST, GRAZ, SIZE)).isEmpty();
        assertThat(finder.findCheapestRoute(BUDAPEST, GRAZ, SIZE)).isEmpty();
        assertThat(finder.findCheapestRoutePrice(BUDAPEST, GRAZ, SIZE)).isNull();
    }

    @Test
    void transitsAreOneWay() {
        givenTransits(transit(BUDAPEST, VIENNA, 2790.0));

        assertThat(finder.findCheapestTransits(VIENNA, BUDAPEST, SIZE)).isEmpty();
    }

    @Test
    void onlyTransitsOfThePackageSizeCount() {
        DepoTransit cheapButForAnotherSize = transit(BUDAPEST, VIENNA, 100.0);
        cheapButForAnotherSize.setPackageSizeId(SIZE + 1);
        givenTransits(cheapButForAnotherSize, transit(BUDAPEST, VIENNA, 2790.0));

        assertThat(finder.findCheapestRoutePrice(BUDAPEST, VIENNA, SIZE)).isEqualTo(2790.0);
    }

    @Test
    void sameDepoNeedsNoTransit() {
        givenTransits();

        assertThat(finder.findCheapestTransits(BUDAPEST, BUDAPEST, SIZE)).hasValueSatisfying(
                route -> assertThat(route).isEmpty());
        assertThat(finder.findCheapestRoutePrice(BUDAPEST, BUDAPEST, SIZE)).isZero();
    }

    private static DepoTransit transit(long origin, long destination, double price) {
        DepoTransitDTO transit = TestFixtures.transit(origin, destination, SIZE);
        transit.setPrice(price);
        return TestFixtures.entity(transit);
    }

    private void givenTransits(DepoTransit... all) {
        when(transits.findAll()).thenReturn(Arrays.asList(all));
    }
}
