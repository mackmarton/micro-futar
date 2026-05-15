package org.bme.micro_futar.orders.services;

import org.bme.micro_futar.orders.entities.Shipment;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
                .price(25.99)
                .confirmed(false)
                .build();
        when(shipmentMapper.toDTO(shipmentEntity)).thenReturn(expectedDTO);

        ShipmentDTO result = shipmentService.newShipment(shipmentDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPrice()).isEqualTo(25.99);
        assertThat(result.isConfirmed()).isFalse();

        verify(shipmentMapper).toEntity(any(ShipmentDTO.class));
        verify(shipmentRepository).save(any(Shipment.class));
        verify(shipmentMapper).toDTO(shipmentEntity);
        verify(shipmentProducer).sendShipmentToTopic(any(ShipmentDTO.class));
    }

}
