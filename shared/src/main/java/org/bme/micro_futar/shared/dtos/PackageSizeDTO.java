package org.bme.micro_futar.shared.dtos;

import lombok.Data;
import lombok.NonNull;

@Data
public class PackageSizeDTO {
    public Long id;
    @NonNull
    public String name;
    @NonNull
    public Double maxLength;
}
