package site.lghtsg.api.stocks.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetStockInfoRes {
    private int stockIdx;
    private int stockTransactionIdx;
    private String name;
    private int price;
    private long issuedShares;
    private int tradingVolume;
    //private String rateOfChange;
    private float rateOfChange;
    private String rateCalDateDiff;
    private String iconImage;
    private String transactionTime;
}
