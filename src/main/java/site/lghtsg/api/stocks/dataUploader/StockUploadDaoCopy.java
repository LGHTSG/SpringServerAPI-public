package site.lghtsg.api.stocks.dataUploader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import site.lghtsg.api.stocks.dataUploader.model.StockInfo;
import site.lghtsg.api.stocks.dataUploader.model.StockTransaction;

import javax.sql.DataSource;
import java.util.*;

/**
 * 자동 업로드 테스트용 DAO
 */
@Repository
public class StockUploadDaoCopy {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {this.jdbcTemplate = new JdbcTemplate(dataSource);}

    public void insertTimeFlag(String time) {
        String insertQuery = "insert into StockTransactionEmpty(stockIdx, price, transactionTime, tradingVolume)\n" +
                "values (999, 99999, '" + time + "', 999999)";

        this.jdbcTemplate.update(insertQuery);
    }

    /**
     * 실시간 스크래핑 데이터 업로드
     * @param transactions
     */
    public void uploadPrices(Map<Integer, StockTransaction> transactions) {
        // insert
        StringBuilder insert = new StringBuilder("insert into StockTodayTransTest(stockIdx, price, transactionTime, tradingVolume) values");
        Object[] params = new Object[transactions.size()*4];
        int paramIdx = 0;
        int idx = 0;
        int[] stockIdxs = new int[transactions.size()];


        for (StockTransaction tr : transactions.values()) {
            params[paramIdx++] = tr.getStockIdx();
            params[paramIdx++] = tr.getPrice();
            params[paramIdx++] = tr.getTransactionTime();
            params[paramIdx++] = tr.getTradingVol();

            stockIdxs[idx++] = tr.getStockIdx();

            insert.append("(?,?,?,?),");
        }

        String lastTrTime = params[2].toString();

        this.jdbcTemplate.update(insert.substring(0, insert.length()-1), params);

        // update lastTrs
        // 쿼리
        String createTempTb = "create temporary table lastTrs select stockTransactionIdx, stockIdx from StockTodayTransTest limit 0";
        String collectLastTrs =
                "insert into lastTrs(stockTransactionIdx, stockIdx)\n" +
                        "(select StockTransactionIdx, stockIdx from StockTodayTransTest where transactionTime = ?)";
        String updateTrs =
                "update StockTest set lastTransactionIdx = \n" +
                        "(select stockTransactionIdx from lastTrs where stockIdx = ?) where stockIdx = ?";
        String dropTempTb = "drop table lastTrs";

        // 실행
        this.jdbcTemplate.update(createTempTb);
        this.jdbcTemplate.update(collectLastTrs, lastTrTime);

        for (int stockIdx : stockIdxs) {
            this.jdbcTemplate.update(updateTrs, stockIdx, stockIdx);
        }

        this.jdbcTemplate.update(dropTempTb);
    }

    /**
     * 국내 일 단위 업데이트 종목 가격 업로드
     * @param transactions
     * @return
     */
    public Set<Integer> uploadPrices(List<StockTransaction> transactions) {
        StringBuilder sb = new StringBuilder("insert into StockTodayTransTest (stockIdx, price, transactionTime, tradingVolume) values");

        Object[] params = new Object[transactions.size()*4];
        int paramIdx = 0;

        Set<Integer> updatedStockIdxs = new HashSet<>(transactions.size());

        for (StockTransaction tr : transactions) {
            int stockIdx = tr.getStockIdx();
            params[paramIdx++] = stockIdx;
            updatedStockIdxs.add(stockIdx);

            params[paramIdx++] = tr.getPrice();
            params[paramIdx++] = tr.getTransactionTime();
            params[paramIdx++] = tr.getTradingVol();

            sb.append("(?, ?, ?, ?),");
        }

        String query = sb.substring(0, sb.length()-1);

        this.jdbcTemplate.update(query, params);

        return updatedStockIdxs;
    }

    /**
     * lastTr(종가) 만 남기고 제거
     * @param stockIdxs 국내 실시간 종목 리스트 or 해외 종목 리스트
     */
    public void clearTodayTrans(List<Integer> stockIdxs) {
        String delete = "delete from StockTodayTransTest where stockIdx = ? and stockTransactionIdx != (\n" +
                "select lastTransactionIdx from StockTest where stockIdx = ?)";

        for (Integer stockIdx : stockIdxs) {
            this.jdbcTemplate.update(delete, stockIdx, stockIdx);
        }
        System.out.println("정리 완료");
    }

    public void copyOldestTr(List<Integer> stockIdxs, boolean isDomestic) {
        // 상폐 등 거래 x 종목 처리 : (첫 1회 후) copy 대상에서 제외

        String condition = (isDomestic) ? "stockIdx not between 3685 and 4178" : "stockIdx between 3685 and 4178";
        String insert =
                "insert into StockTransactionEmpty (stockIdx, price, transactionTime, tradingVolume, createdAt, updatedAt) ( \n" +
                        "select stockIdx, price, transactionTime, tradingVolume, createdAt, updatedAt \n" +
                        "from StockTodayTransTest \n" +
                        "where stockIdx = ? and datediff((" +
                        "select max(transactionTime) from StockTodayTransTest where " + condition + " \n" +
                        "), transactionTime) = 0 " +    // 다른 종목들 copy될 때 한 번 같이 copy되고, 그 이후로는 되지 않음.
                        "order by transactionTime limit 1 \n" +
                        ")";

        String updateS2LastTr =
                "update StockTest set s2LastTransactionIdx = ( \n" +
                        "select stockTransactionIdx from StockTransaction \n" +
                        "where stockIdx = ? order by transactionTime desc limit 1 \n" +
                        ") where stockIdx = ?";

        for (Integer stockIdx : stockIdxs) {
            this.jdbcTemplate.update(insert, stockIdx);
            this.jdbcTemplate.update(updateS2LastTr, stockIdx, stockIdx);
        }
    }

    public void updateTrsOfDaily(Set<Integer> updatedStockIdxs) {
        //싱폐 등 거래 x 종목 처리 : 1회 복사
        List<Integer> targetTrIdxs = new ArrayList<>();

        // lastTr 업데이트
        String updateLastTr =
                "update StockTest set lastTransactionIdx = \n" +
                        "(select stockTransactionIdx from StockTodayTransTest \n" +
                        "where stockIdx = ? order by transactionTime desc limit 1) \n" +
                        "where stockIdx = ?";

        // 옮길 Tr Idx 가져오기(lastTr이 아닌 것들)
        String getTargetTrIdx =
                "select stockTransactionIdx from StockTodayTransTest \n" +
                        "where stockIdx = ? and stockTransactionIdx != (\n" +
                        "select lastTransactionIdx from StockTest \n" +
                        "where stockIdx = ?)";

        for (Integer stockIdx : updatedStockIdxs) {

            this.jdbcTemplate.update(updateLastTr, stockIdx, stockIdx);

            targetTrIdxs.addAll(
                    this.jdbcTemplate.query(getTargetTrIdx, (rs, rowNum) -> rs.getInt("stockTransactionIdx"), stockIdx, stockIdx)
            );
        }
        System.out.println("targetTrIdxs.size() = " + targetTrIdxs.size());

        // 옮기기
        String insert =
                "insert into StockTransactionEmpty(stockIdx, price, transactionTime, tradingVolume, createdAt, updatedAt) " +
                        "select stockIdx, price, transactionTime, tradingVolume, createdAt, updatedAt " +
                        "from StockTodayTransTest " +
                        "where stockTransactionIdx = ?";

        String delete = "delete from StockTodayTransTest where stockTransactionIdx = ?";

        for (Integer trIdx : targetTrIdxs) {
            this.jdbcTemplate.update(insert, trIdx);
            this.jdbcTemplate.update(delete, trIdx);
        }

        // s2LastTr 변경
        String updateS2LastTr =
                "update StockTest set s2LastTransactionIdx = ( \n" +
                        "select stockTransactionIdx from StockTransaction \n" +
                        "where stockIdx = ? order by transactionTime desc limit 1 \n" +
                        ") where stockIdx = ?";

        for (Integer stockIdx : updatedStockIdxs) {
            this.jdbcTemplate.update(updateS2LastTr, stockIdx, stockIdx);
        }

        // 상장폐지 및 거래정지 등 종목 처리

        // 실시간 x 국내주식이면서, 다른 것들과 거래일이 차이나고, 아직 처리 안 된 종목 찾기
        String findDelistedStock =
                "select sttt.stockTransactionIdx from StockTodayTransTest sttt \n" +
                        "inner join StockTest st on sttt.stockIdx = st.stockIdx \n" +
                        "inner join StockTransactionEmpty stt on st.s2LastTransactionIdx = stt.stockTransactionIdx\n" +
                        "where length(st.stockCode) >= 6 and st.url is null \n" +
                        "and datediff((select max(transactionTime) as transactionTime from StockTodayTransTest where stockIdx <= 3684), sttt.transactionTime) >= 1 \n" +
                        "and datediff(sttt.transactionTime, stt.transactionTime) != 0\n";

        List<Integer> trsHaveToCopy = this.jdbcTemplate.query(findDelistedStock, (rs, rowNum) -> rs.getInt("stockTransactionIdx"));

        if (trsHaveToCopy.isEmpty()) return;

        // 복사 및 s2LastTr 업데이트
        String copyToTrTable = "insert into StockTransactionEmpty (stockIdx, price, transactionTime, tradingVolume, createdAt, updatedAt) " +
                "select stockIdx, price, transactionTime, tradingVolume, createdAt, updatedAt " +
                "from StockTodayTransTest " +
                "where stockTransactionIdx = ?";

        String updateS2Last =
                "update StockTest set s2LastTransactionIdx = ( \n" +
                        "select stockTransactionIdx from StockTransactionTest \n" +
                        "where stockIdx = ? order by transactionTime desc limit 1 \n" +
                        ") where stockIdx = ?";

        for (Integer stockIdx : trsHaveToCopy) {
            this.jdbcTemplate.update(copyToTrTable, stockIdx);
            this.jdbcTemplate.update(updateS2Last, stockIdx, stockIdx);
        }
    }

    /**
     * 국내주식 정보(이름, 종목코드, 발행주식수) 입력
     * @param stockInfos
     */
    public void uploadDomesticInfo(List<StockInfo> stockInfos) {
        StringBuilder sb = new StringBuilder("insert into StockTest(name, stockCode, issuedShares) values");

        Object[] params = new Object[stockInfos.size()*3];
        int idx = 0;

        for (StockInfo stockInfo : stockInfos) {
            params[idx++] = stockInfo.getName();
            params[idx++] = stockInfo.getStockCode();
            params[idx++] = stockInfo.getIssuedShares();

            sb.append("(?, ?, ?),");
        }

        String query = sb.substring(0, sb.length()-1);

        this.jdbcTemplate.update(query, params);
    }

    public List<StockInfo> getUrlsAndIdxs() {
        String query = "select stockIdx, url from StockTest where url is not null limit 10000";

        return this.jdbcTemplate.query(query, (rs, rowNum) -> StockInfo.builder()
                .url(rs.getString("url"))
                .stockIdx(rs.getInt("stockIdx"))
                .build());
    }

    public void uploadKoreanStockUrl(StockInfo stockInfo) {
        String query = "update StockTest set url = ? where stockCode = ?";

        this.jdbcTemplate.update(query, stockInfo.getUrl(), stockInfo.getStockCode());
    }

    public void uploadSNPInfos(List<StockInfo> stockInfos) {
        StringBuilder queryBuilder = new StringBuilder("insert into StockTest(name, url) values");
        Object[] params = new Object[stockInfos.size() * 2];
        int paramsIndex = 0;

        for (StockInfo nameAndUrl : stockInfos) {
            params[paramsIndex++] = nameAndUrl.getName();
            params[paramsIndex++] = nameAndUrl.getUrl();

            queryBuilder.append("(?, ?),");
        }

        String query = queryBuilder.substring(0, queryBuilder.length()-1);

        this.jdbcTemplate.update(query, params);
    }

    public void uploadSNP500Details(StockInfo detail) {
        String query = "update StockTest set stockCode = ? , issuedShares = ? where url = ?";
//        String query = "update Stock set stockCode = ? where url = ?";

        this.jdbcTemplate.update(query, detail.getStockCode(), detail.getIssuedShares(), detail.getUrl());
    }


    public List<String> getAmericanStockUrls() {
//        String query = "select url from Stock where LENGTH(stockCode) <= 5";
        String query = "select url from StockTest where issuedShares is null";

        return this.jdbcTemplate.query(query, (rs, rowNum) -> rs.getString("url"));
    }

    public List<StockInfo> getNotServicedStockInfos() {
        String query = "select stockIdx, stockCode from StockTest where length(stockCode) >= 6 and url is null";
//        String query = "select stockIdx, stockCode from Stock where length(stockCode) >= 6";

        return this.jdbcTemplate.query(query, (rs, rowNum) -> StockInfo.builder()
                        .stockIdx(rs.getInt("stockIdx"))
                        .stockCode(rs.getString("stockCode"))
//                .issuedShares(rs.getLong("issuedShares"))
                        .build()
        );
    }

    /**
     *
     * @param isDomestic true : 국내, 실시간 / false : 해외
     * @return stockIdxList
     */
    public List<Integer> getStockIdxs(boolean isDomestic) {
//        String condition = (isDomestic) ? "stockIdx <= 3684 and url is not null" : "stockIdx > 3684";
        String condition = (isDomestic) ? "length(stockCode) >= 6 and url is not null" : "length(stockCode) <= 5";
        String query = "select stockIdx from StockTest where " + condition;

        return this.jdbcTemplate.query(query, (rs, rowNum) -> rs.getInt("stockIdx"));
    }



    public List<StockInfo> getSNPStockInfos() {
        String query = "select stockIdx, stockCode from StockTest where length(stockCode) <= 5";

        return this.jdbcTemplate.query(query, (rs, rowNum) -> StockInfo.builder()
                .stockIdx(rs.getInt("stockIdx"))
                .stockCode(rs.getString("stockCode"))
                .build());
    }

//    public List<Integer> getUpdatedStockIdxs() {
//        String query = "select st.stockIdx from Stock st INNER JOIN StockTransaction tr on st.stockIdx = tr.stockIdx\n" +
//                "where DATEDIFF(tr.createdAt, now()) = 0 group by st.stockIdx having count(*) > 1 limit 10000";
//
//        return new ArrayList<>(this.jdbcTemplate.query(query, (rs, rowNum) -> rs.getInt("stockIdx")));
//    }
}
