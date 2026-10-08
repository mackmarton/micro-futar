package org.bme.micro_futar.orders.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Filter;

@Data
@Entity
@NoArgsConstructor
@Filter(name = "notDeleted")
public class Currency {
    @Id
    private Long id;
    private String code;
    private String name;
    private String symbol;
    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean deleted;
}
