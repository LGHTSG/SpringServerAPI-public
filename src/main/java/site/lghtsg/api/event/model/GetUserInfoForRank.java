package site.lghtsg.api.event.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.Get;
import org.checkerframework.checker.units.qual.A;

@Getter
@Setter
@NoArgsConstructor
public class GetUserInfoForRank{
    private int userIdx;
    private String userName;
    private long userAsset;
    private String profileImg;
    private int remainingTransCnt;

}
