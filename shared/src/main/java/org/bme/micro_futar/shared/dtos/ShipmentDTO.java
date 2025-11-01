package org.bme.micro_futar.shared.dtos;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentDTO {
    private Long id;
    @NonNull
    private String senderName;
    @NonNull
    private String senderEmail;
    @NonNull
    private String senderPhone;
    @NonNull
    private Long senderLocationCountryId;
    @NonNull
    private String senderZip;
    @NonNull
    private Long senderLocationCityId;
    @NonNull
    private String senderAddress;
    @NonNull
    private String recipientName;
    @NonNull
    private String recipientEmail;
    @NonNull
    private String recipientPhone;
    @NonNull
    private Long recipientLocationCountryId;
    @NonNull
    private String recipientZip;
    @NonNull
    private Long recipientLocationCityId;
    @NonNull
    private String recipientAddress;
    @NonNull
    private Long packageSizeId;
    private boolean confirmed;
    //Not filled straight away
    private String parcelNumber;
    private Double price;
}
