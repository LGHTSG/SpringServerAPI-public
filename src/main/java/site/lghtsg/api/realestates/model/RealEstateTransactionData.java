package site.lghtsg.api.realestates.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RealEstateTransactionData {
    String datetime;
    long price;
}
