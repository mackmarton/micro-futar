package org.bme.micro_futar.orders.services;

import lombok.RequiredArgsConstructor;
import org.bme.micro_futar.orders.mappers.PackageSizeMapper;
import org.bme.micro_futar.orders.repositories.PackageSizeRepository;
import org.bme.micro_futar.shared.dtos.PackageSizeDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PackageSizeService {

    private final PackageSizeRepository packageSizeRepository;
    private final PackageSizeMapper packageSizeMapper;

    public List<PackageSizeDTO> getAllPackageSizes() {
        return packageSizeRepository.findAll().stream()
                .map(packageSizeMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<PackageSizeDTO> getPackageSizeById(Long id) {
        return packageSizeRepository.findById(id)
                .map(packageSizeMapper::toDTO);
    }
}
