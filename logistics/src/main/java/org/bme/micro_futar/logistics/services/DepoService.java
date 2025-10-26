package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.dtos.DepoDTO;
import org.bme.micro_futar.logistics.entities.Depo;
import org.bme.micro_futar.logistics.kafka.KafkaProducerService;
import org.bme.micro_futar.logistics.mappers.DepoMapper;
import org.bme.micro_futar.logistics.repositories.DepoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepoService {

    private final DepoRepository depoRepository;
    private final DepoMapper depoMapper;
    private final KafkaProducerService kafkaProducerService;

    public List<DepoDTO> getAllDepos() {
        return depoRepository.findAll().stream()
                .map(depoMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<DepoDTO> getDepoById(Long id) {
        return depoRepository.findById(id)
                .map(depoMapper::toDTO);
    }

    @Transactional
    public DepoDTO createDepo(DepoDTO depoDTO) {
        Depo depo = depoMapper.toEntity(depoDTO);
        Depo savedDepo = depoRepository.save(depo);
        return depoMapper.toDTO(savedDepo);
    }

    @Transactional
    public Optional<DepoDTO> updateDepo(Long id, DepoDTO depoDTO) {
        if (depoDTO.getId() != null && !depoDTO.getId().equals(id)) {
            throw new IllegalArgumentException("Path ID does not match DTO ID");
        }

        return depoRepository.findById(id)
                .map(existingDepo -> {
                    Depo updatedDepo = depoMapper.toEntity(depoDTO);
                    updatedDepo.setId(id);
                    Depo savedDepo = depoRepository.save(updatedDepo);
                    return depoMapper.toDTO(savedDepo);
                });
    }

    @Transactional
    public boolean deleteDepo(Long id) {
        if (depoRepository.existsById(id)) {
            depoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

