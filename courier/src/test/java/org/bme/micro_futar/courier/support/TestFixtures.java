package org.bme.micro_futar.courier.support;

import org.bme.micro_futar.shared.dtos.CourierDTO;
import org.bme.micro_futar.shared.dtos.DepoDTO;
import org.bme.micro_futar.shared.dtos.PackageSizeDTO;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.bme.micro_futar.shared.dtos.ShipmentRouteDTO;
import org.bme.micro_futar.shared.dtos.VehicleDTO;
import org.bme.micro_futar.shared.enums.CourierType;
import org.bme.micro_futar.shared.enums.PaymentStatus;
import org.bme.micro_futar.shared.enums.TransportType;

import java.time.LocalDate;

/**
 * Valid objects with realistic values, in the form the owning service publishes them; a test changes only what its
 * statement is about.
 */
public final class TestFixtures {

    public static final long HUNGARY_ID = 1L;
    public static final long AUSTRIA_ID = 2L;
    public static final long BUDAPEST_ID = 1L;
    public static final long VIENNA_ID = 2L;
    public static final long SMALL_PACKAGE_ID = 1L;
    public static final long BUDAPEST_DEPO_ID = 1L;
    public static final long VIENNA_DEPO_ID = 2L;

    public static final long COURIER_ID = 1L;
    public static final long OTHER_COURIER_ID = 2L;
    public static final long VEHICLE_ID = 1L;
    public static final long SHIPMENT_ID = 1042L;
    public static final long PICKUP_ROUTE_ID = 1L;
    public static final long DELIVERY_ROUTE_ID = 2L;

    public static final String COURIER_EMAIL = "toth.gabor@microfutar.hu";
    public static final String OTHER_COURIER_EMAIL = "nagy.reka@microfutar.hu";
    public static final String SENDER_EMAIL = "kovacs.eszter@postafiok.hu";
    public static final String RECIPIENT_EMAIL = "lukas.gruber@briefkasten.at";
    public static final String PARCEL_NUMBER = "6f1c2a9e-4b7d-4e53-9a18-2d0c5b8e7f41";

    private TestFixtures() {
    }

    /** Drives the van of {@link #vehicle()} out of the Budapest depo. */
    public static CourierDTO courier() {
        CourierDTO courier = new CourierDTO();
        courier.setId(COURIER_ID);
        courier.setName("Tóth Gábor");
        courier.setEmail(COURIER_EMAIL);
        courier.setTelephone("+36209184736");
        courier.setVehicleId(VEHICLE_ID);
        courier.setQualifiedFor(TransportType.ROAD);
        courier.setCourierType(CourierType.DELIVERY);
        courier.setDepoId(BUDAPEST_DEPO_ID);
        return courier;
    }

    public static CourierDTO otherCourier() {
        CourierDTO courier = courier();
        courier.setId(OTHER_COURIER_ID);
        courier.setName("Nagy Réka");
        courier.setEmail(OTHER_COURIER_EMAIL);
        courier.setTelephone("+36703352918");
        courier.setVehicleId(null);
        return courier;
    }

    /** A van; cargo space in cubic centimetres. */
    public static VehicleDTO vehicle() {
        VehicleDTO vehicle = new VehicleDTO();
        vehicle.setId(VEHICLE_ID);
        vehicle.setRegistrationNumber("AEKR-482");
        vehicle.setMaximumPackableVolume(3_000_000.0);
        return vehicle;
    }

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

    public static PackageSizeDTO.PackageSizeDTOBuilder smallPackage() {
        return PackageSizeDTO.builder()
                .id(SMALL_PACKAGE_ID)
                .name("S")
                .maxLength(35.0);
    }

    /** A confirmed small package from Budapest to Vienna. */
    public static ShipmentDTO.ShipmentDTOBuilder shipment() {
        return ShipmentDTO.builder()
                .id(SHIPMENT_ID)
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
                .parcelNumber(PARCEL_NUMBER)
                .maxPrice(4990.0)
                .currencyCode("HUF")
                .paymentStatus(PaymentStatus.AUTHORIZED);
    }

    /** The first leg: from the sender of {@link #shipment()} to the Budapest depo. */
    public static ShipmentRouteDTO.ShipmentRouteDTOBuilder pickupLeg() {
        return ShipmentRouteDTO.builder()
                .id(PICKUP_ROUTE_ID)
                .shipmentId(SHIPMENT_ID)
                .originAddress("Ráday utca 31.")
                .destinationDepoId(BUDAPEST_DEPO_ID)
                .routePartNumber(0)
                .transportType(TransportType.ROAD);
    }

    /** The last leg: from the Vienna depo to the recipient of {@link #shipment()}. */
    public static ShipmentRouteDTO.ShipmentRouteDTOBuilder deliveryLeg() {
        return ShipmentRouteDTO.builder()
                .id(DELIVERY_ROUTE_ID)
                .shipmentId(SHIPMENT_ID)
                .originDepoId(VIENNA_DEPO_ID)
                .destinationAddress("Neubaugasse 25")
                .routePartNumber(2)
                .transportType(TransportType.ROAD);
    }

    /** An assignment of {@link #courier()} for today. */
    public static ShipmentRouteCourierDTO.ShipmentRouteCourierDTOBuilder assignment(long id, long routeId) {
        return ShipmentRouteCourierDTO.builder()
                .id(id)
                .courierId(COURIER_ID)
                .shipmentRouteId(routeId)
                .dateAssignedFor(LocalDate.now());
    }
}
