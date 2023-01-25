package site.lghtsg.api.realestates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import site.lghtsg.api.realestates.model.RealEstateBox;
import site.lghtsg.api.realestates.model.RealEstateInfo;
import site.lghtsg.api.realestates.model.RealEstateTransactionData;
import site.lghtsg.api.realestates.dataUploader.model.RealEstate;
import site.lghtsg.api.realestates.dataUploader.model.RealEstateTransaction;
import site.lghtsg.api.realestates.dataUploader.model.RegionName;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

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
                        "     RealEstateTransaction as ret,\n" +
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
        String getRealEstateBoxesInAreaQuery =
                "select re.realEstateIdx,\n" +
                        "       re.name,\n" +
                        "       ret.price,\n" +
                        "       ret2.price as s2LastPrice,\n" +
                        "       ret.transactionTime,\n" +
                        "       ret2.transactionTime as s2TransactionTime,\n" +
                        "       ii.iconImage\n" +
                        "from RealEstate as re,\n" +
                        "     RealEstateTransaction as ret,\n" +
                        "     RealEstateTransaction as ret2,\n" +
                        "     IconImage as ii,\n" +
                        "     RegionName as rn\n" +
                        "where ret.realEstateTransactionIdx = re.lastTransactionIdx\n" +
                        "  and ret2.realEstateTransactionIdx = re.s2LastTransactionIdx\n" +
                        "  and re.legalTownCodeIdx = rn.legalTownCodeIdx\n" +
                        "  and re.iconImageIdx = ii.iconImageIdx" +
                "  and re.legalTownCodeIdx in (" +
                findAreaQuery + ")";

        Object[] getRealEstateBoxParams = new Object[]{area}; // 주입될 값들

        return this.jdbcTemplate.query(getRealEstateBoxesInAreaQuery, getRealEstateBoxParams, realEstateBoxRowMapper());
    }

    /**
     * TODO : FOR USER API
     * @brief
     * 단일 부동산의 box 반환 - 리스트에 들어가야 하는 데이터 + 정렬용 데이터
     * RealEstateBox(realEstateIdx, name, rateOfChange, rateCalDateDiff, iconImage, price)
     * @param realEstateIdx long
     * @return RealEstateBox
     */
    public RealEstateBox getRealEstateBox(long realEstateIdx) {
        String getRealEstateBoxQuery =
                "select re.realEstateIdx, re.name, ret.price, ret.transactionTime, II.iconImage\n" +
                "from RealEstate as re\n" +
                "INNER JOIN RealEstateTransaction as ret\n" +
                "ON re.realEstateIdx = ret.realEstateIdx and re.realEstateIdx = ? and ret.transactionTime = (\n" +
                "    select max(transactionTime) from RealEstateTransaction where realEstateIdx = ?\n" +
                "    )\n" +
                "INNER JOIN IconImage II on re.iconImageIdx = II.iconImageIdx\n" +
                "group by re.realEstateIdx;";
        Object[] getRealEstateBoxParams = new Object[]{realEstateIdx, realEstateIdx}; // 주입될 값들

        return this.jdbcTemplate.query(getRealEstateBoxQuery, getRealEstateBoxParams, realEstateBoxRowMapper()).get(0);
    }

    /**
     * 이거 안되면 ㄹㅇ 앱 포기각
     * @return
     */
    public List<RealEstateTransactionData> getAllTransactionData(){
        long start = System.currentTimeMillis();
        System.out.println(start);
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
     * @brief
     * 특정 부동산 정보 전달 - api 명세서 작성되어 있는 반환 데이터
     * RealEstateInfo(realEstateIdx, name, rateOfChange, rateCalDateDiff, iconImage, price)
     * @return RealEstateInfo
     */
    public RealEstateInfo getRealEstateInfo(long realEstateIdx) {
        String getRealEstateBoxQuery =
                "select re.realEstateIdx, re.name, ret.price, ret.transactionTime, II.iconImage\n" +
                        "from RealEstate as re\n" +
                        "INNER JOIN RealEstateTransaction as ret\n" +
                        "ON re.realEstateIdx = ret.realEstateIdx and re.realEstateIdx = ? and ret.transactionTime = (\n" +
                        "    select max(transactionTime) from RealEstateTransaction where realEstateIdx = ?\n" +
                        "    )\n" +
                        "INNER JOIN IconImage II on re.iconImageIdx = II.iconImageIdx\n" +
                        "group by re.realEstateIdx;";
        Object[] getRealEstateInfoParams = new Object[]{realEstateIdx, realEstateIdx}; // 주입될 값들

        return this.jdbcTemplate.query(getRealEstateBoxQuery, getRealEstateInfoParams, realEstateInfoRowMapper()).get(0);
    }

    /**
     * @brief
     * 특정 부동산 누적 가격 정보 전달
     * @param realEstateIdx long
     * @return List<RealEstateTransactionData>
     */
    public List<RealEstateTransactionData> getRealEstatePrices(long realEstateIdx){
        String getRealEstatePricesQuery =
                "select re.realEstateIdx, re.name, ret.price, ret.transactionTime\n" +
                "from RealEstate as re\n" +
                "INNER JOIN RealEstateTransaction as ret\n" +
                "ON re.realEstateIdx = ret.realEstateIdx and re.realEstateIdx = ?;";

        Object[] getRealEstatePricesParams = new Object[]{realEstateIdx}; // 주입될 값들
        return this.jdbcTemplate.query(getRealEstatePricesQuery, getRealEstatePricesParams, transactionRowMapper());
    }

    /**
     * TODO : 1. 같은 날 2번 이상의 거래 있는 경우 이는 어떻게 처리할지
     * TODO : 2. 아파트가 다르면 가격 기준 자체가 다르다. 그 동네 가격의 추세를 표현하려고 하는 데이터가, 각 아파트마다 다른 기준가로 들쭉날쭉하게 보일 것.
     * @brief
     * 특정 지역 누적 가격 정보 전달 - 전달은 가능,
     * @param area String
     * @return List<RealEstateTransactionData>
     */
    public List<RealEstateTransactionData> getRealEstatePricesInArea(String area){
        String findAreaQuery = getFindAreaQuery(area);
        String getRealEstatesAreaPrices = "select re.realEstateIdx, re.name, ret.price, ret.transactionTime\n" +
                "from RealEstate as re\n" +
                "         INNER JOIN ("+findAreaQuery+") as rn\n" +
                "                    on re.legalTownCodeIdx = rn.legalTownCodeIdx\n" +
                "         INNER JOIN RealEstateTransaction as ret\n" +
                "                    ON re.realEstateIdx = ret.realEstateIdx;";
        Object[] getRealEstateAreaPricesParams = new Object[]{area}; // 주입될 값들
        return this.jdbcTemplate.query(getRealEstatesAreaPrices, getRealEstateAreaPricesParams, transactionRowMapper());
    }


    private RowMapper<RealEstateTransactionData> transactionRowMapper() {
        return new RowMapper<RealEstateTransactionData>() {
            @Override
            public RealEstateTransactionData mapRow(ResultSet rs, int rowNum) throws SQLException {
                RealEstateTransactionData realEstateTransactionData = new RealEstateTransactionData(
                        rs.getString("transactionTime"),
                        rs.getLong("price")
                );
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

    private RowMapper<RealEstateInfo> realEstateInfoRowMapper(){
        return new RowMapper<RealEstateInfo>() {
            @Override
            public RealEstateInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                RealEstateInfo getRealEstateInfo = new RealEstateInfo();
                getRealEstateInfo.setIdx(rs.getLong("realEstateIdx"));
                getRealEstateInfo.setName(rs.getString("name"));
                getRealEstateInfo.setIconImage(rs.getString("iconImage"));
                getRealEstateInfo.setPrice(rs.getLong("price"));
                return getRealEstateInfo;
            }
        };
    }

    // 이렇게 안쓰고 싶은데.. 눈물이 난다...
    private String getFindAreaQuery(String area){
        String findAreaQuery;
        area.replace('+', ' ');
        String[] area_split = area.split(" ");
        if(area_split.length == 3){
            findAreaQuery =
                    "    select rn.legalTownCodeIdx\n" +
                            "    from RegionName as rn\n" +
                            "    where rn.name = ?\n";
        }
        else if(area_split.length == 2){
            findAreaQuery =
                    "    select rn.legalTownCodeIdx\n" +
                            "    from RegionName as rn\n" +
                            "    inner join RegionName as rn2\n" +
                            "    on rn2.legalTownCodeIdx = rn.parentIdx and rn2.name = ?\n" +
                            "    group by rn.legalTownCodeIdx\n";
        }
        else {
            findAreaQuery =
                    "    select rn.legalTownCodeIdx\n" +
                            "    from RegionName as rn\n" +
                            "    inner join RegionName as rn2\n" +
                            "    on rn2.legalTownCodeIdx = rn.parentIdx\n" +
                            "    inner join RegionName as rn3\n" +
                            "    on rn3.legalTownCodeIdx = rn2.parentIdx and rn3.name = ?\n" +
                            "    group by rn.legalTownCodeIdx\n";
        }
        return findAreaQuery;
    }
}
