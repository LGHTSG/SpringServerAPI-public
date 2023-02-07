package site.lghtsg.api.stocks.dataUploader.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Stock {
    private int stockIdx;
    private String name;
    private int price;
    private int tadingVol;
    private String url;

    public Stock(int stockIdx, int price, int tadingVol) {
        this.stockIdx = stockIdx;
        this.price = price;
        this.tadingVol = tadingVol;
    }


}
