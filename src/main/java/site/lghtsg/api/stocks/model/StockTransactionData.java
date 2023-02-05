package site.lghtsg.api.stocks.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.lghtsg.api.common.model.TransactionData;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockTransactionData extends TransactionData {
    public StockTransactionData(int price, String transactionTime) {
    }
}
