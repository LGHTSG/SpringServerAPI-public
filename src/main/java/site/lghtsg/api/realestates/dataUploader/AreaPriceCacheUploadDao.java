package site.lghtsg.api.realestates.dataUploader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import site.lghtsg.api.realestates.model.RealEstateTransactionData;

import javax.sql.DataSource;
import java.util.List;

public class AreaPriceCacheUploadDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    /**
     * 서울특별시 소속된 평균가 입력과 함께 테이블의 모든 날짜별 row를 추가한다. -> 업데이트 할때는 어차피 하루만 다루면 되니 모든 일자 한번에 insert가 가능하겠지.
     */
    public void initAreaPriceCacheRow(List<RealEstateTransactionData> data, String initColumn){
        String initTableRowQuery = createTableRowInitQuery(data, initColumn);
        // 첫 row init 쿼리문 작성

        this.jdbcTemplate.update(initTableRowQuery);
    }

    static String createTableRowInitQuery(List<RealEstateTransactionData> data, String area) {
        StringBuilder ret = new StringBuilder();
        ret.append("insert into RealEstateAreaPriceCache (transactionDate, ");
        ret.append(area + ") values ");
        for(RealEstateTransactionData elem : data){
            ret.append("('" + elem.getDatetime() + "', " + elem.getPrice() + "), ");
        }
        ret.delete(ret.length() - 2, ret.length() - 1);
        ret.append(';');
        System.out.println(ret.toString());

        return ret.toString();
    }

}
