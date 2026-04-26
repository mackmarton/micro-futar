package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.dtos.DepoTransitDTO;
import org.bme.micro_futar.logistics.entities.DepoTransit;
import org.bme.micro_futar.logistics.events.DepoTransitChangedEvent;
import org.bme.micro_futar.logistics.mappers.DepoTransitMapper;
import org.bme.micro_futar.logistics.repositories.DepoTransitRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepoTransitService {

    private final DepoTransitMapper depoTransitMapper;
    private final DepoTransitRepository depoTransitRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<DepoTransitDTO> getAllDepoTransits() {
        return depoTransitRepository.findAll().stream()
                .map(depoTransitMapper::toDTO)
                .toList();
    }

    public List<DepoTransitDTO> getDepoTransitsByOriginDepoId(Long originDepoId) {
        return depoTransitRepository.findAllByOriginDepoId(originDepoId).stream()
                .map(depoTransitMapper::toDTO)
                .toList();
    }

    public List<DepoTransitDTO> getDepoTransitsByDestinationDepoId(Long destinationDepoId) {
        return depoTransitRepository.findAllByDestinationDepoId(destinationDepoId).stream()
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
        eventPublisher.publishEvent(new DepoTransitChangedEvent(this, DepoTransitChangedEvent.ChangeType.CREATED));
        return depoTransitMapper.toDTO(savedDepoTransit);
    }

    @Transactional
    public Optional<DepoTransitDTO> updateDepoTransit(Long id, DepoTransitDTO depoTransitDTO) {
        if (depoTransitDTO.getId() != null && !depoTransitDTO.getId().equals(id)) {
            throw new IllegalArgumentException("Path ID does not match DTO ID");
        }

        var updatedDepoTransitDTO = depoTransitRepository.findById(id)
                .map(_ -> {
                    DepoTransit updatedDepoTransit = depoTransitMapper.toEntity(depoTransitDTO);
                    updatedDepoTransit.setId(id);
                    DepoTransit savedDepoTransit = depoTransitRepository.save(updatedDepoTransit);
                    return depoTransitMapper.toDTO(savedDepoTransit);
                });
        eventPublisher.publishEvent(new DepoTransitChangedEvent(this, DepoTransitChangedEvent.ChangeType.UPDATED));
        return updatedDepoTransitDTO;
    }

    @Transactional
    public boolean deleteDepoTransit(Long id) {
        if (depoTransitRepository.existsById(id)) {
            depoTransitRepository.deleteById(id);
            eventPublisher.publishEvent(new DepoTransitChangedEvent(this, DepoTransitChangedEvent.ChangeType.DELETED));
            return true;
        }
        return false;
    }
}

