package site.lghtsg.api.users.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GetUserTransactionHistoryRes {
    private String transactionTime;
    private long price;
    private int sellCheck;
}
