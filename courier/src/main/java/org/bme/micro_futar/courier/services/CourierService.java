package org.bme.micro_futar.courier.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.courier.entities.Courier;
import org.bme.micro_futar.courier.mappers.CourierMapper;
import org.bme.micro_futar.courier.repositories.CourierRepository;
import org.bme.micro_futar.shared.dtos.CourierDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourierService {

    private final CourierRepository courierRepository;
    private final CourierMapper courierMapper;

    @Transactional
    public CourierDTO save(CourierDTO courierDTO) {
        log.info("Saving courier: {}", courierDTO);
        Courier courier = courierMapper.toEntity(courierDTO);
        Courier savedCourier = courierRepository.save(courier);
        return courierMapper.toDTO(savedCourier);
    }

    @Transactional(readOnly = true)
    public Optional<CourierDTO> findById(Long id) {
        return courierRepository.findById(id)
                .map(courierMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<CourierDTO> findAll() {
        return courierRepository.findAll().stream()
                .map(courierMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Long findIdByEmail(String email) {
        return courierRepository.findByEmail(email).orElseThrow().getId();
    }
}

