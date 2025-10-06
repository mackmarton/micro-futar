package dtos;

import lombok.Data;
import lombok.NonNull;

@Data
public class OrderDTO {
    public Long id;
    @NonNull
    public String senderName;
    @NonNull
    public String senderEmail;
    @NonNull
    public String senderPhone;
    @NonNull
    public Long senderLocationCountryId;
    @NonNull
    public String senderZip;
    @NonNull
    public Long senderLocationCityId;
    @NonNull
    public String senderAddress;
    @NonNull
    public String recipientName;
    @NonNull
    public String recipientEmail;
    @NonNull
    public String recipientPhone;
    @NonNull
    public Long recipientLocationCountryId;
    @NonNull
    public String recipientZip;
    @NonNull
    public Long recipientLocationCityId;
    @NonNull
    public String recipientAddress;
    @NonNull
    public Long packageSizeId;
    public boolean confirmed;
    //Not filled straight away
    public String parcelNumber;
    public Double price;
}
