package site.lghtsg.api.common.model;

import site.lghtsg.api.stocks.model.StockBox;

import java.util.Comparator;

import static site.lghtsg.api.config.Constant.DESCENDING_PARAM;

public class CompareByTradingVolume implements Comparator<StockBox> {
    private String order;

    public CompareByTradingVolume(String order) {
        this.order = order;
    }
    public int compare(StockBox o1, StockBox o2) {
        double o1MarketCap = o1.getTradingVolume(), o2MarketCap = o2.getTradingVolume();
        int ret = Double.compare(o2MarketCap, o1MarketCap);

        if(order.equals(DESCENDING_PARAM)) ret *= -1;
        return ret;
    }
}