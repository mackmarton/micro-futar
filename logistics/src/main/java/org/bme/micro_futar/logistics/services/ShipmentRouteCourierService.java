package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.entities.ShipmentRouteCourier;
import org.bme.micro_futar.logistics.kafka.KafkaProducerService;
import org.bme.micro_futar.logistics.mappers.ShipmentRouteCourierMapper;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteCourierRepository;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCourierDTO;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipmentRouteCourierService {

    private final ApplicationContext applicationContext;
    private final KafkaProducerService kafkaProducerService;
    private final ShipmentRouteCourierMapper shipmentRouteCourierMapper;
    private final ShipmentRouteCourierRepository shipmentRouteCourierRepository;

    public long countByCourierIdAndDate(Long courierId, Date date) {
        return shipmentRouteCourierRepository.countByCourierIdAndDate(courierId, date);
    }

    public List<ShipmentRouteCourierDTO> findByCourierIdAndDateAssignedFor(Long courierId, Date date) {
        return shipmentRouteCourierRepository.findByCourierIdAndDateAssignedFor(courierId, date).stream()
                .map(shipmentRouteCourierMapper::toDTO)
                .toList();
    }

    @Transactional
    public ShipmentRouteCourierDTO saveWithoutTopicSend(ShipmentRouteCourierDTO dto) {
        ShipmentRouteCourier entity = shipmentRouteCourierMapper.toEntity(dto);
        ShipmentRouteCourier saved = shipmentRouteCourierRepository.save(entity);
        return shipmentRouteCourierMapper.toDTO(saved);
    }

    public ShipmentRouteCourierDTO save(ShipmentRouteCourierDTO dto) {
        var self = applicationContext.getBean(ShipmentRouteCourierService.class);
        var savedDTO = self.saveWithoutTopicSend(dto);
        kafkaProducerService.sendShipmentRouteCourier(savedDTO);
        return savedDTO;
    }
}

