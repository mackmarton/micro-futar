package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.PackageSize;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageSizeRepository extends JpaRepository<PackageSize, Long> {
}
