package site.lghtsg.api.common.model;

import site.lghtsg.api.stocks.model.StockBox;

import java.util.Comparator;

import static site.lghtsg.api.config.Constant.DESCENDING_PARAM;

public class CompareByPrice implements Comparator<Box> {

    private String order;


    public CompareByPrice(String order){
        this.order = order;
    }
    @Override
    public int compare(Box o1, Box o2) {
        long o1Price = o1.getPrice(), o2Price = o2.getPrice();
        int ret = Long.compare(o2Price, o1Price);
        if(order.equals(DESCENDING_PARAM)) ret *= -1;
        return ret;
    }
}
