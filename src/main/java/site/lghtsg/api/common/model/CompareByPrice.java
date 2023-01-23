package site.lghtsg.api.common.model;

import java.util.Comparator;

public class CompareByPrice implements Comparator<Box> {
    @Override
    public int compare(Box o1, Box o2) {
        long o1Price = o1.getPrice(), o2Price = o2.getPrice();
        if (o1Price < o2Price) return 1;
        else if (o1Price > o2Price) return -1;
        return 0;
    }
}
