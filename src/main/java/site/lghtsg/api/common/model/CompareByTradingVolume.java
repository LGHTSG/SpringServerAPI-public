package site.lghtsg.api.common.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.lghtsg.api.common.model.Box;
import site.lghtsg.api.stocks.model.StockBox;

import java.util.Comparator;

@Getter
@Setter
@NoArgsConstructor
public class CompareByTradingVolume implements Comparator<StockBox> {
    public int compare(StockBox o1, StockBox o2) {
        double o1MarketCap = o1.getTradingVolume(), o2MarketCap = o2.getTradingVolume();
        if (o1MarketCap < o2MarketCap) return 1;
        else if (o1MarketCap > o2MarketCap) return -1;
        return 0;
    }
}