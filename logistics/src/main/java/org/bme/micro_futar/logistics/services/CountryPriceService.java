package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.logistics.entities.CountryBaseFee;
import org.bme.micro_futar.logistics.entities.CountryPrice;
import org.bme.micro_futar.logistics.events.CountryBaseFeeChangedEvent;
import org.bme.micro_futar.logistics.events.DepoChangedEvent;
import org.bme.micro_futar.logistics.events.DepoTransitChangedEvent;
import org.bme.micro_futar.logistics.kafka.KafkaProducerService;
import org.bme.micro_futar.logistics.mappers.CountryPriceMapper;
import org.bme.micro_futar.logistics.repositories.CountryBaseFeeRepository;
import org.bme.micro_futar.logistics.repositories.CountryPriceRepository;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
import org.bme.micro_futar.shared.dtos.DepoDTO;
import org.bme.micro_futar.shared.dtos.PackageSizeDTO;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountryPriceService {

    private final CountryPriceRepository countryPriceRepository;
    private final CountryPriceMapper countryPriceMapper;
    private final KafkaProducerService kafkaProducerService;
    private final DepoService depoService;
    private final DepoRouteFinderService depoRouteFinderService;
    private final PackageSizeService packageSizeService;
    private final CountryBaseFeeRepository countryBaseFeeRepository;

    public List<CountryPriceDTO> getAllCountryPrices() {
        return countryPriceRepository.findAll().stream()
                .map(countryPriceMapper::toDTO)
                .toList();
    }

    public Optional<CountryPriceDTO> getCountryPriceById(Long id) {
        return countryPriceRepository.findById(id)
                .map(countryPriceMapper::toDTO);
    }

    public void recalculate() {
        Map<List<Long>, Double> baseFees = new HashMap<>();
        for (CountryBaseFee fee : countryBaseFeeRepository.findAll()) {
            baseFees.put(List.of(fee.getCountryId(), fee.getPackageSizeId()), fee.getPrice());
        }
        Set<List<Long>> warnedMissing = new HashSet<>();

        List<DepoDTO> allDepos = depoService.getAllDepos();
        List<PackageSizeDTO> allPackageSizes = packageSizeService.getAllPackageSizes();

        List<Long> allCountryIds = allDepos.stream()
                .map(DepoDTO::getLocationCountryId)
                .distinct()
                .toList();

        for (PackageSizeDTO packageSize : allPackageSizes) {
            for (Long originCountryId : allCountryIds) {
                for (Long destinationCountryId : allCountryIds) {
                    List<DepoDTO> originDepos = depoService.getAllDeposByCountryId(originCountryId);
                    List<DepoDTO> destinationDepos = depoService.getAllDeposByCountryId(destinationCountryId);

                    Double baseFee = baseFees.get(List.of(originCountryId, packageSize.getId()));
                    if (baseFee == null) {
                        if (warnedMissing.add(List.of(originCountryId, packageSize.getId()))) {
                            log.warn("No base fee for country {} and package size {}, counting it as 0",
                                    originCountryId, packageSize.getId());
                        }
                        baseFee = 0.0;
                    }

                    Double minPrice = null;
                    Double maxPrice = null;

                    for (DepoDTO originDepo : originDepos) {
                        for (DepoDTO destinationDepo : destinationDepos) {
                            Double routePrice = originDepo.equals(destinationDepo)
                                    ? Double.valueOf(0.0)
                                    : depoRouteFinderService.findCheapestRoutePrice(
                                            originDepo.getId(),
                                            destinationDepo.getId(),
                                            packageSize.getId());

                            if (routePrice == null) continue;
                            double price = baseFee + routePrice;

                            if (minPrice == null || price < minPrice) {
                                minPrice = price;
                            }
                            if (maxPrice == null || price > maxPrice) {
                                maxPrice = price;
                            }
                        }
                    }

                    Optional<CountryPrice> optionalCountryPrice = countryPriceRepository
                            .findByOriginCountryIdAndDestinationCountryIdAndPackageSizeId(
                                    originCountryId, destinationCountryId, packageSize.getId()
                            );

                    if (minPrice == null) {
                        optionalCountryPrice.ifPresent(this::deletePrice);
                        continue;
                    }

                    CountryPrice countryPrice;
                    if (optionalCountryPrice.isEmpty()) {
                        countryPrice = new CountryPrice();
                        countryPrice.setOriginCountryId(originCountryId);
                        countryPrice.setDestinationCountryId(destinationCountryId);
                        countryPrice.setPackageSizeId(packageSize.getId());
                    } else {
                        countryPrice = optionalCountryPrice.get();
                    }

                    countryPrice.setMinPrice(minPrice);
                    countryPrice.setMaxPrice(maxPrice);
                    CountryPrice savedCountryPrice = countryPriceRepository.save(countryPrice);
                    kafkaProducerService.sendCountryPrice(countryPriceMapper.toDTO(savedCountryPrice));
                }
            }
        }

        // The loops above only visit countries with a depo, the prices of the others are no longer valid.
        countryPriceRepository.findAll().stream()
                .filter(price -> !allCountryIds.contains(price.getOriginCountryId())
                        || !allCountryIds.contains(price.getDestinationCountryId()))
                .forEach(this::deletePrice);
    }

    private void deletePrice(CountryPrice countryPrice) {
        CountryPriceDTO deletedCountryPrice = countryPriceMapper.toDTO(countryPrice);
        // Soft delete via @SQLDelete
        countryPriceRepository.delete(countryPrice);
        deletedCountryPrice.setDeleted(true);
        kafkaProducerService.sendCountryPrice(deletedCountryPrice);
    }

    @EventListener
    public void handleDepoChanged(DepoChangedEvent event) {
        recalculate();
    }

    @EventListener
    public void handleCountryBaseFeeChanged(CountryBaseFeeChangedEvent event) {
        recalculate();
    }

    @EventListener
    public void handleDepoTransitChanged(DepoTransitChangedEvent event) {
        recalculate();
    }
}

