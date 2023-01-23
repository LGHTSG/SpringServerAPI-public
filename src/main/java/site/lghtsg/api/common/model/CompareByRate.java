package site.lghtsg.api.common.model;

import java.util.Comparator;

public class CompareByRate implements Comparator<Box> {
    @Override
    public int compare(Box o1, Box o2) {
        double o1Rate = o1.getRateOfChange(), o2Rate = o2.getRateOfChange();
        if (o1Rate < o2Rate) return 1;
        else if (o1Rate > o2Rate) return -1;
        return 0;
    }
}