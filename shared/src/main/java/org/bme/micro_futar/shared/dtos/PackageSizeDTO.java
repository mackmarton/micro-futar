package org.bme.micro_futar.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
@AllArgsConstructor
public class PackageSizeDTO {
    private Long id;
    @NonNull
    private String name;
    @NonNull
    private Double maxLength;
}
