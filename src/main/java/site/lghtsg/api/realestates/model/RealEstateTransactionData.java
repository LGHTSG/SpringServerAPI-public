package site.lghtsg.api.realestates.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RealEstateTransactionData implements Comparable<RealEstateTransactionData> {
    String datetime;
    long price;

    @Override
    public int compareTo(RealEstateTransactionData rtd) {
        if(this.price < rtd.price){
            return 1;
        }
        else if(this.price > rtd.price) {
            return -1;
        }
        else return 0;
    }
}
