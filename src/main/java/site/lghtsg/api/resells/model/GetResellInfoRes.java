package site.lghtsg.api.resells.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetResellInfoRes {
    private Long resellIdx;
    private String name;
    private Long price;
    private Long lastPrice;
    private String releasedPrice;
    private String releasedDate;
    private String color;
    private String brand;
    private String productNum;
    private Double rateOfChange;
    private String rateCalDateDiff;
    private String image1;
    private String image2;
    private String image3;
    private String iconImage;
}
