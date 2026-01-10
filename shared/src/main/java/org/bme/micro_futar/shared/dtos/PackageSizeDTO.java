package org.bme.micro_futar.shared.dtos;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageSizeDTO {
    private Long id;
    @NonNull
    private String name;
    @NonNull
    private Double maxLength;
}
