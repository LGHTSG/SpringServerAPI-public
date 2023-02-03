package site.lghtsg.api.common.model;

import java.util.Comparator;

import static site.lghtsg.api.config.Constant.ASCENDING_PARAM;

public class CompareByRate implements Comparator<Box> {

    private String order;


    public CompareByRate(String order) {
        this.order = order;
    }

    @Override
    public int compare(Box o1, Box o2) {
        double o1Rate = o1.getRateOfChange(), o2Rate = o2.getRateOfChange();
        int ret = Double.compare(o2Rate, o1Rate);

        if(!order.equals(ASCENDING_PARAM)) ret *= -1;
        return ret;
    }
}