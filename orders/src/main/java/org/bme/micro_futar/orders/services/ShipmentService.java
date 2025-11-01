package org.bme.micro_futar.orders.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.exceptions.NoServiceException;
import org.bme.micro_futar.orders.kafka.ShipmentProducer;
import org.bme.micro_futar.orders.mappers.ShipmentMapper;
import org.bme.micro_futar.orders.repositories.ShipmentRepository;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final CountryPriceService countryPriceService;
    private final ShipmentMapper shipmentMapper;
    private final ShipmentProducer shipmentProducer;

    @Transactional
    public ShipmentDTO newShipment(ShipmentDTO shipmentDTO) {
        shipmentDTO.setPrice(calculatePrice(shipmentDTO));
        shipmentDTO.setConfirmed(false);
        var shipmentEntity = shipmentMapper.toEntity(shipmentDTO);
        var savedEntity = shipmentRepository.save(shipmentEntity);
        ShipmentDTO savedShipmentDTO = shipmentMapper.toDTO(savedEntity);
        shipmentProducer.sendShipmentToTopic(savedShipmentDTO);
        return savedShipmentDTO;
    }

    @Transactional
    public ShipmentDTO confirm(long id) {
        var shipmentEntity = shipmentRepository.findById(id).orElseThrow();
        shipmentEntity.setConfirmed(true);
        shipmentEntity.setParcelNumber(UUID.randomUUID().toString());
        var savedEntity = shipmentRepository.save(shipmentEntity);
        ShipmentDTO confirmedShipmentDTO = shipmentMapper.toDTO(savedEntity);
        shipmentProducer.sendShipmentToTopic(confirmedShipmentDTO);
        return confirmedShipmentDTO;
    }

    private double calculatePrice(ShipmentDTO shipmentDTO) {
        var countryPrice = countryPriceService.findPriceByCountriesAndSize(shipmentDTO.getSenderLocationCountryId(), shipmentDTO.getRecipientLocationCountryId(), shipmentDTO.getPackageSizeId());
        if (countryPrice.isEmpty()) {
            throw new NoServiceException("There is no service between the origin and destination countries for this size of package!");
        }
        return countryPrice.get().getPrice();
    }

}
