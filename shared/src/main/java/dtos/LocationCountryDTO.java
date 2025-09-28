package dtos;

import lombok.Data;
import lombok.NonNull;

@Data
public class LocationCountryDTO {
    public Long id;
    @NonNull
    public Long regionId;
    @NonNull
    public String name;
}
