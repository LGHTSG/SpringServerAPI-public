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
    private int assetIdx;
    private String assetName;
    private long price;
    private long s2Price; // 증감율 계산 위함
    private String transactionTime; // 증감율 계산 기간j
    private String s2TransactionTime; // 증감율 계산 기간
    private double rateOfChange;
    private String rateCalDateDiff;
    private String iconImage;
    private int sellCheck;
    private String updatedAt;
    private String category;
}