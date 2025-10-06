package org.bme.micro_futar.orders.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String senderName;
    public String senderEmail;
    public String senderPhone;
    public Long senderLocationCountryId;
    public String senderZip;
    public Long senderLocationCityId;
    public String senderAddress;
    public String recipientName;
    public String recipientEmail;
    public String recipientPhone;
    public String recipientLocationCountryId;
    public String recipientZip;
    public String recipientLocationCityId;
    public String recipientAddress;
    public Long packageSizeId;
    public boolean confirmed;
    //Not filled straight away
    public String parcelNumber;
    public Double price;
}
