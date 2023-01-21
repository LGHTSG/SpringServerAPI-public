package site.lghtsg.api.realestates.model.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@Builder
public class RealEstateTransaction {
    private Integer id;
    private Integer price;
    private LocalDate date;
    private Integer realEstateId;
}