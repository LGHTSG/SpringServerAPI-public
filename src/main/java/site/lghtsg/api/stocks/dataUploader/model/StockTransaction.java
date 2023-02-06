package site.lghtsg.api.stocks.dataUploader.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class StockTransaction {
    private int stockIdx;
    private String transactionTime;
    private int tradingVol;
    private int price;

}
