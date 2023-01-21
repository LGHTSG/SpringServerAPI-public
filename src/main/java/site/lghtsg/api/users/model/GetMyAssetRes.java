package site.lghtsg.api.users.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetMyAssetRes {
    private String assetName;
    private String iconImage;
    private int price;
    private float rateOfChange;
    private String rateCalDateDiff;
}
