package site.lghtsg.api.realestates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.BaseResponse;
import site.lghtsg.api.realestates.model.RealEstateBox;
import site.lghtsg.api.realestates.model.RealEstateInfo;
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
     * 모든 부동산의 box 반환
     * @return
     */
    // 부동산 증감폭 계산하여 리스트 반환하는 로직이 필수는 아니지만, 다른 파트에서는 어떻게 구현?
    // 리스트가 길어지면 안되므로 짤라서 계산 -> dao 안에서 모든 계산이 이루어져야 함
    public List<RealEstateBox> getAllRealEstateBox(String sort, String order) {
        String getRealEstateBoxesQuery =
                "select re.realEstateIdx, re.name, ret.price, ret.transactionTime, II.iconImage\n" +
                "from RealEstate as re\n" +
                "INNER JOIN RealEstateTransaction as ret\n" +
                "ON re.realEstateIdx = ret.realEstateIdx and ret.transactionTime = (\n" +
                "    select max(transactionTime) from RealEstateTransaction where realEstateIdx = ret.realEstateIdx\n" +
                "    )\n" +
                "INNER JOIN IconImage II on re.iconImageIdx = II.iconImageIdx\n" +
                "group by re.realEstateIdx\n" +
                "order by re.realEstateIdx asc\n" +
                "LIMIT 100;";

        return this.jdbcTemplate.query(getRealEstateBoxesQuery, realEstateBoxRowMapper());
    }

    /**
     * @brief
     * 특정 지역 포함되는 부동산 box 반환
     * @param area String
     */
    public List<RealEstateBox> getRealEstateBoxesInArea(String area, String sort, String order){
        String findAreaQuery;
        area.replace('+', ' ');
        String[] area_split = area.split(" ");
        if(area_split.length == 3){
            findAreaQuery =
                    "    select rn.legaltowncodeidx, rn.name\n" +
                    "    from RegionName as rn\n" +
                    "    where rn.name = ?\n";
        }
        else if(area_split.length == 2){
            findAreaQuery =
                    "    select rn.legaltowncodeidx, rn.name\n" +
                    "    from RegionName as rn\n" +
                    "    inner join RegionName as rn2\n" +
                    "    on rn2.legalTownCodeIdx = rn.parentIdx and rn2.name = ?\n" +
                    "    group by rn.legalTownCodeIdx\n";
        }
        else {
            findAreaQuery =
                    "    select rn.legalTownCodeIdx, rn.name\n" +
                    "    from RegionName as rn\n" +
                    "    inner join RegionName as rn2\n" +
                    "    on rn2.legalTownCodeIdx = rn.parentIdx\n" +
                    "    inner join RegionName as rn3\n" +
                    "    on rn3.legalTownCodeIdx = rn2.parentIdx and rn3.name = ?\n" +
                    "    group by rn.legalTownCodeIdx\n";
        }
        String getRealEstateBoxesInAreaQuery =
                "select re.realEstateIdx, re.name, ret.price, ret.transactionTime, II.iconImage\n" +
                "from RealEstate as re\n" +
                "INNER JOIN RegionName as rn\n" +
                "ON re.legalTownCodeIdx = rn.legalTownCodeIdx\n" +
                "INNER JOIN RealEstateTransaction as ret\n" +
                "ON re.realEstateIdx = ret.realEstateIdx and ret.transactionTime = (\n" +
                "    select max(transactionTime) from RealEstateTransaction where realEstateIdx = ret.realEstateIdx\n" +
                "    )\n" +
                "INNER JOIN IconImage II on re.iconImageIdx = II.iconImageIdx\n" +
                "INNER JOIN (\n" +
                findAreaQuery +
                ") as rnn\n" +
                "on re.legalTownCodeIdx = rnn.legalTownCodeIdx\n" +
                "group by re.realEstateIdx\n" +
                "order by re.realEstateIdx asc\n" +
                "LIMIT 100;";
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
     * @brief
     * 지역 관계 리스트 전달
     */
    public List<String> getAreaRelationList(){
        return null;
    }


    /**
     * @brief
     * 특정 부동산 정보 전달 - api 명세서 작성되어 있는 반환 데이터
     * RealEstateInfo(realEstateIdx, name, rateOfChange, rateCalDateDiff, iconImage, price)
     * @return RealEstateInfo
     */
    public RealEstateInfo getAreaRelationInfo(long realEstateIdx){
        return null;
    }

    /**
     * @brief
     * 특정 부동산 누적 가격 정보 전달
     * @param realEstateIdx long
     * @return List<RealEstateTransactionData>
     */
    public List<RealEstateTransactionData> getRealEstatePrices(long realEstateIdx){
        String getRealEstateBoxQuery =
                "select re.realEstateIdx, re.name, ret.price, ret.transactionTime\n" +
                "from RealEstate as re\n" +
                "INNER JOIN RealEstateTransaction as ret\n" +
                "ON re.realEstateIdx = ret.realEstateIdx and re.realEstateIdx = ?;";

        Object[] getRealEstateBoxParams = new Object[]{realEstateIdx}; // 주입될 값들
        return this.jdbcTemplate.query(getRealEstateBoxQuery, getRealEstateBoxParams, transactionRowMapper());
    }

    /**
     * @brief
     * 특정 지역 누적 가격 정보 전달
     * @param area String
     * @return List<RealEstateTransactionData>
     */
    public List<RealEstateTransactionData> getAreaPrices(String area){
        return null;
    }


    private RowMapper<RealEstateTransactionData> transactionRowMapper() {
        return new RowMapper<RealEstateTransactionData>() {
            @Override
            public RealEstateTransactionData mapRow(ResultSet rs, int rowNum) throws SQLException {
                RealEstateTransactionData realEstateTransactionData = new RealEstateTransactionData();
                realEstateTransactionData.setDatetime(rs.getString("transactionTime"));
                realEstateTransactionData.setPrice(rs.getLong("price"));
                return realEstateTransactionData;
            }
        };
    }

    private RowMapper<RealEstateBox> realEstateBoxRowMapper(){
        return new RowMapper<RealEstateBox>() {
            @Override
            public RealEstateBox mapRow(ResultSet rs, int rowNum) throws SQLException {
                RealEstateBox getRealEstateBox = new RealEstateBox();
                getRealEstateBox.setRealEstateIdx(rs.getLong("realEstateIdx"));
                getRealEstateBox.setName(rs.getString("name"));
//                getRealEstateBox.setRateOfChange(rs.getString("rateOfChange"));
//                getRealEstateBox.setRateCalDateDiff(rs.getString("rateCalDateDiff"));
                getRealEstateBox.setIconImage(rs.getString("iconImage"));
                getRealEstateBox.setPrice(rs.getLong("price"));
                return getRealEstateBox;
            }
        };
    }
}
