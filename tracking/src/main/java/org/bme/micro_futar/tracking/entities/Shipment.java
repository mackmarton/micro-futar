package org.bme.micro_futar.tracking.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Data
@Entity
@NoArgsConstructor
public class Shipment {
    @Id
    private Long id;
    private String senderName;
    private String senderEmail;
    private String senderPhone;
    private Long senderLocationCountryId;
    private String senderZip;
    private Long senderLocationCityId;
    private String senderAddress;
    private Double senderLatitude;
    private Double senderLongitude;
    private String recipientName;
    private String recipientEmail;
    private String recipientPhone;
    private String recipientLocationCountryId;
    private String recipientZip;
    private String recipientLocationCityId;
    private String recipientAddress;
    private Double recipientLatitude;
    private Double recipientLongitude;
    private Long packageSizeId;
    @ColumnDefault("false")
    private boolean delivered;
    private boolean confirmed;
    //Not filled straight away
    private String parcelNumber;
    private Double price;
    @ColumnDefault("'HUF'")
    private String currencyCode;
}
