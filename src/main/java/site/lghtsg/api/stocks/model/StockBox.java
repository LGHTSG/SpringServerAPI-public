package site.lghtsg.api.stocks.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockBox {
    private long stockIdx;
    private String name;
    private int price;
    //private String rateOfChange;
    private float rafeOfChange;
    private String rateCalDateDiff;
    private String iconImage;
}
