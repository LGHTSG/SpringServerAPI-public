package site.lghtsg.api.resells.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class GetResellTransactionRes {
    private int price;
    private String transactionTime;
}
