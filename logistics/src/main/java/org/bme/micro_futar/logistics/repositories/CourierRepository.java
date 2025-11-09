package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.Courier;
import org.bme.micro_futar.shared.enums.CourierType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourierRepository extends JpaRepository<Courier, Long> {
    List<Courier> findByDepoIdAndCourierType(Long depoId, CourierType courierType);
}
