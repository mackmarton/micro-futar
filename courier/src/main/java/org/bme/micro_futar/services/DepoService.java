package org.bme.micro_futar.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.entities.Depo;
import org.bme.micro_futar.mappers.DepoMapper;
import org.bme.micro_futar.repositories.DepoRepository;
import org.bme.micro_futar.shared.dtos.DepoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepoService {

    private final DepoRepository depoRepository;
    private final DepoMapper depoMapper;

    @Transactional
    public DepoDTO save(DepoDTO depoDTO) {
        log.info("Saving depo: {}", depoDTO);
        Depo depo = depoMapper.toEntity(depoDTO);
        Depo savedDepo = depoRepository.save(depo);
        return depoMapper.toDTO(savedDepo);
    }

    @Transactional(readOnly = true)
    public Optional<DepoDTO> findById(Long id) {
        log.debug("Finding depo by id: {}", id);
        return depoRepository.findById(id)
                .map(depoMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<DepoDTO> findAll() {
        log.debug("Finding all depos");
        return depoRepository.findAll().stream()
                .map(depoMapper::toDTO)
                .toList();
    }
}

