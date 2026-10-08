package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.entities.Currency;
import org.bme.micro_futar.logistics.kafka.KafkaProducerService;
import org.bme.micro_futar.logistics.mappers.CurrencyMapper;
import org.bme.micro_futar.logistics.repositories.CurrencyRepository;
import org.bme.micro_futar.shared.dtos.CurrencyDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final CurrencyMapper currencyMapper;
    private final KafkaProducerService kafkaProducerService;

    public List<CurrencyDTO> getAllCurrencies() {
        return currencyRepository.findAll().stream()
                .map(currencyMapper::toDTO)
                .toList();
    }

    public Optional<CurrencyDTO> getCurrencyById(Long id) {
        return currencyRepository.findById(id)
                .map(currencyMapper::toDTO);
    }

    @Transactional
    public CurrencyDTO createCurrency(CurrencyDTO currencyDTO) {
        if (currencyRepository.existsByCode(currencyDTO.getCode())) {
            throw new IllegalArgumentException("Currency code already exists: " + currencyDTO.getCode());
        }

        Currency currency = currencyMapper.toEntity(currencyDTO);
        currency.setDeleted(false);
        // code is unique: reuse a soft-deleted currency with the same code instead of inserting a duplicate
        currencyRepository.findIdByCodeIncludingDeleted(currencyDTO.getCode()).ifPresent(deletedId -> {
            currencyRepository.restoreById(deletedId);
            currency.setId(deletedId);
        });
        Currency savedCurrency = currencyRepository.save(currency);
        CurrencyDTO result = currencyMapper.toDTO(savedCurrency);
        kafkaProducerService.sendCurrency(result);
        return result;
    }

    @Transactional
    public Optional<CurrencyDTO> updateCurrency(Long id, CurrencyDTO currencyDTO) {
        if (currencyDTO.getId() != null && !currencyDTO.getId().equals(id)) {
            throw new IllegalArgumentException("Path ID does not match DTO ID");
        }

        return currencyRepository.findById(id)
                .map(existingCurrency -> {
                    currencyRepository.findIdByCodeIncludingDeleted(currencyDTO.getCode())
                            .filter(otherId -> !otherId.equals(id))
                            .ifPresent(other -> {
                                throw new IllegalArgumentException("Currency code already exists: " + currencyDTO.getCode());
                            });

                    Currency updatedCurrency = currencyMapper.toEntity(currencyDTO);
                    updatedCurrency.setId(id);
                    updatedCurrency.setDeleted(false);
                    Currency savedCurrency = currencyRepository.save(updatedCurrency);
                    CurrencyDTO result = currencyMapper.toDTO(savedCurrency);
                    kafkaProducerService.sendCurrency(result);
                    return result;
                });
    }

    @Transactional
    public boolean deleteCurrency(Long id) {
        return currencyRepository.findById(id)
                .map(currency -> {
                    CurrencyDTO deletedCurrency = currencyMapper.toDTO(currency);
                    // Soft delete via @SQLDelete
                    currencyRepository.delete(currency);
                    deletedCurrency.setDeleted(true);
                    kafkaProducerService.sendCurrency(deletedCurrency);
                    return true;
                })
                .orElse(false);
    }
}
