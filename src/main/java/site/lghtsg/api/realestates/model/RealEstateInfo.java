package site.lghtsg.api.realestates.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RealEstateInfo {
    private long idx;
    private String name;
    private double rateOfChange;
    private String rateCalDateDiff;
    private String iconImage;
    private long price;
}
