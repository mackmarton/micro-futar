package org.bme.micro_futar.orders.support;

import org.bme.micro_futar.orders.mappers.LocationCityMapper;
import org.bme.micro_futar.orders.entities.LocationCity;
import org.bme.micro_futar.orders.entities.Shipment;
import org.bme.micro_futar.orders.mappers.ShipmentMapper;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
import org.bme.micro_futar.shared.dtos.CurrencyDTO;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.bme.micro_futar.shared.dtos.LocationRegionDTO;
import org.bme.micro_futar.shared.dtos.PackageSizeDTO;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.enums.PaymentStatus;
import org.mapstruct.factory.Mappers;

/** Valid objects with realistic values; a test changes only what its statement is about. */
public final class TestFixtures {

    public static final long CENTRAL_EUROPE_ID = 1L;
    public static final long HUNGARY_ID = 1L;
    public static final long AUSTRIA_ID = 2L;
    public static final long BUDAPEST_ID = 1L;
    public static final long VIENNA_ID = 2L;
    public static final long SMALL_PACKAGE_ID = 1L;

    public static final String SENDER_EMAIL = "kovacs.eszter@postafiok.hu";
    public static final String RECIPIENT_EMAIL = "lukas.gruber@briefkasten.at";
    public static final String PARCEL_NUMBER = "6f1c2a9e-4b7d-4e53-9a18-2d0c5b8e7f41";

    private TestFixtures() {
    }

    public static LocationRegionDTO.LocationRegionDTOBuilder centralEurope() {
        return LocationRegionDTO.builder()
                .id(CENTRAL_EUROPE_ID)
                .name("Közép-Európa");
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

    public static PackageSizeDTO.PackageSizeDTOBuilder smallPackage() {
        return PackageSizeDTO.builder()
                .id(SMALL_PACKAGE_ID)
                .name("S")
                .maxLength(35.0);
    }

    public static CurrencyDTO.CurrencyDTOBuilder forint() {
        return CurrencyDTO.builder()
                .id(1L)
                .code("HUF")
                .name("Magyar forint")
                .symbol("Ft");
    }

    public static LocationCity entity(LocationCityDTO city) {
        return Mappers.getMapper(LocationCityMapper.class).toEntity(city);
    }

    /** The price table row matching {@link #shipmentRequest()}, in forints. */
    public static CountryPriceDTO.CountryPriceDTOBuilder countryPrice() {
        return CountryPriceDTO.builder()
                .id(1L)
                .originCountryId(HUNGARY_ID)
                .destinationCountryId(AUSTRIA_ID)
                .packageSizeId(SMALL_PACKAGE_ID)
                .minPrice(3490.0)
                .maxPrice(4990.0);
    }

    /** A small package from Budapest to Vienna, as a client would send it. */
    public static ShipmentDTO.ShipmentDTOBuilder shipmentRequest() {
        return ShipmentDTO.builder()
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
                .packageSizeId(SMALL_PACKAGE_ID);
    }

    /** The stored form of {@link #shipmentRequest()}, waiting for payment; the id is left to the database. */
    public static Shipment pendingShipment() {
        return Mappers.getMapper(ShipmentMapper.class).toEntity(shipmentRequest()
                .parcelNumber(PARCEL_NUMBER)
                .maxPrice(4990.0)
                .currencyCode("HUF")
                .paymentStatus(PaymentStatus.PENDING)
                .build());
    }
}
