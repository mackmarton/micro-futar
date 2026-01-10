package org.bme.micro_futar.orders.services;

import org.bme.micro_futar.orders.entities.Shipment;
import org.bme.micro_futar.orders.exceptions.NoServiceException;
import org.bme.micro_futar.orders.kafka.ShipmentProducer;
import org.bme.micro_futar.orders.mappers.ShipmentMapper;
import org.bme.micro_futar.orders.repositories.ShipmentRepository;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
                .recipientName("Jane Smith")
                .recipientEmail("jane@example.com")
                .recipientPhone("+43201234567")
                .recipientLocationCountryId(2L)
                .recipientZip("5678")
                .recipientLocationCityId(2L)
                .recipientAddress("Test Avenue 2")
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
        shipmentEntity.setRecipientName("Jane Smith");
        shipmentEntity.setRecipientEmail("jane@example.com");
        shipmentEntity.setRecipientPhone("+43201234567");
        shipmentEntity.setRecipientLocationCountryId("2");
        shipmentEntity.setRecipientZip("5678");
        shipmentEntity.setRecipientLocationCityId("2");
        shipmentEntity.setRecipientAddress("Test Avenue 2");
        shipmentEntity.setPackageSizeId(1L);
        shipmentEntity.setPrice(25.99);
        shipmentEntity.setConfirmed(false);

        countryPriceDTO = CountryPriceDTO.builder()
                .id(1L)
                .originCountryId(1L)
                .destinationCountryId(2L)
                .packageSizeId(1L)
                .build();
    }

    @Test
    void testNewShipment_Success() {
        when(countryPriceService.findPriceByCountriesAndSize(1L, 2L, 1L))
                .thenReturn(Optional.of(countryPriceDTO));
        when(shipmentMapper.toEntity(any(ShipmentDTO.class))).thenReturn(shipmentEntity);
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipmentEntity);

        ShipmentDTO expectedDTO = ShipmentDTO.builder()
                .id(1L)
                .senderName("John Doe")
                .senderEmail("john@example.com")
                .senderPhone("+36301234567")
                .senderLocationCountryId(1L)
                .senderZip("1234")
                .senderLocationCityId(1L)
                .senderAddress("Test Street 1")
                .recipientName("Jane Smith")
                .recipientEmail("jane@example.com")
                .recipientPhone("+43201234567")
                .recipientLocationCountryId(2L)
                .recipientZip("5678")
                .recipientLocationCityId(2L)
                .recipientAddress("Test Avenue 2")
                .packageSizeId(1L)
                .price(25.99)
                .confirmed(false)
                .build();
        when(shipmentMapper.toDTO(shipmentEntity)).thenReturn(expectedDTO);

        ShipmentDTO result = shipmentService.newShipment(shipmentDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPrice()).isEqualTo(25.99);
        assertThat(result.isConfirmed()).isFalse();

        verify(countryPriceService).findPriceByCountriesAndSize(1L, 2L, 1L);
        verify(shipmentMapper).toEntity(any(ShipmentDTO.class));
        verify(shipmentRepository).save(any(Shipment.class));
        verify(shipmentMapper).toDTO(shipmentEntity);
        verify(shipmentProducer).sendShipmentToTopic(any(ShipmentDTO.class));
    }

    @Test
    void testNewShipment_NoServiceException() {
        when(countryPriceService.findPriceByCountriesAndSize(1L, 2L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentService.newShipment(shipmentDTO))
                .isInstanceOf(NoServiceException.class)
                .hasMessageContaining("There is no service between the origin and destination countries");

        verify(countryPriceService).findPriceByCountriesAndSize(1L, 2L, 1L);
        verify(shipmentRepository, never()).save(any());
        verify(shipmentProducer, never()).sendShipmentToTopic(any());
    }

    @Test
    void testConfirm_Success() {
        shipmentEntity.setConfirmed(false);
        shipmentEntity.setParcelNumber(null);

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipmentEntity));

        Shipment confirmedEntity = new Shipment();
        confirmedEntity.setId(1L);
        confirmedEntity.setSenderName("John Doe");
        confirmedEntity.setSenderEmail("john@example.com");
        confirmedEntity.setSenderPhone("+36301234567");
        confirmedEntity.setSenderLocationCountryId(1L);
        confirmedEntity.setSenderZip("1234");
        confirmedEntity.setSenderLocationCityId(1L);
        confirmedEntity.setSenderAddress("Test Street 1");
        confirmedEntity.setRecipientName("Jane Smith");
        confirmedEntity.setRecipientEmail("jane@example.com");
        confirmedEntity.setRecipientPhone("+43201234567");
        confirmedEntity.setRecipientLocationCountryId("2");
        confirmedEntity.setRecipientZip("5678");
        confirmedEntity.setRecipientLocationCityId("2");
        confirmedEntity.setRecipientAddress("Test Avenue 2");
        confirmedEntity.setPackageSizeId(1L);
        confirmedEntity.setPrice(25.99);
        confirmedEntity.setConfirmed(true);
        confirmedEntity.setParcelNumber(UUID.randomUUID().toString());

        when(shipmentRepository.save(any(Shipment.class))).thenReturn(confirmedEntity);

        ShipmentDTO confirmedDTO = ShipmentDTO.builder()
                .id(1L)
                .senderName("John Doe")
                .senderEmail("john@example.com")
                .senderPhone("+36301234567")
                .senderLocationCountryId(1L)
                .senderZip("1234")
                .senderLocationCityId(1L)
                .senderAddress("Test Street 1")
                .recipientName("Jane Smith")
                .recipientEmail("jane@example.com")
                .recipientPhone("+43201234567")
                .recipientLocationCountryId(2L)
                .recipientZip("5678")
                .recipientLocationCityId(2L)
                .recipientAddress("Test Avenue 2")
                .packageSizeId(1L)
                .price(25.99)
                .confirmed(true)
                .parcelNumber(confirmedEntity.getParcelNumber())
                .build();
        when(shipmentMapper.toDTO(confirmedEntity)).thenReturn(confirmedDTO);

        ShipmentDTO result = shipmentService.confirm(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.isConfirmed()).isTrue();
        assertThat(result.getParcelNumber()).isNotNull();

        verify(shipmentRepository).findById(1L);
        verify(shipmentRepository).save(any(Shipment.class));
        verify(shipmentMapper).toDTO(confirmedEntity);
        verify(shipmentProducer).sendShipmentToTopic(any(ShipmentDTO.class));
    }

    @Test
    void testConfirm_ShipmentNotFound() {
        when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentService.confirm(999L))
                .isInstanceOf(RuntimeException.class);

        verify(shipmentRepository).findById(999L);
        verify(shipmentRepository, never()).save(any());
        verify(shipmentProducer, never()).sendShipmentToTopic(any());
    }
}
