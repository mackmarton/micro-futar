package org.bme.micro_futar.orders.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.entities.Currency;
import org.bme.micro_futar.orders.mappers.CurrencyMapper;
import org.bme.micro_futar.orders.repositories.CurrencyRepository;
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
    public CurrencyDTO saveCurrency(CurrencyDTO currencyDTO) {
        Currency currency;

        if (currencyDTO.getId() != null) {
            Optional<Currency> existing = currencyRepository.findById(currencyDTO.getId());
            if (existing.isPresent()) {
                currency = existing.get();
                currency.setCode(currencyDTO.getCode());
                currency.setName(currencyDTO.getName());
                currency.setSymbol(currencyDTO.getSymbol());
            } else {
                currency = currencyMapper.toEntity(currencyDTO);
            }
        } else {
            currency = currencyMapper.toEntity(currencyDTO);
        }

        return currencyMapper.toDTO(currencyRepository.save(currency));
    }
}
