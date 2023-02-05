package site.lghtsg.api.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionData implements Comparable<TransactionData>{

    String datetime;
    long price;

    @Override
    public int compareTo(TransactionData o) {
        // TODO: this.datetime이 어떠한 경우에서 null일 경우 처리를 해줘야함.
        // 우선은 AllArgsConstructor 로.
        return this.datetime.compareTo(o.datetime);
    }
}
