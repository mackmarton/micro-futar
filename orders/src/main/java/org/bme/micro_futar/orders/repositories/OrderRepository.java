package org.bme.micro_futar.orders.repositories;

import org.bme.micro_futar.orders.entities.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity,Long> {
}
