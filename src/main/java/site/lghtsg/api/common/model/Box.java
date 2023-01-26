package site.lghtsg.api.common.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Box {
    private long idx;
    private String name;
    private double rateOfChange;
    private String rateCalDateDiff;
    private String iconImage;
    private String transactionTime;
    private long price;
    private int saleCheck;
    private String updatedAt;
}
