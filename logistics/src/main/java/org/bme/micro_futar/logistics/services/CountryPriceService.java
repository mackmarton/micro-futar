package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.entities.CountryPrice;
import org.bme.micro_futar.logistics.kafka.KafkaProducerService;
import org.bme.micro_futar.logistics.mappers.CountryPriceMapper;
import org.bme.micro_futar.logistics.repositories.CountryPriceRepository;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CountryPriceService {

    private final CountryPriceRepository countryPriceRepository;
    private final CountryPriceMapper countryPriceMapper;
    private final KafkaProducerService kafkaProducerService;

    public List<CountryPriceDTO> getAllCountryPrices() {
        return countryPriceRepository.findAll().stream()
                .map(countryPriceMapper::toDTO)
                .toList();
    }

    public Optional<CountryPriceDTO> getCountryPriceById(Long id) {
        return countryPriceRepository.findById(id)
                .map(countryPriceMapper::toDTO);
    }

    @Transactional
    public CountryPriceDTO createCountryPrice(CountryPriceDTO countryPriceDTO) {
        CountryPrice countryPrice = countryPriceMapper.toEntity(countryPriceDTO);
        CountryPrice savedCountryPrice = countryPriceRepository.save(countryPrice);
        CountryPriceDTO result = countryPriceMapper.toDTO(savedCountryPrice);
        kafkaProducerService.sendCountryPrice(result);
        return result;
    }

    @Transactional
    public Optional<CountryPriceDTO> updateCountryPrice(Long id, CountryPriceDTO countryPriceDTO) {
        if (countryPriceDTO.getId() != null && !countryPriceDTO.getId().equals(id)) {
            throw new IllegalArgumentException("Path ID does not match DTO ID");
        }

        return countryPriceRepository.findById(id)
                .map(existingCountryPrice -> {
                    CountryPrice updatedCountryPrice = countryPriceMapper.toEntity(countryPriceDTO);
                    CountryPrice savedCountryPrice = countryPriceRepository.save(updatedCountryPrice);
                    CountryPriceDTO result = countryPriceMapper.toDTO(savedCountryPrice);
                    kafkaProducerService.sendCountryPrice(result);
                    return result;
                });
    }

    @Transactional
    public boolean deleteCountryPrice(Long id) {
        if (countryPriceRepository.existsById(id)) {
            countryPriceRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
