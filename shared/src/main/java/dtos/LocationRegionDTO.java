package dtos;

import lombok.Data;
import lombok.NonNull;

@Data
public class LocationRegionDTO {
    public Long id;
    @NonNull
    public String name;
}
