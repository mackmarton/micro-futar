package org.bme.micro_futar.payments.repositories;

import org.bme.micro_futar.payments.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByShipmentId(Long shipmentId);

    boolean existsByShipmentId(Long shipmentId);
}
