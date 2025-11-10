package org.bme.micro_futar.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.entities.PackageSize;
import org.bme.micro_futar.mappers.PackageSizeMapper;
import org.bme.micro_futar.repositories.PackageSizeRepository;
import org.bme.micro_futar.shared.dtos.PackageSizeDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PackageSizeService {

    private final PackageSizeRepository packageSizeRepository;
    private final PackageSizeMapper packageSizeMapper;

    @Transactional
    public PackageSizeDTO save(PackageSizeDTO packageSizeDTO) {
        log.info("Saving packageSize: {}", packageSizeDTO);
        PackageSize packageSize = packageSizeMapper.toEntity(packageSizeDTO);
        PackageSize savedPackageSize = packageSizeRepository.save(packageSize);
        return packageSizeMapper.toDTO(savedPackageSize);
    }

    @Transactional(readOnly = true)
    public Optional<PackageSizeDTO> findById(Long id) {
        log.debug("Finding packageSize by id: {}", id);
        return packageSizeRepository.findById(id)
                .map(packageSizeMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<PackageSizeDTO> findAll() {
        log.debug("Finding all packageSizes");
        return packageSizeRepository.findAll().stream()
                .map(packageSizeMapper::toDTO)
                .toList();
    }
}

