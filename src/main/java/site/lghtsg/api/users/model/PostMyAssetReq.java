package site.lghtsg.api.users.model;

import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
public class PostMyAssetReq {
    private int assetIdx; // 구매한 자산 idx
    private String category;
    private final long price; // 구매 가격
    private final String transactionTime; // 구매 시간
}
