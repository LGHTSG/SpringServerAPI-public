package site.lghtsg.api.users.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostMyAssetReq {
    private int assetIdx; // 구매한 자산 idx
    private String category;
    private long price; // 구매 가격
}
