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

@Service
@RequiredArgsConstructor
public class PackageSizeService {

    private final PackageSizeRepository packageSizeRepository;
    private final PackageSizeMapper packageSizeMapper;

    public List<PackageSizeDTO> getAllPackageSizes() {
        return packageSizeRepository.findAll().stream()
                .map(packageSizeMapper::toDTO)
                .toList();
    }

    public Optional<PackageSizeDTO> getPackageSizeById(Long id) {
        return packageSizeRepository.findById(id)
                .map(packageSizeMapper::toDTO);
    }

    @Transactional
    public PackageSize savePackageSize(PackageSizeDTO packageSizeDTO) {
        PackageSize packageSize = packageSizeMapper.toEntity(packageSizeDTO);
        return packageSizeRepository.save(packageSize);
    }
}
