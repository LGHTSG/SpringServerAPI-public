package site.lghtsg.api.realestates.model;

import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
@Setter
public class GetRealEstateBox {
    // realEstate(realEstateIdx, name, price, rateOfChange, rateCalDiff, iconImage) 반환
    // 가격은 현재가에서 계산 -> provider 단계에서 계산해서 반환
    private int realEstateIdx;
    private String name;
    private String rateOfChange;
    private String rateCalDiff;
    private String iconImage;
    private int price;

    public GetRealEstateBox(int realEstateIdx, String name, @Autowired(required = false) String rateOfChange, @Autowired(required = false) String rateCalDiff, String iconImage, int price){
        this.realEstateIdx = realEstateIdx;
        this.name = name;
    }
}
