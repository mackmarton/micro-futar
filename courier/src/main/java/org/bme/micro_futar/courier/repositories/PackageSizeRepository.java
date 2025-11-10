package org.bme.micro_futar.courier.repositories;

import org.bme.micro_futar.courier.entities.PackageSize;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageSizeRepository extends JpaRepository<PackageSize, Long> {
}

