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
    private int userTransactionIdx;
    private String assetName;
    private int price;
    private float rateOfChange;
    private String rateCalDateDiff;
    private String iconImage;
    private int saleCheck;
    private String updatedAt;
    private String category;
}