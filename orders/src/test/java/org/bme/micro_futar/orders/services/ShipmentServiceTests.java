package org.bme.micro_futar.orders.services;

import org.bme.micro_futar.orders.dtos.CreateShipmentResponseDTO;
import org.bme.micro_futar.orders.entities.LocationCountry;
import org.bme.micro_futar.orders.entities.Shipment;
import org.bme.micro_futar.orders.exceptions.PriceNotAvailableException;
import org.bme.micro_futar.orders.kafka.ShipmentNotificationProducer;
import org.bme.micro_futar.orders.kafka.ShipmentProducer;
import org.bme.micro_futar.orders.mappers.ShipmentMapper;
import org.bme.micro_futar.orders.repositories.LocationCountryRepository;
import org.bme.micro_futar.orders.repositories.ShipmentRepository;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
import org.bme.micro_futar.shared.dtos.PaymentRequestedEventDTO;
import org.bme.micro_futar.shared.dtos.PaymentStatusEventDTO;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.enums.NotificationType;
import org.bme.micro_futar.shared.enums.PaymentStatus;
import org.bme.micro_futar.shared.utils.PaymentTokens;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTests {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private CountryPriceService countryPriceService;

    @Mock
    private ShipmentMapper shipmentMapper;

    @Mock
    private ShipmentProducer shipmentProducer;

    @Mock
    private ShipmentNotificationProducer shipmentNotificationProducer;

    @Mock
    private LocationCountryRepository locationCountryRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ShipmentService shipmentService;

    private ShipmentDTO shipmentDTO;
    private Shipment shipmentEntity;
    private CountryPriceDTO countryPriceDTO;

    @BeforeEach
    void setUp() {
        shipmentDTO = ShipmentDTO.builder()
                .senderName("John Doe")
                .senderEmail("john@example.com")
                .senderPhone("+36301234567")
                .senderLocationCountryId(1L)
                .senderZip("1234")
                .senderLocationCityId(1L)
                .senderAddress("Test Street 1")
                .senderLatitude(1.234)
                .senderLongitude(1.234)
                .recipientName("Jane Smith")
                .recipientEmail("jane@example.com")
                .recipientPhone("+43201234567")
                .recipientLocationCountryId(2L)
                .recipientZip("5678")
                .recipientLocationCityId(2L)
                .recipientAddress("Test Avenue 2")
                .recipientLatitude(1.234)
                .recipientLongitude(1.234)
                .packageSizeId(1L)
                .build();

        shipmentEntity = new Shipment();
        shipmentEntity.setId(1L);
        shipmentEntity.setSenderName("John Doe");
        shipmentEntity.setSenderEmail("john@example.com");
        shipmentEntity.setSenderPhone("+36301234567");
        shipmentEntity.setSenderLocationCountryId(1L);
        shipmentEntity.setSenderZip("1234");
        shipmentEntity.setSenderLocationCityId(1L);
        shipmentEntity.setSenderAddress("Test Street 1");
        shipmentEntity.setSenderLatitude(1.234);
        shipmentEntity.setSenderLongitude(1.234);
        shipmentEntity.setRecipientName("Jane Smith");
        shipmentEntity.setRecipientEmail("jane@example.com");
        shipmentEntity.setRecipientPhone("+43201234567");
        shipmentEntity.setRecipientLocationCountryId("2");
        shipmentEntity.setRecipientZip("5678");
        shipmentEntity.setRecipientLocationCityId("2");
        shipmentEntity.setRecipientAddress("Test Avenue 2");
        shipmentEntity.setRecipientLatitude(1.234);
        shipmentEntity.setRecipientLongitude(1.234);
        shipmentEntity.setPackageSizeId(1L);
        shipmentEntity.setPrice(25.99);
        shipmentEntity.setConfirmed(false);

        countryPriceDTO = CountryPriceDTO.builder()
                .id(1L)
                .originCountryId(1L)
                .destinationCountryId(2L)
                .packageSizeId(1L)
                .minPrice(10.0)
                .maxPrice(20.0)
                .build();
    }

    @Test
    void testNewShipment_Success() {
        when(countryPriceService.findPriceByCountriesAndSize(1L, 2L, 1L)).thenReturn(Optional.of(countryPriceDTO));
        LocationCountry senderCountry = new LocationCountry();
        senderCountry.setCurrencyCode("EUR");
        when(locationCountryRepository.findById(1L)).thenReturn(Optional.of(senderCountry));
        when(shipmentMapper.toEntity(any(ShipmentDTO.class))).thenReturn(shipmentEntity);
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipmentEntity);
        ShipmentDTO savedDTO = ShipmentDTO.builder()
                .id(1L)
                .senderName("John Doe")
                .senderEmail("john@example.com")
                .senderPhone("+36301234567")
                .senderLocationCountryId(1L)
                .senderZip("1234")
                .senderLocationCityId(1L)
                .senderAddress("Test Street 1")
                .senderLatitude(1.234)
                .senderLongitude(1.234)
                .recipientName("Jane Smith")
                .recipientEmail("jane@example.com")
                .recipientPhone("+43201234567")
                .recipientLocationCountryId(2L)
                .recipientZip("5678")
                .recipientLocationCityId(2L)
                .recipientAddress("Test Avenue 2")
                .recipientLatitude(1.234)
                .recipientLongitude(1.234)
                .packageSizeId(1L)
                .maxPrice(20.0)
                .currencyCode("EUR")
                .paymentStatus(PaymentStatus.PENDING)
                .parcelNumber("parcel-1")
                .build();
        when(shipmentMapper.toDTO(shipmentEntity)).thenReturn(savedDTO);
        shipmentDTO.setPrice(0.01);

        CreateShipmentResponseDTO result = shipmentService.newShipment(shipmentDTO);

        assertThat(result.getShipment().getId()).isEqualTo(1L);
        assertThat(result.getPaymentToken()).isNotBlank();
        // The client-sent price is ignored; the max price comes from the price table.
        verify(shipmentMapper).toEntity(argThat(dto -> dto.getPrice() == null
                && dto.getMaxPrice() == 20.0
                && "EUR".equals(dto.getCurrencyCode())
                && dto.getPaymentStatus() == PaymentStatus.PENDING
                && !dto.isConfirmed()));
        verify(eventPublisher).publishEvent(argThat((PaymentRequestedEventDTO request) ->
                request.getShipmentId().equals(1L)
                        && request.getMaxAmount().compareTo(new BigDecimal("20.0")) == 0
                        && request.getCurrencyCode().equals("EUR")
                        && request.getPaymentTokenHash().equals(PaymentTokens.hash(result.getPaymentToken()))));
        // Logistics and the sender only hear about it once the payment is authorized.
        verifyNoInteractions(shipmentProducer, shipmentNotificationProducer);
    }

    @Test
    void testNewShipment_NoPriceForRoute() {
        when(countryPriceService.findPriceByCountriesAndSize(anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentService.newShipment(shipmentDTO))
                .isInstanceOf(PriceNotAvailableException.class);
        verify(shipmentRepository, never()).save(any());
    }

    @Test
    void testApplyPaymentStatus_AuthorizedHandsShipmentToLogistics() {
        shipmentEntity.setPaymentStatus(PaymentStatus.PENDING);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipmentEntity));
        when(shipmentRepository.save(shipmentEntity)).thenReturn(shipmentEntity);
        shipmentDTO.setId(1L);
        ShipmentDTO dto = shipmentDTO;
        when(shipmentMapper.toDTO(shipmentEntity)).thenReturn(dto);

        shipmentService.applyPaymentStatus(
                PaymentStatusEventDTO.of(1L, PaymentStatus.AUTHORIZED, new BigDecimal("20"), "HUF"));

        assertThat(shipmentEntity.getPaymentStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(shipmentEntity.isConfirmed()).isTrue();
        verify(shipmentProducer).sendShipmentToTopic(dto);
        verify(shipmentNotificationProducer).sendNotificationToTopic(argThat(event ->
                event.getType() == NotificationType.SHIPMENT_CREATED
                        && event.getDedupKey().equals("SHIPMENT_CREATED:shipment:1")));
    }

    @Test
    void testApplyPaymentStatus_RedeliveredAuthorizedIsIgnored() {
        shipmentEntity.setPaymentStatus(PaymentStatus.PAID);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipmentEntity));

        shipmentService.applyPaymentStatus(
                PaymentStatusEventDTO.of(1L, PaymentStatus.AUTHORIZED, new BigDecimal("20"), "HUF"));

        assertThat(shipmentEntity.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        verifyNoInteractions(shipmentProducer, shipmentNotificationProducer);
    }

    @Test
    void testApplyPaymentStatus_PaidStoresFinalPrice() {
        shipmentEntity.setPaymentStatus(PaymentStatus.AUTHORIZED);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipmentEntity));

        shipmentService.applyPaymentStatus(
                PaymentStatusEventDTO.of(1L, PaymentStatus.PAID, new BigDecimal("14.5"), "HUF"));

        assertThat(shipmentEntity.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(shipmentEntity.getPrice()).isEqualTo(14.5);
    }

    @Test
    void testApplyPaymentStatus_ExpiredLeavesShipmentUnconfirmed() {
        shipmentEntity.setPaymentStatus(PaymentStatus.PENDING);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipmentEntity));

        shipmentService.applyPaymentStatus(PaymentStatusEventDTO.of(1L, PaymentStatus.EXPIRED, null, "HUF"));

        assertThat(shipmentEntity.getPaymentStatus()).isEqualTo(PaymentStatus.EXPIRED);
        assertThat(shipmentEntity.isConfirmed()).isFalse();
        verifyNoInteractions(shipmentProducer, shipmentNotificationProducer);
    }

}
