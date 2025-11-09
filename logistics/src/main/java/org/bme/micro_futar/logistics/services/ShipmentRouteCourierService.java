package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.entities.ShipmentRouteCarrier;
import org.bme.micro_futar.logistics.kafka.KafkaProducerService;
import org.bme.micro_futar.logistics.mappers.ShipmentRouteCarrierMapper;
import org.bme.micro_futar.logistics.repositories.ShipmentRouteCarrierRepository;
import org.bme.micro_futar.shared.dtos.ShipmentRouteCarrierDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipmentRouteCourierService {

    private final ShipmentRouteCarrierRepository shipmentRouteCarrierRepository;
    private final ShipmentRouteCarrierMapper shipmentRouteCarrierMapper;
    private final KafkaProducerService kafkaProducerService;

    public long countByCourierIdAndDate(Long carrierId, Date date) {
        return shipmentRouteCarrierRepository.countByCarrierIdAndDate(carrierId, date);
    }

    public List<ShipmentRouteCarrierDTO> findByCourierIdAndDateAssignedFor(Long carrierId, Date date) {
        return shipmentRouteCarrierRepository.findByCarrierIdAndDateAssignedFor(carrierId, date).stream()
                .map(shipmentRouteCarrierMapper::toDTO)
                .toList();
    }

    @Transactional
    public ShipmentRouteCarrierDTO save(ShipmentRouteCarrierDTO dto) {
        ShipmentRouteCarrier entity = shipmentRouteCarrierMapper.toEntity(dto);
        ShipmentRouteCarrier saved = shipmentRouteCarrierRepository.save(entity);
        ShipmentRouteCarrierDTO result = shipmentRouteCarrierMapper.toDTO(saved);
        kafkaProducerService.sendShipmentRouteCarrier(result);
        return result;
    }
}

