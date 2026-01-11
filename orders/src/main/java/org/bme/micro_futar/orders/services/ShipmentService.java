package org.bme.micro_futar.orders.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.kafka.ShipmentProducer;
import org.bme.micro_futar.orders.mappers.ShipmentMapper;
import org.bme.micro_futar.orders.repositories.ShipmentRepository;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final CountryPriceService countryPriceService;
    private final ShipmentMapper shipmentMapper;
    private final ShipmentProducer shipmentProducer;

    @Transactional
    public ShipmentDTO newShipment(ShipmentDTO shipmentDTO) {
        shipmentDTO.setConfirmed(true);
        shipmentDTO.setParcelNumber(UUID.randomUUID().toString());
        var shipmentEntity = shipmentMapper.toEntity(shipmentDTO);
        var savedEntity = shipmentRepository.save(shipmentEntity);
        ShipmentDTO savedShipmentDTO = shipmentMapper.toDTO(savedEntity);
        shipmentProducer.sendShipmentToTopic(savedShipmentDTO);
        return savedShipmentDTO;
    }
}
