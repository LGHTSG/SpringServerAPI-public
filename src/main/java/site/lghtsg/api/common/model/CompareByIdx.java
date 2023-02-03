package site.lghtsg.api.common.model;

import java.util.Comparator;

import static site.lghtsg.api.config.Constant.ASCENDING_PARAM;

public class CompareByIdx implements Comparator<Box>{
    private String order;
    @Override
    public int compare(Box o1, Box o2) {
        long o1Idx = o1.getIdx(), o2Idx = o2.getIdx();
        int ret = Long.compare(o2Idx, o1Idx);

        if(!order.equals(ASCENDING_PARAM)) ret *= -1;
        return ret;
    }
    public CompareByIdx(String order){
        this.order = order;
    }
}
