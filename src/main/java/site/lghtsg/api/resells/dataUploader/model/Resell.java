package site.lghtsg.api.resells.dataUploader.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Resell {
    private String name;
    private String releasedPrice;
    private String releasedDate;
    private String color;
    private String brand;
    private String productNum;
    private String image1;
    private String image2;
    private String image3;
    private int productCode;
    private Long iconImageIdx;
}
