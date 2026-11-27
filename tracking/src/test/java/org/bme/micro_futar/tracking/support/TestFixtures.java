package org.bme.micro_futar.tracking.support;

import org.bme.micro_futar.shared.dtos.DepoDTO;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.bme.micro_futar.shared.enums.TransportType;

/** Valid objects with realistic values; a test changes only what its statement is about. */
public final class TestFixtures {

    public static final long CENTRAL_EUROPE_ID = 1L;
    public static final long HUNGARY_ID = 1L;
    public static final long AUSTRIA_ID = 2L;
    public static final long BUDAPEST_ID = 1L;
    public static final long VIENNA_ID = 2L;
    public static final long SMALL_PACKAGE_ID = 1L;
    public static final long BUDAPEST_DEPO_ID = 1L;
    public static final long VIENNA_DEPO_ID = 2L;
    public static final long SHIPMENT_ID = 1042L;

    public static final String SENDER_EMAIL = "kovacs.eszter@postafiok.hu";
    public static final String RECIPIENT_EMAIL = "lukas.gruber@briefkasten.at";
    public static final String PARCEL_NUMBER = "6f1c2a9e-4b7d-4e53-9a18-2d0c5b8e7f41";
    public static final String DELIVERY_ADDRESS = "1070 Bécs, Neubaugasse 25";

    private TestFixtures() {
    }

    public static LocationCountryDTO.LocationCountryDTOBuilder hungary() {
        return LocationCountryDTO.builder()
                .id(HUNGARY_ID)
                .regionId(CENTRAL_EUROPE_ID)
                .name("Magyarország")
                .currencyCode("HUF")
                .isoCode("HU");
    }

    public static LocationCountryDTO.LocationCountryDTOBuilder austria() {
        return LocationCountryDTO.builder()
                .id(AUSTRIA_ID)
                .regionId(CENTRAL_EUROPE_ID)
                .name("Ausztria")
                .currencyCode("EUR")
                .isoCode("AT");
    }

    public static LocationCityDTO.LocationCityDTOBuilder budapest() {
        return LocationCityDTO.builder()
                .id(BUDAPEST_ID)
                .countryId(HUNGARY_ID)
                .name("Budapest")
                .centerLatitude(47.4979)
                .centerLongitude(19.0402)
                .bboxMinLatitude(47.3496)
                .bboxMinLongitude(18.9251)
                .bboxMaxLatitude(47.6131)
                .bboxMaxLongitude(19.3349);
    }

    public static LocationCityDTO.LocationCityDTOBuilder vienna() {
        return LocationCityDTO.builder()
                .id(VIENNA_ID)
                .countryId(AUSTRIA_ID)
                .name("Bécs")
                .centerLatitude(48.2082)
                .centerLongitude(16.3738)
                .bboxMinLatitude(48.1180)
                .bboxMinLongitude(16.1818)
                .bboxMaxLatitude(48.3231)
                .bboxMaxLongitude(16.5775);
    }

    // DepoDTO has no builder, so every call returns a new object to change with setters.
    public static DepoDTO budapestDepo() {
        DepoDTO depo = new DepoDTO();
        depo.setId(BUDAPEST_DEPO_ID);
        depo.setName("Budapest Dél depó");
        depo.setLocationCountryId(HUNGARY_ID);
        depo.setZip("1097");
        depo.setLocationCityId(BUDAPEST_ID);
        depo.setAddress("Gubacsi út 6.");
        depo.setLatitude(47.4639);
        depo.setLongitude(19.0891);
        return depo;
    }

    public static DepoDTO viennaDepo() {
        DepoDTO depo = new DepoDTO();
        depo.setId(VIENNA_DEPO_ID);
        depo.setName("Bécs Inzersdorf depó");
        depo.setLocationCountryId(AUSTRIA_ID);
        depo.setZip("1230");
        depo.setLocationCityId(VIENNA_ID);
        depo.setAddress("Laxenburger Straße 365");
        depo.setLatitude(48.1362);
        depo.setLongitude(16.3611);
        return depo;
    }

    /** A paid small package from Budapest to Vienna, as orders publishes it. */
    public static ShipmentDTO.ShipmentDTOBuilder shipment() {
        return ShipmentDTO.builder()
                .id(SHIPMENT_ID)
                .parcelNumber(PARCEL_NUMBER)
                .senderName("Kovács Eszter")
                .senderEmail(SENDER_EMAIL)
                .senderPhone("+36301847265")
                .senderLocationCountryId(HUNGARY_ID)
                .senderZip("1092")
                .senderLocationCityId(BUDAPEST_ID)
                .senderAddress("Ráday utca 31.")
                .senderLatitude(47.4857)
                .senderLongitude(19.0658)
                .recipientName("Lukas Gruber")
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientPhone("+436641284573")
                .recipientLocationCountryId(AUSTRIA_ID)
                .recipientZip("1070")
                .recipientLocationCityId(VIENNA_ID)
                .recipientAddress("Neubaugasse 25")
                .recipientLatitude(48.2006)
                .recipientLongitude(16.3492)
                .packageSizeId(SMALL_PACKAGE_ID)
                .confirmed(true)
                .maxPrice(4990.0)
                .currencyCode("HUF");
    }

    /** First leg of {@link #shipment()}: from the sender to the Budapest depot. Not yet fulfilled. */
    public static ShipmentRouteDTO.ShipmentRouteDTOBuilder pickupLeg() {
        return ShipmentRouteDTO.builder()
                .id(101L)
                .shipmentId(SHIPMENT_ID)
                .routePartNumber(0)
                .originAddress("1092 Budapest, Ráday utca 31.")
                .destinationDepoId(BUDAPEST_DEPO_ID)
                .transportType(TransportType.ROAD);
    }

    /** Second leg: between the two depots. */
    public static ShipmentRouteDTO.ShipmentRouteDTOBuilder transitLeg() {
        return ShipmentRouteDTO.builder()
                .id(102L)
                .shipmentId(SHIPMENT_ID)
                .routePartNumber(1)
                .originDepoId(BUDAPEST_DEPO_ID)
                .destinationDepoId(VIENNA_DEPO_ID)
                .transportType(TransportType.ROAD);
    }

    /** Last leg: from the Vienna depot to the recipient. */
    public static ShipmentRouteDTO.ShipmentRouteDTOBuilder deliveryLeg() {
        return ShipmentRouteDTO.builder()
                .id(103L)
                .shipmentId(SHIPMENT_ID)
                .routePartNumber(2)
                .originDepoId(VIENNA_DEPO_ID)
                .destinationAddress(DELIVERY_ADDRESS)
                .transportType(TransportType.ROAD);
    }
}
