package site.lghtsg.api.common.model;
import site.lghtsg.api.stocks.model.StockBox;

import java.util.Comparator;


public class CompareByMarketCap implements Comparator<StockBox> {
    public int compare(StockBox o1, StockBox o2) {
        double o1MarketCap = o1.getIssuedShares() * o1.getPrice(), o2MarketCap = o2.getIssuedShares() * o2.getPrice();
        if (o1MarketCap < o2MarketCap) return 1;
        else if (o1MarketCap > o2MarketCap) return -1;
        return 0;
    }
}