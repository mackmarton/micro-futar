package org.bme.micro_futar.logistics.services.geocoding;

/** Centre and (optional) bounding box of a settlement. The bbox values are all null or all set. */
public record GeocodeResult(
        double centerLatitude,
        double centerLongitude,
        Double bboxMinLatitude,
        Double bboxMinLongitude,
        Double bboxMaxLatitude,
        Double bboxMaxLongitude) {

    public boolean hasBbox() {
        return bboxMinLatitude != null;
    }
}
