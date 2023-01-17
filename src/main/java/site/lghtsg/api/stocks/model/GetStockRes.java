package site.lghtsg.api.stocks.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetStockRes {
    private int stockIdx;
    private int stockTransactionIdx;
    private String name;
    private int price;
    //private int rateOfChange;
    private String rateCalDateDiff;
    private String iconImage;
}
