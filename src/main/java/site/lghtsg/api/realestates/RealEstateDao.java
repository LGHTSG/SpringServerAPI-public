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
import site.lghtsg.api.realestates.model.upload.RealEstate;
import site.lghtsg.api.realestates.model.upload.RealEstateTransaction;
import site.lghtsg.api.realestates.model.upload.RegionName;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
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
        String[] area_split = area.split(" ");
        if(area_split.length == 3){
            findAreaQuery =
                    "    select rn.legaltowncodeidx, rn.name\n" +
                    "    from RegionName as rn\n" +
                    "    where rn.name = '?'\n";
        }
        else if(area_split.length == 2){
            findAreaQuery =
                    "    select rn.legaltowncodeidx, rn.name\n" +
                    "    from RegionName as rn\n" +
                    "    inner join RegionName as rn2\n" +
                    "    on rn2.legalTownCodeIdx = rn.parentIdx and rn2.name = '?'\n" +
                    "    group by rn.legalTownCodeIdx\n";
        }
        else {
            findAreaQuery =
                    "    select rn.legalTownCodeIdx, rn.name\n" +
                    "    from RegionName as rn\n" +
                    "    inner join RegionName as rn2\n" +
                    "    on rn2.legalTownCodeIdx = rn.parentIdx\n" +
                    "    inner join RegionName as rn3\n" +
                    "    on rn3.legalTownCodeIdx = rn2.parentIdx and rn3.name = '?'\n" +
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
     * 지역 리스트 전달
     * @param keyword
     * @return regionNames
     */
    public List<String> getRegionNames(String keyword) {
        String query = "Select name from RegionName where name like '%" + keyword + "%'";

        return this.jdbcTemplate.query(query, (rs, rowNum) -> rs.getString("name"));
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


    // 데이터 업로드용

    /**
     * 부동산 정보 업로드
     * @param realEstateList
    */
    public void uploadRealEstates(Set<RealEstate> realEstateList) { // 한 업로드 단위(파일, api응답) 안에서의 중복 방지
        String createTempTable = "create temporary table RealEstate_temp select * from RealEstate limit 0, 0;\n";
        StringBuilder insertQueryBuilder = new StringBuilder("insert into RealEstate_temp (legalTownCodeIdx, name) values ");

        String insertOnlyNotDuplicated =
            "insert into RealEstate (legalTownCodeIdx, name)\n" +
            "select legalTownCodeIdx, name \n" +
            "from (\n" +
            "select temp.legalTownCodeIdx, temp.name\n" +
            "from RealEstate as r right join RealEstate_temp as temp on (r.legalTownCodeIdx, r.name) = (temp.legalTownCodeIdx, temp.name)\n" +
            "where r.legalTownCodeIdx is null\n" +
            ") as newData;";

        String dropTempTable = "drop table RealEstate_temp;";

        Object[] params = new Object[realEstateList.size() * 2];
        int paramsIndex = 0;

        for (RealEstate realEstate : realEstateList) {
            params[paramsIndex++] = realEstate.getRegionId();
            params[paramsIndex++] = realEstate.getName();

            insertQueryBuilder.append("(?, ?),");
        }

        String insertOnTempTable = insertQueryBuilder.substring(0, Math.max(insertQueryBuilder.length()-1, 0)) + ";\n";

        // 쿼리 실행
        this.jdbcTemplate.update(createTempTable);
        this.jdbcTemplate.update(insertOnTempTable, params);
        this.jdbcTemplate.update(insertOnlyNotDuplicated);
        this.jdbcTemplate.update(dropTempTable);

    }

    /**
     * 실거래가 정보 업로드
     * @param transactionList
     */
    public void uploadTransactions(List<RealEstateTransaction> transactionList) {
        StringBuilder queryBuilder = new StringBuilder("insert into `RealEstateTransaction`(price, transactionTime, realEstateIdx) values");
        String[] params = new String[transactionList.size() * 3];

        int paramsIndex = 0;

        for (RealEstateTransaction transaction : transactionList) {
            params[paramsIndex++] = String.valueOf(transaction.getPrice());
            params[paramsIndex++] = transaction.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            params[paramsIndex++] = String.valueOf(transaction.getRealEstateId());

            queryBuilder.append("(?, ?, ?),");
        }

        String query = queryBuilder.substring(0, queryBuilder.length() - 1); // 끝에 , 제거

        jdbcTemplate.update(query, params);
    }

    /**
     * 지역정보 업로드
     * @param regionNameList
     * @return
     */
    public String uploadRegionNames(List<RegionName> regionNameList) {
        StringBuilder queryBuilder = new StringBuilder("insert into RegionName(legalTownCodeIdx, name, parentIdx) values");
        String[] params = new String[regionNameList.size() * 3];

        int paramsIndex = 0;

        for (RegionName regionName : regionNameList) {
            Integer parentRegionId = regionName.getParentId();

            params[paramsIndex++] = regionName.getLegalCodeId().toString();
            params[paramsIndex++] = regionName.getName();
            params[paramsIndex++] = parentRegionId == null ? null : parentRegionId.toString(); // 최상위 지역일 때

            queryBuilder.append("(?, ?, ?),");
        }

        String query = queryBuilder.substring(0, queryBuilder.length() - 1); // 끝에 , 제거

        jdbcTemplate.update(query, params);

        return "success";
    }

    /**
     * 부동산 정보(id, 건물명) 가져오기
     * @return realEstateList
     */
    public List<RealEstate> getRealEstates() {
        String query = "select realEstateIdx, name from `RealEstate`";

        return jdbcTemplate.query(query, (rs, rowNum) -> RealEstate.builder()
                .id(rs.getInt("realEstateIdx"))
                .name(rs.getString("name"))
                .build());
    }

    public List<RegionName> getRegions() {
        String query = "select parentIdx, name, legalTownCodeIdx from `RegionName`";

        return jdbcTemplate.query(query, regionNameRowMapper());
    }

    /**
     * 엑셀 데이터 업로드용
     * @return
     */
    public List<RegionName> getRegionsForExcel() {
        String query = "(select legalTownCodeIdx, replace(name, '시 ', '') as name " +
                "from (" +
                "select legalTownCodeIdx, name from RegionName " +
                "union " +
                "select legalTownCodeIdx, concat(name, '동') from RegionName where name like '%상당구 북문로%'" +
                ") as regionname " +
                "where substring_index(name, ' ', 3) like '%도 %시 %구') " +
                "UNION (select legalTownCodeIdx, name from RegionName)";

        return jdbcTemplate.query(query, (rs, rowNum) -> { return RegionName.builder()
                .legalCodeId(rs.getInt("legalTownCodeIdx"))
                .name(rs.getString("name"))
                .build();});
    }

    public List<String> getSigunguCodes() {
        String query = "select left(legalTownCodeIdx, 5) as legalTownCodeIdx from `RegionName`" +
                "where substring_index(name, ' ', 1) <> substring_index(name, ' ', 2)" +
                "group by left(legalTownCodeIdx, 5)";

        return jdbcTemplate.query(query, (rs, rowNum) -> rs.getString("legalTownCodeIdx"));
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

    private RowMapper<RegionName> regionNameRowMapper() {
        return ((rs, rowNum) -> {
            RegionName regionName = RegionName.builder()
                    .legalCodeId(rs.getInt("legalTownCodeIdx"))
                    .name(rs.getString("name"))
                    .parentId(rs.getInt("parentIdx"))
                    .build();
            return regionName;
        });
    }
}
