package site.lghtsg.api.resells.model;

import lombok.Getter;
import lombok.Setter;
import site.lghtsg.api.common.model.Box;

@Getter
@Setter
public class GetResellBoxRes extends Box{
    private String imageUrl;
    private Long lastPrice;
}
