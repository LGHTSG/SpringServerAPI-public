package site.lghtsg.api.resells.dataUploader.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResellTransaction {
    private int resellIdx;
    private int price;
    private String transactionTime;
}
