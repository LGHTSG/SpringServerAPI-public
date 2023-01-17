package site.lghtsg.api.realestates.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RealEstateInfo {

    private long realEstateIdx;
    private String name;
    private String rateOfChange;
    private String rateCalDateDiff;
    private String iconImage;
    private long price;

}
