package site.lghtsg.api.realestates.dataUploader.model;

import lombok.*;

import java.time.LocalDate;

@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
@Builder
public class RealEstateTransaction {
    private Integer id;
    private Integer price;
    private String date;
    private Integer realEstateId;
    private String createdAt;
    private String updatedAt;
}