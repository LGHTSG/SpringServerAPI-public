package site.lghtsg.api.common.model;

import java.util.Comparator;

public class CompareByIdx implements Comparator<Box>{
    @Override
    public int compare(Box o1, Box o2) {
        double o1Rate = o1.getIdx(), o2Rate = o2.getIdx();
        if (o1Rate < o2Rate) return 1;
        else if (o1Rate > o2Rate) return -1;
        return 0;
    }
}
