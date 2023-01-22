package site.lghtsg.api.resells.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResellBox {
    private long resellIdx;
    private String name;
    private String rateOfChange;
    private String rateCalDateDiff;
    private String iconImage;
    private String price;
}
