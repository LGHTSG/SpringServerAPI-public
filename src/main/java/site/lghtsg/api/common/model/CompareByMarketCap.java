package site.lghtsg.api.common.model;
import site.lghtsg.api.stocks.model.StockBox;

import java.util.Comparator;

import static site.lghtsg.api.config.Constant.ASCENDING_PARAM;


public class CompareByMarketCap implements Comparator<StockBox> {
    private String order;

    public CompareByMarketCap(String order){
        this.order = order;
    }
    public int compare(StockBox o1, StockBox o2) {
        double o1MarketCap = o1.getIssuedShares() * o1.getPrice(), o2MarketCap = o2.getIssuedShares() * o2.getPrice();
        int ret = Double.compare(o2MarketCap, o1MarketCap);

        if(order.equals(ASCENDING_PARAM)) ret *= -1;
        return ret;
    }
}