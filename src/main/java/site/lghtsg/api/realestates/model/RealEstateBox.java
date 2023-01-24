package site.lghtsg.api.realestates.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.lghtsg.api.common.model.Box;


@Getter
@Setter
@NoArgsConstructor
public class RealEstateBox extends Box {
    private long s2Price;
}
