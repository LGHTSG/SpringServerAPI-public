package site.lghtsg.api.realestates.dataUploader.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

@EqualsAndHashCode
@Getter
@AllArgsConstructor
@Builder
public class RealEstateTransaction {
    private Integer id;
    private Integer price;
    private LocalDate date;
    private Integer realEstateId;
}