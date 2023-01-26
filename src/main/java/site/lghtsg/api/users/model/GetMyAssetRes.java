package site.lghtsg.api.users.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetMyAssetRes {
    private String assetName;
    private long price;
    private double rateOfChange;
    private String rateCalDateDiff;
    private String iconImage;
    private int saleCheck;
    private String updatedAt;
}