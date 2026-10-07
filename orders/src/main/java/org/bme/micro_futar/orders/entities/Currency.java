package org.bme.micro_futar.orders.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class Currency {
    @Id
    private Long id;
    private String code;
    private String name;
    private String symbol;
}
