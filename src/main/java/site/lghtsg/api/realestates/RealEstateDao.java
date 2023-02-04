package site.lghtsg.api.realestates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
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
     *
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
     * @param area String
     * @brief 특정 지역 포함되는 부동산 box 반환
     */
    public List<RealEstateBox> getRealEstateBoxesInArea(String area) {

        String findAreaQuery = getFindAreaWithLIKEQuery(area);
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
     *
     * @return
     */
    public List<RealEstateTransactionData> getAllTransactionData() {
        String getTransactionData =
                "select ret.realEstateTransactionIdx, ret.realEstateIdx, ret.price, ret.transactionTime\n" +
                        "from RealEstateTransaction as ret;";

        return this.jdbcTemplate.query(getTransactionData, transactionRowMapper());
    }

    /**
     * 검색어 없는 경우 전체 리스트 전달
     *
     * @return regionNames
     */
    public List<String> getAllRegionNames() {
        // 서울시부터 볼 수 있도록 법정동코드 기준
        String query = "select name from RegionName order by RegionName.legalTownCodeIdx";
        return this.jdbcTemplate.query(query, (rs, rowNum) -> rs.getString("name"));
    }

    /**
     * 검색어에 따른 지역 리스트 전달
     *
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
     * 특정 부동산 정보 전달 - api 명세서 작성되어 있는 반환 데이터
     *
     * @brief
     * queryForObject 는 반환값이 0이거나 2이상인 경우 IncorrectResultSizeDataAccessException 에러를 발생시킨다.
     * 기존에는 0 / 2이상 두 에러를 구분하려고 했으나,
     * DB 의 pk가 auto_increment 이기 때문에 2 이상의 결과가 나오는 경우가 없기에 0인 경우로만 생각하기로 하였음.
     *
     * @return RealEstateInfo
     * RealEstateInfo(realEstateIdx, name, rateOfChange, rateCalDateDiff, iconImage, price)
     */
    public RealEstateBox getRealEstateBox(long realEstateIdx)  {
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
        try {
            return this.jdbcTemplate.queryForObject(getRealEstateBoxQuery, realEstateBoxRowMapper(), realEstateIdx);
        }
        catch (IncorrectResultSizeDataAccessException error) {
            return null;
        }
    }

    /**
     * @param realEstateIdx long
     * @return List<RealEstateTransactionData>
     * @brief 특정 부동산 누적 가격 정보 전달 - 전체 가격
     */
    public List<RealEstateTransactionData> getRealEstatePrices(long realEstateIdx) {
        String getRealEstatePricesQuery =
                "select re.realEstateIdx, re.name, ret.price, ret.transactionTime\n" +
                        "from RealEstate as re\n" +
                        "         JOIN RealEstateTransaction ret ON re.realEstateIdx = ret.realEstateIdx and re.realEstateIdx = ?\n" +
                        "union\n" +
                        "select re.realEstateIdx, re.name, rett.price, rett.transactionTime\n" +
                        "from RealEstate as re\n" +
                        "         join RealEstateTodayTrans as rett on re.realEstateIdx = rett.realEstateIdx and re.realEstateIdx = ?\n" +
                        "order by transactionTime;";

        Object[] getRealEstatePricesParam = new Object[]{realEstateIdx, realEstateIdx};
        return this.jdbcTemplate.query(getRealEstatePricesQuery, transactionRowMapper(), getRealEstatePricesParam);
    }
//
//    public List<RealEstateTransactionData> getCachedRealEstatePricesInArea(String area){
//        // RealEstateAreaPriceCache 테이블에서 컬럼 이름
//        String findAreaCachedQuery = getFindAreaColumnFromCacheTable(area);
//
//
//    }

    /**
     * TODO : 캐싱된 테이블에서 가져오는 방식으로 변경 예정
     *
     * @param area String
     * @return List<RealEstateTransactionData>
     * @brief 특정 지역 누적 가격 정보 전달 (업로드 용)
     * 가격 캐싱 없이 모든 누적 가격 데이터를 불러옴 - 같은 날 겹치는 가격 존재
     */
    public List<RealEstateTransactionData> getRealEstatePricesInArea(String area) {
        String findAreaQuery = getFindAreaWithLIKEQuery(area);
        System.out.println(findAreaQuery);
        String getRealEstatesAreaPrices =
                "select re.realEstateIdx, re.name, ret.price, ret.transactionTime\n" +
                        "from RealEstate as re\n" +
                        "         JOIN RealEstateTransaction ret ON re.realEstateIdx = ret.realEstateIdx\n" +
                        "         JOIN RegionName rn on re.legalTownCodeIdx = rn.legalTownCodeIdx and rn.name like ?;";
        return this.jdbcTemplate.query(getRealEstatesAreaPrices, transactionRowMapper(), findAreaQuery);
    }

    public int checkDateExists(RealEstateTransactionData now) {
        String checkDateExists = "select transactionDate from RealEstateAreaPriceCache where transactionDate = ?;";
        try {
            this.jdbcTemplate.queryForObject(checkDateExists, String.class, now.getDatetime());
        }
        catch(IncorrectResultSizeDataAccessException error){
            return 0;
        }
        return 1;
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
     *
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

    /**
     * area 제대로 입력했는지 validation
     * 데이터가 변동이 없는 테이블이므로 area 가 일치하는 값이 존재하거나, 존재하지 않는 경우 2가지기에 queryForObject 사용
     *
     * @return
     */

    public int isInputAreaInAreaList(String area) {
        area = getFindAreaWithLIKEQuery(area);
        area = area.substring(0, area.length() - 1);
        String isInputAreaInAreaListQuery = "select rn.name from RegionName as rn where rn.name = ?;";
        try {
            this.jdbcTemplate.queryForObject(isInputAreaInAreaListQuery, String.class, area);
        } catch(IncorrectResultSizeDataAccessException error){
            return 0;
        }
        return 1;
    }

    private String getFindAreaColumnFromCacheTable(String area){
        return area.replace(' ', '_');
    }

    private String getFindAreaWithLIKEQuery(String area) {
        area = area.replace('+', ' ');
        area = area.replace('_', ' ');
        return area + "%";
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


    private RowMapper<RealEstateBox> realEstateBoxRowMapper() {
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
}

