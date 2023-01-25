package site.lghtsg.api.stocks.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.lghtsg.api.common.model.Box;


@Getter
@Setter
@NoArgsConstructor
public class StockBox extends Box {
    private long issuedShares;
    private int tradingVolume;
    private long closingPrice;
}
