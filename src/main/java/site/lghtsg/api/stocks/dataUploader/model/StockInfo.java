package site.lghtsg.api.stocks.dataUploader.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class StockInfo {
    private int stockIdx;
    private String name;
    private String stockCode;
    private long issuedShares;
    private String url;
}
