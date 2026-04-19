package org.bme.micro_futar.courier.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.entities.Shipment;
import org.bme.micro_futar.courier.kafka.KafkaProducerService;
import org.bme.micro_futar.courier.mappers.ShipmentMapper;
import org.bme.micro_futar.courier.repositories.ShipmentRepository;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentMapper shipmentMapper;
    private final ShipmentRepository shipmentRepository;
    private final ApplicationContext applicationContext;
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    public ShipmentDTO saveWithoutTopicSend(ShipmentDTO shipmentDTO) {
        log.info("Saving shipment: {}", shipmentDTO);
        Shipment shipment = shipmentMapper.toEntity(shipmentDTO);
        Shipment savedShipment = shipmentRepository.save(shipment);
        return shipmentMapper.toDTO(savedShipment);
    }

    @Transactional
    public ShipmentDTO save(ShipmentDTO shipmentDTO) {
        ShipmentService self = applicationContext.getBean(ShipmentService.class);
        ShipmentDTO savedShipmentDTO = self.saveWithoutTopicSend(shipmentDTO);
        kafkaProducerService.sendShipment(savedShipmentDTO);
        return savedShipmentDTO;
    }

    @Transactional(readOnly = true)
    public Optional<ShipmentDTO> findById(Long id) {
        log.debug("Finding shipment by id: {}", id);
        return shipmentRepository.findById(id)
                .map(shipmentMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ShipmentDTO> findAll() {
        log.debug("Finding all shipments");
        return shipmentRepository.findAll().stream()
                .map(shipmentMapper::toDTO)
                .toList();
    }
}

