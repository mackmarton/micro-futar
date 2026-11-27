package org.bme.micro_futar.logistics.support;

import org.bme.micro_futar.logistics.dtos.CountryBaseFeeDTO;
import org.bme.micro_futar.logistics.dtos.DepoTransitDTO;
import org.bme.micro_futar.logistics.entities.Courier;
import org.bme.micro_futar.logistics.entities.CountryBaseFee;
import org.bme.micro_futar.logistics.entities.Depo;
import org.bme.micro_futar.logistics.entities.DepoTransit;
import org.bme.micro_futar.logistics.entities.LocationCity;
import org.bme.micro_futar.logistics.entities.LocationCountry;
import org.bme.micro_futar.logistics.entities.LocationRegion;
import org.bme.micro_futar.logistics.entities.PackageSize;
import org.bme.micro_futar.logistics.entities.ShipmentRoute;
import org.bme.micro_futar.logistics.entities.ShipmentRouteCourier;
import org.bme.micro_futar.logistics.entities.Vehicle;
import org.bme.micro_futar.logistics.mappers.CountryBaseFeeMapper;
import org.bme.micro_futar.logistics.mappers.CourierMapper;
import org.bme.micro_futar.logistics.mappers.DepoMapper;
import org.bme.micro_futar.logistics.mappers.DepoTransitMapper;
import org.bme.micro_futar.logistics.mappers.LocationCityMapper;
import org.bme.micro_futar.logistics.mappers.LocationCountryMapper;
import org.bme.micro_futar.logistics.mappers.LocationRegionMapper;
import org.bme.micro_futar.logistics.mappers.PackageSizeMapper;
import org.bme.micro_futar.logistics.mappers.VehicleMapper;
import org.bme.micro_futar.shared.dtos.CourierDTO;
import org.bme.micro_futar.shared.dtos.CurrencyDTO;
import org.bme.micro_futar.shared.dtos.DepoDTO;
import org.bme.micro_futar.shared.dtos.LocationCityDTO;
import org.bme.micro_futar.shared.dtos.LocationCountryDTO;
import org.bme.micro_futar.shared.dtos.LocationRegionDTO;
import org.bme.micro_futar.shared.dtos.PackageSizeDTO;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.dtos.VehicleDTO;
import org.bme.micro_futar.shared.enums.CourierType;
import org.bme.micro_futar.shared.enums.TransportType;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

/**
 * Valid objects with realistic values; a test changes only what its statement is about. Logistics owns the master
 * data and the database numbers it, so references to it are parameters. The entity forms are not yet saved.
 */
public final class TestFixtures {

    /** Ids for tests without a database, and for the sender of a shipment message that is not planned. */
    public static final long HUNGARY_ID = 1L;
    public static final long AUSTRIA_ID = 2L;
    public static final long BUDAPEST_ID = 1L;
    public static final long VIENNA_ID = 2L;
    public static final long SMALL_PACKAGE_ID = 1L;

    public static final String ADMIN_EMAIL = "szabo.gergely@microfutar.hu";
    public static final String DISPATCHER_EMAIL = "toth.anna@microfutar.hu";
    public static final String SENDER_EMAIL = "kovacs.eszter@postafiok.hu";
    public static final String RECIPIENT_EMAIL = "lukas.gruber@briefkasten.at";
    public static final String PARCEL_NUMBER = "6f1c2a9e-4b7d-4e53-9a18-2d0c5b8e7f41";

    private TestFixtures() {
    }

    public static LocationRegionDTO.LocationRegionDTOBuilder centralEurope() {
        return LocationRegionDTO.builder()
                .name("Közép-Európa");
    }

    public static LocationCountryDTO.LocationCountryDTOBuilder hungary(Long regionId) {
        return LocationCountryDTO.builder()
                .regionId(regionId)
                .name("Magyarország")
                .currencyCode("HUF")
                .isoCode("HU");
    }

    /** Priced in forints, because only HUF is seeded. */
    public static LocationCountryDTO.LocationCountryDTOBuilder austria(Long regionId) {
        return LocationCountryDTO.builder()
                .regionId(regionId)
                .name("Ausztria")
                .currencyCode("HUF")
                .isoCode("AT");
    }

    /** Without geodata, as the admin sends it. */
    public static LocationCityDTO.LocationCityDTOBuilder budapest(Long countryId) {
        return LocationCityDTO.builder()
                .countryId(countryId)
                .name("Budapest");
    }

    public static LocationCityDTO.LocationCityDTOBuilder vienna(Long countryId) {
        return LocationCityDTO.builder()
                .countryId(countryId)
                .name("Bécs");
    }

    public static PackageSizeDTO.PackageSizeDTOBuilder smallPackage() {
        return PackageSizeDTO.builder()
                .name("S")
                .maxLength(35.0);
    }

    public static CurrencyDTO.CurrencyDTOBuilder euro() {
        return CurrencyDTO.builder()
                .code("EUR")
                .name("Euró")
                .symbol("€");
    }

    public static DepoDTO budapestDepo(Long countryId, Long cityId) {
        DepoDTO depo = new DepoDTO();
        depo.setName("Budapest Dél depó");
        depo.setLocationCountryId(countryId);
        depo.setLocationCityId(cityId);
        depo.setZip("1097");
        depo.setAddress("Gubacsi út 6.");
        depo.setLatitude(47.4639);
        depo.setLongitude(19.0891);
        return depo;
    }

    public static DepoDTO viennaDepo(Long countryId, Long cityId) {
        DepoDTO depo = new DepoDTO();
        depo.setName("Bécs Inzersdorf depó");
        depo.setLocationCountryId(countryId);
        depo.setLocationCityId(cityId);
        depo.setZip("1230");
        depo.setAddress("Laxenburger Straße 365");
        depo.setLatitude(48.1362);
        depo.setLongitude(16.3611);
        return depo;
    }

    /** A road leg of a small package, priced in forints. */
    public static DepoTransitDTO transit(Long originDepoId, Long destinationDepoId, Long packageSizeId) {
        DepoTransitDTO transit = new DepoTransitDTO();
        transit.setOriginDepoId(originDepoId);
        transit.setDestinationDepoId(destinationDepoId);
        transit.setPackageSizeId(packageSizeId);
        transit.setTransportType(TransportType.ROAD);
        transit.setPrice(2790.0);
        return transit;
    }

    public static CountryBaseFeeDTO baseFee(Long countryId, Long packageSizeId) {
        CountryBaseFeeDTO fee = new CountryBaseFeeDTO();
        fee.setCountryId(countryId);
        fee.setPackageSizeId(packageSizeId);
        fee.setPrice(1500.0);
        return fee;
    }

    /** A van; cargo space in cubic centimetres. */
    public static VehicleDTO vehicle() {
        VehicleDTO vehicle = new VehicleDTO();
        vehicle.setRegistrationNumber("AEKR-482");
        vehicle.setMaximumPackableVolume(3_000_000.0);
        return vehicle;
    }

    /** A road courier delivering around the depo. */
    public static CourierDTO courier(Long depoId, Long vehicleId) {
        CourierDTO courier = new CourierDTO();
        courier.setName("Tóth Gábor");
        courier.setEmail("toth.gabor@microfutar.hu");
        courier.setTelephone("+36209184736");
        courier.setDepoId(depoId);
        courier.setVehicleId(vehicleId);
        courier.setQualifiedFor(TransportType.ROAD);
        courier.setCourierType(CourierType.DELIVERY);
        return courier;
    }

    /** A paid and confirmed small package from Budapest to Vienna, as orders announces it. */
    public static ShipmentDTO.ShipmentDTOBuilder shipment(long id) {
        return ShipmentDTO.builder()
                .id(id)
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
                .parcelNumber(PARCEL_NUMBER)
                .maxPrice(4990.0)
                .currencyCode("HUF")
                .confirmed(true);
    }

    /** An open leg of a shipment; the test sets where it leads. */
    public static ShipmentRoute route(long shipmentId, int partNumber) {
        ShipmentRoute route = new ShipmentRoute();
        route.setShipmentId(shipmentId);
        route.setRoutePartNumber(partNumber);
        route.setTransportType(TransportType.ROAD);
        return route;
    }

    /** A live assignment: not failed, not picked up, not canceled. */
    public static ShipmentRouteCourier assignment(Long routeId, LocalDate date) {
        ShipmentRouteCourier assignment = new ShipmentRouteCourier();
        assignment.setCourierId(1L);
        assignment.setShipmentRouteId(routeId);
        assignment.setDateAssignedFor(date);
        return assignment;
    }

    public static LocationRegion entity(LocationRegionDTO region) {
        return Mappers.getMapper(LocationRegionMapper.class).toEntity(region);
    }

    public static LocationCountry entity(LocationCountryDTO country) {
        return Mappers.getMapper(LocationCountryMapper.class).toEntity(country);
    }

    public static LocationCity entity(LocationCityDTO city) {
        return Mappers.getMapper(LocationCityMapper.class).toEntity(city);
    }

    public static PackageSize entity(PackageSizeDTO packageSize) {
        return Mappers.getMapper(PackageSizeMapper.class).toEntity(packageSize);
    }

    public static Depo entity(DepoDTO depo) {
        return Mappers.getMapper(DepoMapper.class).toEntity(depo);
    }

    public static DepoTransit entity(DepoTransitDTO transit) {
        return Mappers.getMapper(DepoTransitMapper.class).toEntity(transit);
    }

    public static CountryBaseFee entity(CountryBaseFeeDTO fee) {
        return Mappers.getMapper(CountryBaseFeeMapper.class).toEntity(fee);
    }

    public static Vehicle entity(VehicleDTO vehicle) {
        return Mappers.getMapper(VehicleMapper.class).toEntity(vehicle);
    }

    public static Courier entity(CourierDTO courier) {
        return Mappers.getMapper(CourierMapper.class).toEntity(courier);
    }
}
