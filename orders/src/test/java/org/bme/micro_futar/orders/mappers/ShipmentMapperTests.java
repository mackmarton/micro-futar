package org.bme.micro_futar.orders.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.bme.micro_futar.orders.entities.Shipment;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ShipmentMapperTests {

    private final ShipmentMapper mapper = Mappers.getMapper(ShipmentMapper.class);

    @Test
    void locationIdsKeepLongValuesInBothDirections() {
        ShipmentDTO dto = ShipmentDTO.builder()
                .senderName("John Doe")
                .senderEmail("john@example.com")
                .senderPhone("+36301234567")
                .senderLocationCountryId(1L)
                .senderZip("1234")
                .senderLocationCityId(2L)
                .senderAddress("Test Street 1")
                .senderLatitude(1.234)
                .senderLongitude(1.234)
                .recipientName("Jane Smith")
                .recipientEmail("jane@example.com")
                .recipientPhone("+43201234567")
                .recipientLocationCountryId(3L)
                .recipientZip("5678")
                .recipientLocationCityId(4L)
                .recipientAddress("Test Avenue 2")
                .recipientLatitude(1.234)
                .recipientLongitude(1.234)
                .packageSizeId(1L)
                .build();

        Shipment entity = mapper.toEntity(dto);
        assertEquals(1L, entity.getSenderLocationCountryId());
        assertEquals(2L, entity.getSenderLocationCityId());
        assertEquals(3L, entity.getRecipientLocationCountryId());
        assertEquals(4L, entity.getRecipientLocationCityId());

        ShipmentDTO back = mapper.toDTO(entity);
        assertEquals(1L, back.getSenderLocationCountryId());
        assertEquals(2L, back.getSenderLocationCityId());
        assertEquals(3L, back.getRecipientLocationCountryId());
        assertEquals(4L, back.getRecipientLocationCityId());
    }
}
