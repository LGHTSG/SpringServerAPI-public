package site.lghtsg.api.resells.dataUploader.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Resell {
    private String name;
    private String releasedPrice;
    private String releasedDate;
    private String color;
    private String brand;
    private String productNum;
    private String image1;
    private Long iconImageIdx;
}
