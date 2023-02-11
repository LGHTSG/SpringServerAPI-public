package site.lghtsg.api.users.model;

import lombok.*;
import org.checkerframework.checker.units.qual.A;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostMyAssetReq {
    private int assetIdx; // 구매한 자산 idx
    private String category;
    private long price; // 구매 가격
    private String transactionTime; // 구매 시간
}
