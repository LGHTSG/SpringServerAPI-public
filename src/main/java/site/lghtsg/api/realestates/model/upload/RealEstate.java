package site.lghtsg.api.realestates.model.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class RealEstate {
    private Integer id;
    private String name;
    private Integer regionId;
}
