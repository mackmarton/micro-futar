package org.bme.micro_futar.logistics.repositories;

import org.bme.micro_futar.logistics.entities.PackageSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PackageSizeRepository extends JpaRepository<PackageSize, Long> {

    // Native query: bypasses @SQLRestriction so consumers also learn about deleted rows
    @Query(value = "SELECT * FROM package_size", nativeQuery = true)
    List<PackageSize> findAllIncludingDeleted();
}
