package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.dtos.CountryBaseFeeDTO;
import org.bme.micro_futar.logistics.entities.CountryBaseFee;
import org.bme.micro_futar.logistics.events.CountryBaseFeeChangedEvent;
import org.bme.micro_futar.logistics.exceptions.DuplicateEntityException;
import org.bme.micro_futar.logistics.mappers.CountryBaseFeeMapper;
import org.bme.micro_futar.logistics.repositories.CountryBaseFeeRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CountryBaseFeeService {

    private final CountryBaseFeeMapper countryBaseFeeMapper;
    private final CountryBaseFeeRepository countryBaseFeeRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ReferenceValidator referenceValidator;

    public List<CountryBaseFeeDTO> getAllCountryBaseFees() {
        return countryBaseFeeRepository.findAll().stream()
                .map(countryBaseFeeMapper::toDTO)
                .toList();
    }

    public Optional<CountryBaseFeeDTO> getCountryBaseFeeById(Long id) {
        return countryBaseFeeRepository.findById(id)
                .map(countryBaseFeeMapper::toDTO);
    }

    @Transactional
    public CountryBaseFeeDTO createCountryBaseFee(CountryBaseFeeDTO dto) {
        referenceValidator.validate(dto);
        requireUnique(dto, null);
        CountryBaseFee entity = countryBaseFeeMapper.toEntity(dto);
        entity.setId(null);
        entity.setDeleted(false);
        CountryBaseFee saved = countryBaseFeeRepository.save(entity);
        eventPublisher.publishEvent(new CountryBaseFeeChangedEvent(this, CountryBaseFeeChangedEvent.ChangeType.CREATED));
        return countryBaseFeeMapper.toDTO(saved);
    }

    @Transactional
    public Optional<CountryBaseFeeDTO> updateCountryBaseFee(Long id, CountryBaseFeeDTO dto) {
        if (dto.getId() != null && !dto.getId().equals(id)) {
            throw new IllegalArgumentException("Path ID does not match DTO ID");
        }
        if (!countryBaseFeeRepository.existsById(id)) {
            return Optional.empty();
        }
        referenceValidator.validate(dto);
        requireUnique(dto, id);
        CountryBaseFee entity = countryBaseFeeMapper.toEntity(dto);
        entity.setId(id);
        entity.setDeleted(false);
        CountryBaseFee saved = countryBaseFeeRepository.save(entity);
        eventPublisher.publishEvent(new CountryBaseFeeChangedEvent(this, CountryBaseFeeChangedEvent.ChangeType.UPDATED));
        return Optional.of(countryBaseFeeMapper.toDTO(saved));
    }

    @Transactional
    public boolean deleteCountryBaseFee(Long id) {
        if (!countryBaseFeeRepository.existsById(id)) {
            return false;
        }
        countryBaseFeeRepository.deleteById(id);
        eventPublisher.publishEvent(new CountryBaseFeeChangedEvent(this, CountryBaseFeeChangedEvent.ChangeType.DELETED));
        return true;
    }

    /** The unique key only covers non-deleted rows (@SQLRestriction), so soft-deleted fees never conflict. */
    private void requireUnique(CountryBaseFeeDTO dto, Long ownId) {
        countryBaseFeeRepository.findByCountryIdAndPackageSizeId(dto.getCountryId(), dto.getPackageSizeId())
                .filter(existing -> !existing.getId().equals(ownId))
                .ifPresent(existing -> {
                    throw new DuplicateEntityException("A base fee already exists for country " + dto.getCountryId()
                            + " and package size " + dto.getPackageSizeId() + " (id " + existing.getId() + ")");
                });
    }
}
