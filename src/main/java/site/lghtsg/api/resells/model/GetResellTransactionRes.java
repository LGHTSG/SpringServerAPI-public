package site.lghtsg.api.resells.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;


@Getter
@Setter
@AllArgsConstructor
public class GetResellTransactionRes {
    private int resellIdx;
    private int price;
    private Timestamp transactionTime;
}
