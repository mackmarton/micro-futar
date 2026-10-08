package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.entities.Depo;
import org.bme.micro_futar.logistics.events.DepoChangedEvent;
import org.bme.micro_futar.logistics.kafka.KafkaProducerService;
import org.bme.micro_futar.logistics.mappers.DepoMapper;
import org.bme.micro_futar.logistics.repositories.DepoRepository;
import org.bme.micro_futar.shared.dtos.DepoDTO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepoService {

    private final DepoRepository depoRepository;
    private final DepoMapper depoMapper;
    private final KafkaProducerService kafkaProducerService;
    private final ApplicationEventPublisher eventPublisher;

    public List<DepoDTO> getAllDepos() {
        return depoRepository.findAll().stream()
                .map(depoMapper::toDTO)
                .toList();
    }

    public Optional<DepoDTO> getDepoById(Long id) {
        return depoRepository.findById(id)
                .map(depoMapper::toDTO);
    }

    @Transactional
    public DepoDTO createDepo(DepoDTO depoDTO) {
        Depo depo = depoMapper.toEntity(depoDTO);
        depo.setDeleted(false);
        Depo savedDepo = depoRepository.save(depo);
        DepoDTO result = depoMapper.toDTO(savedDepo);
        kafkaProducerService.sendDepo(result);
        eventPublisher.publishEvent(new DepoChangedEvent(this, DepoChangedEvent.ChangeType.CREATED));
        return result;
    }

    @Transactional
    public Optional<DepoDTO> updateDepo(Long id, DepoDTO depoDTO) {
        if (depoDTO.getId() != null && !depoDTO.getId().equals(id)) {
            throw new IllegalArgumentException("Path ID does not match DTO ID");
        }

        var updatedDepoDTO = depoRepository.findById(id)
                .map(_ -> {
                    Depo updatedDepo = depoMapper.toEntity(depoDTO);
                    updatedDepo.setId(id);
                    updatedDepo.setDeleted(false);
                    Depo savedDepo = depoRepository.save(updatedDepo);
                    DepoDTO result = depoMapper.toDTO(savedDepo);
                    kafkaProducerService.sendDepo(result);
                    return result;
                });
        eventPublisher.publishEvent(new DepoChangedEvent(this, DepoChangedEvent.ChangeType.UPDATED));
        return updatedDepoDTO;
    }

    @Transactional
    public boolean deleteDepo(Long id) {
        return depoRepository.findById(id)
                .map(depo -> {
                    DepoDTO deletedDepo = depoMapper.toDTO(depo);
                    // Soft delete via @SQLDelete
                    depoRepository.delete(depo);
                    deletedDepo.setDeleted(true);
                    kafkaProducerService.sendDepo(deletedDepo);
                    eventPublisher.publishEvent(new DepoChangedEvent(this, DepoChangedEvent.ChangeType.DELETED));
                    return true;
                })
                .orElse(false);
    }

    public List<DepoDTO> getAllDeposByCountryId(Long countryId) {
        return depoMapper.toDTOList(depoRepository.findAllByLocationCountryId(countryId));
    }
}
