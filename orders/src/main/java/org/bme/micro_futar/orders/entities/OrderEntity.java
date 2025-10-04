package org.bme.micro_futar.orders.entities;

import enums.PackageSize;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

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
    public PackageSize packageSize;
    //Not filled straight away
    public String parcelNumber;
    public Double price;
}
