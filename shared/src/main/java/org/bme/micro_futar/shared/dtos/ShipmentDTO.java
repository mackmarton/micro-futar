package org.bme.micro_futar.shared.dtos;

import lombok.*;
import org.bme.micro_futar.shared.enums.PaymentStatus;

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
    private Double senderLatitude;
    @NonNull
    private Double senderLongitude;
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
    private Double recipientLatitude;
    @NonNull
    private Double recipientLongitude;
    @NonNull
    private Long packageSizeId;
    private boolean confirmed;
    private boolean delivered;
    //Not filled straight away
    private String parcelNumber;
    /** Final price, set once logistics priced the shipment and the payment was captured. */
    private Double price;
    /** Upper bound authorized on the sender's card at checkout. */
    private Double maxPrice;
    private String currencyCode;
    private PaymentStatus paymentStatus;
}
