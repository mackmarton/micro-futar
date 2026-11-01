package org.bme.micro_futar.payments.repositories;

import org.bme.micro_futar.payments.entities.ProcessedStripeEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedStripeEventRepository extends JpaRepository<ProcessedStripeEvent, String> {
}
