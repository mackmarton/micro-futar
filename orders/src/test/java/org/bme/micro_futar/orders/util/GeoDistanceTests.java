package org.bme.micro_futar.orders.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeoDistanceTests {

    // City centres
    private static final double[] BUDAPEST = {47.4979, 19.0402};
    private static final double[] DEBRECEN = {47.5316, 21.6273};
    private static final double[] VIENNA = {48.2082, 16.3738};

    @Test
    void budapestToDebrecenIsAbout194Km() {
        double km = GeoDistance.haversineKm(BUDAPEST[0], BUDAPEST[1], DEBRECEN[0], DEBRECEN[1]);
        assertThat(km).isBetween(190.0, 198.0);
    }

    @Test
    void budapestToViennaIsAbout215Km() {
        double km = GeoDistance.haversineKm(BUDAPEST[0], BUDAPEST[1], VIENNA[0], VIENNA[1]);
        assertThat(km).isBetween(210.0, 220.0);
    }

    @Test
    void distanceToSelfIsZero() {
        assertThat(GeoDistance.haversineKm(47.5, 19.0, 47.5, 19.0)).isZero();
    }

    @Test
    void pointInsideBboxIsWithinEvenWithoutTolerance() {
        assertThat(GeoDistance.isWithinBbox(47.5, 19.05, 47.35, 18.9, 47.65, 19.3, 0)).isTrue();
    }

    @Test
    void pointJustOutsideBboxIsAcceptedWithinTolerance() {
        // ~1.1 km north of the northern edge
        assertThat(GeoDistance.isWithinBbox(47.66, 19.1, 47.35, 18.9, 47.65, 19.3, 0)).isFalse();
        assertThat(GeoDistance.isWithinBbox(47.66, 19.1, 47.35, 18.9, 47.65, 19.3, 3)).isTrue();
    }

    @Test
    void pointBeyondToleranceIsRejectedOnEveryEdge() {
        assertThat(GeoDistance.isWithinBbox(47.70, 19.1, 47.35, 18.9, 47.65, 19.3, 3)).isFalse(); // ~5.5 km north
        assertThat(GeoDistance.isWithinBbox(47.30, 19.1, 47.35, 18.9, 47.65, 19.3, 3)).isFalse(); // south
        assertThat(GeoDistance.isWithinBbox(47.5, 19.40, 47.35, 18.9, 47.65, 19.3, 3)).isFalse(); // east, ~7.5 km
        assertThat(GeoDistance.isWithinBbox(47.5, 18.80, 47.35, 18.9, 47.65, 19.3, 3)).isFalse(); // west
    }

    @Test
    void missingNotANumberAndOutOfRangeCoordinatesAreInvalid() {
        assertThat(GeoDistance.isValidCoordinate(47.5, 19.0)).isTrue();
        assertThat(GeoDistance.isValidCoordinate(90.0, 180.0)).isTrue();
        assertThat(GeoDistance.isValidCoordinate(90.1, 0.0)).isFalse();
        assertThat(GeoDistance.isValidCoordinate(0.0, -180.1)).isFalse();
        assertThat(GeoDistance.isValidCoordinate(null, 19.0)).isFalse();
        assertThat(GeoDistance.isValidCoordinate(47.5, null)).isFalse();
        assertThat(GeoDistance.isValidCoordinate(Double.NaN, 19.0)).isFalse();
    }
}
