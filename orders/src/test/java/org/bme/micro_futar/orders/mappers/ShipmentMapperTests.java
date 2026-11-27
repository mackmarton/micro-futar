package org.bme.micro_futar.orders.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import org.bme.micro_futar.orders.entities.Shipment;
import org.bme.micro_futar.orders.support.TestFixtures;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ShipmentMapperTests {

    private final ShipmentMapper mapper = Mappers.getMapper(ShipmentMapper.class);

    @Test
    void locationIdsKeepLongValuesInBothDirections() {
        ShipmentDTO dto = TestFixtures.shipmentRequest()
                .senderLocationCountryId(1L)
                .senderLocationCityId(2L)
                .recipientLocationCountryId(3L)
                .recipientLocationCityId(4L)
                .build();

        Shipment entity = mapper.toEntity(dto);
        assertThat(entity.getSenderLocationCountryId()).isEqualTo(1L);
        assertThat(entity.getSenderLocationCityId()).isEqualTo(2L);
        assertThat(entity.getRecipientLocationCountryId()).isEqualTo(3L);
        assertThat(entity.getRecipientLocationCityId()).isEqualTo(4L);

        ShipmentDTO back = mapper.toDTO(entity);
        assertThat(back.getSenderLocationCountryId()).isEqualTo(1L);
        assertThat(back.getSenderLocationCityId()).isEqualTo(2L);
        assertThat(back.getRecipientLocationCountryId()).isEqualTo(3L);
        assertThat(back.getRecipientLocationCityId()).isEqualTo(4L);
    }
}
