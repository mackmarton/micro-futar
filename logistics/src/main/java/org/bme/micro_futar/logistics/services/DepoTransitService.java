package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.dtos.DepoTransitDTO;
import org.bme.micro_futar.logistics.entities.DepoTransit;
import org.bme.micro_futar.logistics.mappers.DepoTransitMapper;
import org.bme.micro_futar.logistics.repositories.DepoTransitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepoTransitService {

    private final DepoTransitMapper depoTransitMapper;
    private final DepoTransitRepository depoTransitRepository;

    public List<DepoTransitDTO> getAllDepoTransits() {
        return depoTransitRepository.findAll().stream()
                .map(depoTransitMapper::toDTO)
                .toList();
    }

    public Optional<DepoTransitDTO> getDepoTransitById(Long id) {
        return depoTransitRepository.findById(id)
                .map(depoTransitMapper::toDTO);
    }

    @Transactional
    public DepoTransitDTO createDepoTransit(DepoTransitDTO depoTransitDTO) {
        DepoTransit depoTransit = depoTransitMapper.toEntity(depoTransitDTO);
        DepoTransit savedDepoTransit = depoTransitRepository.save(depoTransit);
        return depoTransitMapper.toDTO(savedDepoTransit);
    }

    @Transactional
    public Optional<DepoTransitDTO> updateDepoTransit(Long id, DepoTransitDTO depoTransitDTO) {
        if (depoTransitDTO.getId() != null && !depoTransitDTO.getId().equals(id)) {
            throw new IllegalArgumentException("Path ID does not match DTO ID");
        }

        return depoTransitRepository.findById(id)
                .map(_ -> {
                    DepoTransit updatedDepoTransit = depoTransitMapper.toEntity(depoTransitDTO);
                    updatedDepoTransit.setId(id);
                    DepoTransit savedDepoTransit = depoTransitRepository.save(updatedDepoTransit);
                    return depoTransitMapper.toDTO(savedDepoTransit);
                });
    }

    @Transactional
    public boolean deleteDepoTransit(Long id) {
        if (depoTransitRepository.existsById(id)) {
            depoTransitRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

