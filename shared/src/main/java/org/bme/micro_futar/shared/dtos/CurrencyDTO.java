package org.bme.micro_futar.shared.dtos;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyDTO {
    private Long id;
    @NonNull
    private String code;
    @NonNull
    private String name;
    private String symbol;
}
