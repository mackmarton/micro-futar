package org.bme.micro_futar.orders.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.entities.PackageSize;
import org.bme.micro_futar.orders.mappers.PackageSizeMapper;
import org.bme.micro_futar.orders.repositories.PackageSizeRepository;
import org.bme.micro_futar.shared.dtos.PackageSizeDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PackageSizeService {

    private final  PackageSizeRepository packageSizeRepository;
    private final  PackageSizeMapper packageSizeMapper;

    public List<PackageSizeDTO> getAllPackageSizes() {
        return packageSizeRepository.findAll().stream()
                .map(packageSizeMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<PackageSizeDTO> getPackageSizeById(Long id) {
        return packageSizeRepository.findById(id)
                .map(packageSizeMapper::toDTO);
    }

    @Transactional
    public PackageSizeDTO createPackageSize(PackageSizeDTO packageSizeDTO) {
        PackageSize packageSize = packageSizeMapper.toEntity(packageSizeDTO);
        PackageSize savedPackageSize = packageSizeRepository.save(packageSize);
        return packageSizeMapper.toDTO(savedPackageSize);
    }

    @Transactional
    public Optional<PackageSizeDTO> updatePackageSize(Long id, PackageSizeDTO packageSizeDTO) {
        if (packageSizeDTO.getId() != null && !packageSizeDTO.getId().equals(id)) {
            throw new IllegalArgumentException("Path ID does not match DTO ID");
        }

        return packageSizeRepository.findById(id)
                .map(existingPackageSize -> {
                    PackageSize updatedPackageSize = packageSizeMapper.toEntity(packageSizeDTO);
                    PackageSize savedPackageSize = packageSizeRepository.save(updatedPackageSize);
                    return packageSizeMapper.toDTO(savedPackageSize);
                });
    }

    @Transactional
    public boolean deletePackageSize(Long id) {
        if (packageSizeRepository.existsById(id)) {
            packageSizeRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
