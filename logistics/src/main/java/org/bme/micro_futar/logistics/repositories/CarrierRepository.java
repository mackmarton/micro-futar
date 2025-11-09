package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.Carrier;
import org.bme.micro_futar.shared.enums.CarrierType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarrierRepository extends JpaRepository<Carrier, Long> {
    List<Carrier> findByDepoIdAndCarrierType(Long depoId, CarrierType carrierType);
}
