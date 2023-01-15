package site.lghtsg.api.realestates.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RealEstateData {
    private int realEstateIdx;
    private String name;
    private String iconImage;
}
