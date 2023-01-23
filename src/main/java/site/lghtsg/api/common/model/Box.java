package site.lghtsg.api.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
//@AllArgsConstructor
public class Box {
    private long idx;
    private String name;
    private double rateOfChange;
    private String rateCalDateDiff;
    private String iconImage;
    private long price;
}
