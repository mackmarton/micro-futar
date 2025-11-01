package org.bme.micro_futar.shared.dtos;

import lombok.Data;
import lombok.NonNull;

@Data
public class PackageSizeDTO {
    private Long id;
    @NonNull
    private String name;
    @NonNull
    private Double maxLength;
}
