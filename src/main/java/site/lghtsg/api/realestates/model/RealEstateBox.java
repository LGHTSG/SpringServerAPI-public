package site.lghtsg.api.realestates.model;

import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
@Setter
@NoArgsConstructor
public class RealEstateBox {
    // realEstate(realEstateIdx, name, price, rateOfChange, rateCalDiff, iconImage) 반환
    // 가격은 현재가에서 계산 -> provider 단계에서 계산해서 반환
    private long realEstateIdx;
    private String name;
    private String rateOfChange;
    private String rateCalDateDiff;
    private String iconImage;
    private long price;

//    public RealEstateBox(long realEstateIdx, String name, @Autowired(required = false) String rateOfChange, @Autowired(required = false) String rateCalDiff, String iconImage, int price){
//        this.realEstateIdx = realEstateIdx;
//        this.name = name;
//    }
}
