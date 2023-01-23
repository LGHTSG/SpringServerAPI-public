package site.lghtsg.api.realestates.dataUploader.model;


import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class RegionName {
    private Integer legalCodeId;
    private String name;
    private Integer parentId;
}
