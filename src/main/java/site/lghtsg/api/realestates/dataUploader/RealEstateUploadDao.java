package site.lghtsg.api.realestates.dataUploader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import site.lghtsg.api.realestates.dataUploader.model.RealEstate;
import site.lghtsg.api.realestates.dataUploader.model.RealEstateTransaction;
import site.lghtsg.api.realestates.dataUploader.model.RegionName;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class RealEstateUploadDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * 부동산 정보 업로드
     * @param realEstateSet
     */
    public void uploadRealEstates(Set<RealEstate> realEstateSet) {
        // 한 업로드 단위(파일, api응답) 안에서의 중복 방지
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

        Object[] params = new Object[realEstateSet.size() * 2];
        int paramsIndex = 0;

        for (RealEstate realEstate : realEstateSet) {
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
     * TodayTrans에 업로드
     * @param transactionSet
     */
    public Set<Integer> uploadTransactions(List<RealEstateTransaction> transactionSet) {
        // insert
        StringBuilder queryBuilder = new StringBuilder("insert into RealEstateTransaction (price, transactionTime, realEstateIdx) values");
        Object[] params = new Object[transactionSet.size() * 3];

        Set<Integer> updatedREIdx = new HashSet<>(transactionSet.size());

        int paramsIndex = 0;

        for (RealEstateTransaction transaction : transactionSet) {
            params[paramsIndex++] = transaction.getPrice();
            params[paramsIndex++] = transaction.getDate();

            int REIdx = transaction.getRealEstateId();
            params[paramsIndex++] = REIdx;
            updatedREIdx.add(REIdx); // 중복 RealEstate는 제거됨

            queryBuilder.append("(?, ?, ?),");
        }

        String query = queryBuilder.substring(0, queryBuilder.length() - 1); // 끝에 , 제거

        jdbcTemplate.update(query, params);

        System.out.println("updatedREIdx.size() = " + updatedREIdx.size());

        return updatedREIdx;
    }

    public void updateTrs() {
        String setLastTrs = "update RealEstate RE " +
                "set RE.lastTransactionIdx = ( " +
                "select RET.realEstateTransactionIdx " +
                "from RealEstateTransaction RET " +
                "where RE.realEstateIdx = RET.realEstateIdx " +
                "order by RET.transactionTime desc ";

        String lastTr = "limit 0,1)";
        String s2LastTr = "limit 1,1)";

//        String updates2Last = "update RealEstate " +
//                "set s2LastTransactionIdx = lastTransactionIdx";

        this.jdbcTemplate.update(setLastTrs + s2LastTr);
        this.jdbcTemplate.update(setLastTrs + lastTr);
    }


    /**
     * 지역정보 업로드
     * @param regionNameList
     * @return
     */
    public String uploadRegionNames(List<RegionName> regionNameList) {

        StringBuilder queryBuilder = new StringBuilder("insert into RegionName(legalTownCodeIdx, name, parentIdx) values");
        Object[] params = new Object[regionNameList.size() * 3];

        int paramsIndex = 0;

        for (RegionName regionName : regionNameList) {
            Integer parentRegionId = regionName.getParentId();

            params[paramsIndex++] = regionName.getLegalCodeId();
            params[paramsIndex++] = regionName.getName();
            params[paramsIndex++] = parentRegionId; // 최상위 지역일 때

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
        String query = "select realEstateIdx, name, legalTownCodeIdx from `RealEstate`";

        return jdbcTemplate.query(query, (rs, rowNum) -> RealEstate.builder()
                .id(rs.getInt("realEstateIdx"))
                .name(rs.getString("name"))
                .regionId(rs.getInt("legalTownCodeIdx"))
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
                "select legalTownCodeIdx, concat(name, '동') from RegionName where name like '%상당구 북문로%' or name like '%상당구 남문로%'" +
                ") as regionname " +
                "where substring_index(name, ' ', 3) like '%도 %시 %구') " +
                "UNION (select legalTownCodeIdx, name from RegionName)";

        return jdbcTemplate.query(query, (rs, rowNum) -> { return RegionName.builder()
                .legalCodeId(rs.getInt("legalTownCodeIdx"))
                .name(rs.getString("name"))
                .build();});
    }

    /**
     * "~도 ~시 ~구" 형태의 데이터는 제외됨
     * @return
     */
    public List<String> getSigunguCodes() {
        String query = "select left(legalTownCodeIdx, 5) as legalTownCodeIdx from `RegionName`" +
                "where substring_index(name, ' ', 1) <> substring_index(name, ' ', 2) and substring_index(name, ' ', 3) not like '%도 %시 %구' " +
                "group by left(legalTownCodeIdx, 5)";

        return jdbcTemplate.query(query, (rs, rowNum) -> rs.getString("legalTownCodeIdx"));
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

    // 삭제 예정

//    public void updateTrs(Set<Integer> realEstateIdxs) {
//        // 옮길 데이터의 Idx 리스트
//        List<Integer> targetTrIdxs = new ArrayList<>();
//
//        // RealEstate.lastTransactionIdx 업데이트
//        String update =
//                "update RealEstate set lastTransactionIdx = \n" +
//                        "(select realEstateTransactionIdx from RealEstateTodayTrans\n" +
//                        "where realEstateIdx = ? order by transactionTime desc limit 1)\n" +
//                        "where realEstateIdx = ?";
//        // 옮길 Trs의 Idx 가져오기 (lastTr이 아닌 것들)
//        String getTargetTrIdxs =
//                "select realEstateTransactionIdx from RealEstateTodayTrans\n" +
//                        "where realEstateIdx = ? and realEstateTransactionIdx != (\n" +
//                        "select lastTransactionIdx from RealEstate where realEstateIdx = ?\n" +
//                        ")";
//
//        for (Integer reIdx : realEstateIdxs) {
//            this.jdbcTemplate.update(update, reIdx, reIdx);
//
//            targetTrIdxs.addAll(
//                    this.jdbcTemplate.query(getTargetTrIdxs, (rs, rowNum) -> rs.getInt("realEstateTransactionIdx"), reIdx, reIdx)
//            );
//        }
//
//        System.out.println("targetTrIdxs.size() = " + targetTrIdxs.size());
//
//        // 데이터 옮기기(TodayTrans -> Transaction)
//        String insert =
//                "insert into RealEstateTransaction (realEstateIdx, price, transactionTime, createdAt, updatedAt) " +
//                "select realEstateIdx, price, transactionTime, createdAt, updatedAt " +
//                "from RealEstateTodayTrans " +
//                "where realEstateTransactionIdx = ?";
//
//        String delete = "delete from RealEstateTodayTrans where realEstateTransactionIdx = ?";
//
//        for (Integer idx : targetTrIdxs) {
//
//            this.jdbcTemplate.update(insert, idx);
//            this.jdbcTemplate.update(delete, idx);
//        }
//
//        // s2Last 변경
//        String s2Lastupdate =
//                "update RealEstate set s2LastTransactionIdx = ( \n" +
//                "select realEstateTransactionIdx from RealEstateTransaction \n" +
//                "where realEstateIdx = ? order by transactionTime desc limit 1 \n" +
//                ") where realEstateIdx = ?";
//
//        for (Integer realEstateIdx : realEstateIdxs) {
//            this.jdbcTemplate.update(s2Lastupdate, realEstateIdx, realEstateIdx);
//        }
//        System.out.println("업데이트 완료");
//    }

}
