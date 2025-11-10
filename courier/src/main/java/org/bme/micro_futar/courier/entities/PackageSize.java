package org.bme.micro_futar.courier.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class PackageSize {
    @Id
    private Long id;
    private String name;
    private Double maxLength;
}
