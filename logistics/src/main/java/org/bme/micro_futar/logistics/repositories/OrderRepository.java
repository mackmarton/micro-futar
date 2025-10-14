package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity,Long> {
}
