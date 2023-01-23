package site.lghtsg.api.realestates.model;

import site.lghtsg.api.common.model.TransactionData;

public class RealEstateTransactionData extends TransactionData {

    public RealEstateTransactionData(String datetime, long price) {
        super(datetime, price);
    }
}
