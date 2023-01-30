package site.lghtsg.api.users.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Asset {
    private int assetIdx;
    private String transactionTime;
    private long price;
    private String category;
    private int sellCheck;
}
