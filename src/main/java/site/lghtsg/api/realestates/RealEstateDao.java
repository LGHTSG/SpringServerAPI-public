package site.lghtsg.api.realestates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import site.lghtsg.api.realestates.model.RealEstateBox;
import site.lghtsg.api.realestates.model.RealEstateTransactionData;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class RealEstateDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    /**
     * ==========================================================================================
     * TODO : 데이터만 받아옴. fluctuation 작업은 Provider 에서 할 것.
     * 모든 부동산의 box 반환
     * @return
     */
    public List<RealEstateBox> getAllRealEstateBoxes() {

        String getRealEstateBoxesQuery =
                "select re.realEstateIdx,\n" +
                        "       re.name,\n" +
                        "       ret.price,\n" +
                        "       ret2.price as s2LastPrice,\n" +
                        "       ret.transactionTime,\n" +
                        "       ret2.transactionTime as s2TransactionTime,\n" +
                        "       ii.iconImage\n" +
                        "from RealEstate as re,\n" +
                        "     RealEstateTodayTrans as ret,\n" +
                        "     RealEstateTransaction as ret2,\n" +
                        "     IconImage as ii,\n" +
                        "     RegionName as rn\n" +
                        "where ret.realEstateTransactionIdx = re.lastTransactionIdx\n" +
                        "  and ret2.realEstateTransactionIdx = re.s2LastTransactionIdx\n" +
                        "  and re.legalTownCodeIdx = rn.legalTownCodeIdx\n" +
                        "  and re.iconImageIdx = ii.iconImageIdx";

        return this.jdbcTemplate.query(getRealEstateBoxesQuery, realEstateBoxRowMapper());
    }

    /**
     * @brief
     * 특정 지역 포함되는 부동산 box 반환
     * @param area String
     */
    public List<RealEstateBox> getRealEstateBoxesInArea(String area){

        String findAreaQuery = getFindAreaQuery(area);
        String getRealEstateBoxesInAreaQuery = "select re.realEstateIdx,\n" +
                "       re.name,\n" +
                "       rett.price,\n" +
                "       ret.price           as s2LastPrice,\n" +
                "       rett.transactionTime,\n" +
                "       ret.transactionTime as s2TransactionTime,\n" +
                "       ii.iconImage\n" +
                "from RealEstate as re\n" +
                "         join RealEstateTodayTrans rett on re.lastTransactionIdx = rett.realEstateTransactionIdx\n" +
                "         join RealEstateTransaction ret on re.s2LastTransactionIdx = ret.realEstateTransactionIdx\n" +
                "         join RegionName rn on re.legalTownCodeIdx = rn.legalTownCodeIdx and rn.name like ?\n" +
                "         join IconImage ii on re.iconImageIdx = ii.iconImageIdx;";

        return this.jdbcTemplate.query(getRealEstateBoxesInAreaQuery, realEstateBoxRowMapper(), findAreaQuery);
    }

    /**
     * 이게 왜 필요하지?
     * @return
     */
    public List<RealEstateTransactionData> getAllTransactionData(){
        String getTransactionData =
                "select ret.realEstateTransactionIdx, ret.realEstateIdx, ret.price, ret.transactionTime\n" +
                "from RealEstateTransaction as ret;";

        return this.jdbcTemplate.query(getTransactionData, transactionRowMapper());
    }

     /**
     * 검색어 없는 경우 전체 리스트 전달
     * @return regionNames
     */
    public List<String> getAllRegionNames(){
        // 서울시부터 볼 수 있도록 법정동코드 기준
        String query = "select name from RegionName order by RegionName.legalTownCodeIdx";
        return this.jdbcTemplate.query(query, (rs, rowNum) -> rs.getString("name"));
    }

    /**
     * 검색어에 따른 지역 리스트 전달
     * @param keyword
     * @return regionNames
     */
    public List<String> getRegionNamesWithKeyword(String keyword) {
        String query = "Select name from RegionName where name like '%" + keyword + "%'";

        return this.jdbcTemplate.query(query, (rs, rowNum) -> rs.getString("name"));
    }


    /**
     * TODO : RealEstateBox와 RealEstateInfo 상호간 관계를 명확히해야 중복되는 코드 제거하는 리팩토링
     * -> wrapping 하는 것으로 대체
     * @brief
     * 특정 부동산 정보 전달 - api 명세서 작성되어 있는 반환 데이터
     * RealEstateInfo(realEstateIdx, name, rateOfChange, rateCalDateDiff, iconImage, price)
     * @return RealEstateInfo
     */
    public RealEstateBox getRealEstateBox(long realEstateIdx) {
        String getRealEstateBoxQuery =
                "select re.realEstateIdx,\n" +
                        "       re.name,\n" +
                        "       ret.price,\n" +
                        "       ret2.price           as s2LastPrice,\n" +
                        "       ret.transactionTime,\n" +
                        "       ret2.transactionTime as s2TransactionTime,\n" +
                        "       ii.iconImage\n" +
                        "from RealEstate as re,\n" +
                        "     RealEstateTodayTrans as ret,\n" +
                        "     RealEstateTransaction as ret2,\n" +
                        "     IconImage as ii,\n" +
                        "     RegionName as rn\n" +
                        "where ret.realEstateTransactionIdx = re.lastTransactionIdx\n" +
                        "  and ret2.realEstateTransactionIdx = re.s2LastTransactionIdx\n" +
                        "  and re.legalTownCodeIdx = rn.legalTownCodeIdx\n" +
                        "  and re.iconImageIdx = ii.iconImageIdx\n" +
                        "  and re.realEstateIdx = ?";

        return this.jdbcTemplate.queryForObject(getRealEstateBoxQuery, realEstateBoxRowMapper(), realEstateIdx);
    }

    /**
     * @brief
     * 특정 부동산 누적 가격 정보 전달 - 전체 가격
     * @param realEstateIdx long
     * @return List<RealEstateTransactionData>
     */
    public List<RealEstateTransactionData> getRealEstatePrices(long realEstateIdx){
        String getRealEstatePricesQuery =
                "select re.realEstateIdx, re.name, ret.price, ret.transactionTime\n" +
                        "from RealEstate as re\n" +
                        "         JOIN RealEstateTransaction ret ON re.realEstateIdx = ret.realEstateIdx and re.realEstateIdx = ?\n" +
                        "union\n" +
                        "select re.realEstateIdx, re.name, rett.price, rett.transactionTime\n" +
                        "from RealEstate as re\n" +
                        "         join RealEstateTodayTrans as rett on re.realEstateIdx = rett.realEstateIdx and re.realEstateIdx = ?\n" +
                        "order by transactionTime;";

        Object [] getRealEstatePricesParam = new Object[] {realEstateIdx, realEstateIdx};
        return this.jdbcTemplate.query(getRealEstatePricesQuery, transactionRowMapper(), getRealEstatePricesParam);
    }

    /**
     * @brief
     * 특정 지역 누적 가격 정보 전달 (업로드 용)
     * 가격 캐싱 없이 모든 누적 가격 데이터를 불러옴 - 같은 날 겹치는 가격 존재
     * @param area String
     * @return List<RealEstateTransactionData>
     */
    public List<RealEstateTransactionData> getRealEstatePricesInArea(String area){
        String findAreaQuery = getFindAreaQuery(area);
        System.out.println(findAreaQuery);
        String getRealEstatesAreaPrices =
                "select re.realEstateIdx, re.name, ret.price, ret.transactionTime\n" +
                        "from RealEstate as re\n" +
                        "         JOIN RealEstateTransaction ret ON re.realEstateIdx = ret.realEstateIdx\n" +
                        "         JOIN RegionName rn on re.legalTownCodeIdx = rn.legalTownCodeIdx and rn.name like ?;";
        return this.jdbcTemplate.query(getRealEstatesAreaPrices, transactionRowMapper(), findAreaQuery);
    }

    public void checkDateExists(RealEstateTransactionData now){
        String checkDateExists = "select transactionDate from RealEstateAreaPriceCache where transactionDate = ?;";
        this.jdbcTemplate.queryForObject(checkDateExists, String.class, now.getDatetime());
    }

    public void insertAreaCacheTable(RealEstateTransactionData now, String area){
        String insertAreaCacheTableQuery =
                "insert into RealEstateAreaPriceCache\n" +
                "(transactionDate, " + area + ") values (?, ?);";
        Object [] insertAreaCacheTableParam = new Object[]{now.getDatetime(), now.getPrice()};
        this.jdbcTemplate.update(insertAreaCacheTableQuery, insertAreaCacheTableParam);
    }

    /**
     * 특정 위치 특정 가격 값을 update한다.
     * @param now
     */
    public void updateAreaCacheTable(RealEstateTransactionData now, String area){
        String updateAreaCacheTableQuery =
                "update RealEstateAreaPriceCache\n" +
                "set " + area + " = ?\n" +
                "where transactionDate = ?;";
        Object [] updateAreaCacheTableParam = new Object[]{now.getPrice(), now.getDatetime()};
        this.jdbcTemplate.update(updateAreaCacheTableQuery, updateAreaCacheTableParam);
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



    private RowMapper<RealEstateTransactionData> transactionRowMapper() {
        return new RowMapper<RealEstateTransactionData>() {
            @Override
            public RealEstateTransactionData mapRow(ResultSet rs, int rowNum) throws SQLException {
                RealEstateTransactionData realEstateTransactionData = new RealEstateTransactionData();
                realEstateTransactionData.setPrice(rs.getLong("price"));
                realEstateTransactionData.setDatetime(rs.getString("transactionTime"));
                return realEstateTransactionData;
            }
        };
    }


    private RowMapper<RealEstateBox> realEstateBoxRowMapper(){
        return new RowMapper<RealEstateBox>() {
            @Override
            public RealEstateBox mapRow(ResultSet rs, int rowNum) throws SQLException {
                RealEstateBox getRealEstateBox = new RealEstateBox();
                getRealEstateBox.setIdx(rs.getLong("realEstateIdx"));
                getRealEstateBox.setName(rs.getString("name"));
                getRealEstateBox.setIconImage(rs.getString("iconImage"));
                getRealEstateBox.setPrice(rs.getLong("price"));
                getRealEstateBox.setS2Price(rs.getLong("s2LastPrice"));
                getRealEstateBox.setTransactionTime(rs.getString("transactionTime"));
                getRealEstateBox.setS2TransactionTime(rs.getString("s2TransactionTime"));
                return getRealEstateBox;
            }
        };
    }

    // 이렇게 안쓰고 싶은데.. 눈물이 난다...
    // -> parentIdx 버리고 like로 전환. 오히려 속도가 늘었다.
    private String getFindAreaQuery(String area){
        area = area.replace('+', ' ');
        area = area.replace('_', ' ');
        String findAreaQuery = area + "%";
        return findAreaQuery;
    }
}
