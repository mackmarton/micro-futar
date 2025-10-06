package org.bme.micro_futar.orders.repositories;

import org.bme.micro_futar.orders.entities.PackageSize;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageSizeRepository extends JpaRepository<PackageSize, Long> {
}
