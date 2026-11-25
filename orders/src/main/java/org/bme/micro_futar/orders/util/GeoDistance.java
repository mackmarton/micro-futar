package org.bme.micro_futar.orders.util;

/** Small pure geo helpers (no Spring, no I/O) for the shipment location check. */
public final class GeoDistance {

    private static final double EARTH_RADIUS_KM = 6371.0088;
    private static final double KM_PER_DEGREE_LATITUDE = 111.32;

    private GeoDistance() {
    }

    /** Great-circle distance in kilometres. */
    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.min(1.0, Math.sqrt(a)));
    }

    /** Whether the point lies inside the rectangle, which is first grown by {@code toleranceKm} on every side. */
    public static boolean isWithinBbox(double lat, double lon,
                                       double minLat, double minLon, double maxLat, double maxLon,
                                       double toleranceKm) {
        double latMargin = toleranceKm / KM_PER_DEGREE_LATITUDE;
        // Meridians converge towards the poles; use the latitude that makes the margin largest (closest to a pole).
        double widestLat = Math.min(89.0, Math.max(Math.abs(minLat), Math.abs(maxLat)));
        double lonMargin = toleranceKm / (KM_PER_DEGREE_LATITUDE * Math.cos(Math.toRadians(widestLat)));
        return lat >= minLat - latMargin && lat <= maxLat + latMargin
                && lon >= minLon - lonMargin && lon <= maxLon + lonMargin;
    }

    public static boolean isValidCoordinate(Double lat, Double lon) {
        return lat != null && lon != null && !lat.isNaN() && !lon.isNaN()
                && lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
    }
}
