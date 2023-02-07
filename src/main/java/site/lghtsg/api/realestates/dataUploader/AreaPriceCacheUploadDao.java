package site.lghtsg.api.realestates.dataUploader;

import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import site.lghtsg.api.realestates.model.RealEstateTransactionData;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class AreaPriceCacheUploadDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    /**
     * 서울특별시 소속된 평균가 입력과 함께 테이블의 모든 날짜별 row를 추가한다. -> 업데이트 할때는 어차피 하루만 다루면 되니 모든 일자 한번에 insert가 가능하겠지.
     */
    public void initAreaPriceCacheRow(List<String> days){
        // 첫 row init 쿼리문 작성
        String initTableRowQuery = createTableRowInitQuery(days);

        this.jdbcTemplate.update(initTableRowQuery);
    }

    static String createTableRowInitQuery(List<String> days) {
        StringBuilder ret = new StringBuilder();
        ret.append("insert into RealEstateAreaPriceCache (transactionDate) ");
        ret.append(" values ");

        for(String elem : days) {
            ret.append("('").append(elem).append("'), ");
        }
        ret.delete(ret.length() - 2, ret.length() - 1);
        ret.append(';');
        System.out.println(ret.toString());

        return ret.toString();
    }


    public void insertAreaCacheTable(RealEstateTransactionData now, String area) {
        String insertAreaCacheTableQuery =
                "insert into RealEstateAreaPriceCache\n" +
                        "(transactionDate, " + area + ") values (?, ?);";
        Object[] insertAreaCacheTableParam = new Object[]{now.getDatetime(), now.getPrice()};
        this.jdbcTemplate.update(insertAreaCacheTableQuery, insertAreaCacheTableParam);
    }

    /**
     * 특정 위치 특정 가격 값을 update한다.
     * @param now
     */
    public void updateAreaCacheTable(RealEstateTransactionData now, String area) {
        String updateAreaCacheTableQuery =
                "update RealEstateAreaPriceCache\n" +
                        "set " + area + " = ?\n" +
                        "where transactionDate = ?;";
        Object[] updateAreaCacheTableParam = new Object[]{now.getPrice(), now.getDatetime()};
        this.jdbcTemplate.update(updateAreaCacheTableQuery, updateAreaCacheTableParam);
    }


}
