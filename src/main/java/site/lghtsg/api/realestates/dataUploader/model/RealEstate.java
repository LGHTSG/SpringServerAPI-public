package site.lghtsg.api.realestates.dataUploader.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@Getter
@AllArgsConstructor
@Builder
public class RealEstate {
    private Integer id;
    private String name;
    private Integer regionId;
}
