package dtos;

import enums.PackageSize;
import lombok.Data;

@Data
public class OrderDTO {
    public Long id;
    public String senderName;
    public String senderEmail;
    public String senderPhone;
    public String senderCountry;
    public String senderZip;
    public String senderCity;
    public String senderAddress;
    public String senderAddressExtra;
    public String recipientName;
    public String recipientEmail;
    public String recipientPhone;
    public String recipientCountry;
    public String recipientZip;
    public String recipientCity;
    public String recipientAddress;
    public String recipientAddressExtra;
    public PackageSize packageSize;
    //Not filled straight away
    public String parcelNumber;
    public Double price;
}
