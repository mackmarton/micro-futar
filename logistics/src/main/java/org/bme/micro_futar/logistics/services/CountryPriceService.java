package org.bme.micro_futar.logistics.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.logistics.entities.CountryPrice;
import org.bme.micro_futar.logistics.events.DepoChangedEvent;
import org.bme.micro_futar.logistics.events.DepoTransitChangedEvent;
import org.bme.micro_futar.logistics.kafka.KafkaProducerService;
import org.bme.micro_futar.logistics.mappers.CountryPriceMapper;
import org.bme.micro_futar.logistics.repositories.CountryPriceRepository;
import org.bme.micro_futar.shared.dtos.CountryPriceDTO;
import org.bme.micro_futar.shared.dtos.DepoDTO;
import org.bme.micro_futar.shared.dtos.PackageSizeDTO;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CountryPriceService {

    private final CountryPriceRepository countryPriceRepository;
    private final CountryPriceMapper countryPriceMapper;
    private final KafkaProducerService kafkaProducerService;
    private final DepoService depoService;
    private final DepoRouteFinderService depoRouteFinderService;
    private final PackageSizeService packageSizeService;

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

                    Double minPrice = null;
                    Double maxPrice = null;

                    for (DepoDTO originDepo : originDepos) {
                        for (DepoDTO destinationDepo : destinationDepos) {
                            if (originDepo.equals(destinationDepo)) continue;

                            Double price = depoRouteFinderService.findCheapestRoutePrice(
                                    originDepo.getId(),
                                    destinationDepo.getId(),
                                    packageSize.getId()
                            );

                            if (price == null) continue;

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
                        optionalCountryPrice.ifPresent(countryPriceRepository::delete);
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
    }

    @EventListener
    public void handleDepoChanged(DepoChangedEvent event) {
        recalculate();
    }

    @EventListener
    public void handleDepoTransitChanged(DepoTransitChangedEvent event) {
        recalculate();
    }
}

