package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.entities.Courier;
import org.bme.micro_futar.logistics.kafka.KafkaProducerService;
import org.bme.micro_futar.logistics.mappers.CourierMapper;
import org.bme.micro_futar.logistics.repositories.CourierRepository;
import org.bme.micro_futar.shared.dtos.CourierDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourierService {

    private final CourierRepository courierRepository;
    private final CourierMapper courierMapper;
    private final KafkaProducerService kafkaProducerService;

    public List<CourierDTO> getAllCouriers() {
        return courierRepository.findAll().stream()
                .map(courierMapper::toDTO)
                .toList();
    }

    public Optional<CourierDTO> getCourierById(Long id) {
        return courierRepository.findById(id)
                .map(courierMapper::toDTO);
    }

    @Transactional
    public CourierDTO createCourier(CourierDTO CourierDTO) {
        Courier courier = courierMapper.toEntity(CourierDTO);
        Courier savedCourier = courierRepository.save(courier);
        CourierDTO result = courierMapper.toDTO(savedCourier);
        kafkaProducerService.sendCourier(result);
        return result;
    }

    @Transactional
    public Optional<CourierDTO> updateCourier(Long id, CourierDTO CourierDTO) {
        if (CourierDTO.getId() != null && !CourierDTO.getId().equals(id)) {
            throw new IllegalArgumentException("Path ID does not match DTO ID");
        }

        return courierRepository.findById(id)
                .map(_ -> {
                    Courier updatedCourier = courierMapper.toEntity(CourierDTO);
                    updatedCourier.setId(id);
                    Courier savedCourier = courierRepository.save(updatedCourier);
                    CourierDTO result = courierMapper.toDTO(savedCourier);
                    kafkaProducerService.sendCourier(result);
                    return result;
                });
    }

    @Transactional
    public boolean deleteCourier(Long id) {
        if (courierRepository.existsById(id)) {
            courierRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<CourierDTO> getCouriersByDepoIdAndType(Long depoId, org.bme.micro_futar.shared.enums.CourierType courierType) {
        return courierRepository.findByDepoIdAndCourierType(depoId, courierType).stream()
                .map(courierMapper::toDTO)
                .toList();
    }

    public List<CourierDTO> getCouriersByDepoId(Long depoId) {
        return courierRepository.findAllByDepoId(depoId).stream()
                .map(courierMapper::toDTO)
                .toList();
    }

    public List<CourierDTO> getCrossDepoCouriers() {
        return courierRepository.findAllByDepoIdIsNull().stream()
                .map(courierMapper::toDTO)
                .toList();
    }
}
