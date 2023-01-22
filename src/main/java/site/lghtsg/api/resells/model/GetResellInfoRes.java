package site.lghtsg.api.resells.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetResellInfoRes {
    private int resellIdx;
    private String name;
    private String price;
    private String releasedPrice;
    private String releasedDate;
    private String color;
    private String brand;
    private String productNum;
    private String rateOfChange;
    private String rateCalDateDiff;
    private String image1;
    private String image2;
    private String image3;
    private int iconImageIdx;
}
