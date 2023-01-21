package site.lghtsg.api.stocks;


import org.apache.commons.codec.binary.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import site.lghtsg.api.stocks.model.GetStockInfoRes;
import site.lghtsg.api.stocks.model.GetStockPricesRes;
import site.lghtsg.api.stocks.model.StockBox;

import javax.sql.DataSource;
import java.util.List;

import static site.lghtsg.api.config.Constant.ASCENDING_PARAM;
import static site.lghtsg.api.config.Constant.LIST_LIMIT_QUERY;


@Repository
public class StockDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {this.jdbcTemplate = new JdbcTemplate(dataSource);}



    // sort 기준 오름차순, 내림차순 조회
    public List<StockBox> getStockBoxes(String sort, String order) {

        String getStocksQuery = "select S.stockIdx, ST.stockTransactionIdx, S.name, ST.price, I.iconImage, ";

        //거래량
        /*
        if(sort.equals("trading-volume") && order.equals("ascending")){
            getStocksQuery += "ST.tradingVolume from Stock as S left join IconImage I on S.iconImageIdx = I.iconImageIdx " +
                    "left join StockTransaction ST on S.stockIdx = ST.stockIdx " +
                    "inner join (select S.name, max(ST.transactionTime) as maxtime from Stock as S join StockTransaction ST " +
                    "on S.stockIdx = ST.stockIdx group by S.name) " +
                    "dt on S.name = dt.name and ST.transactionTime = dt.maxtime order by ST.tradingVolume asc limit 100";
        }
        if(sort.equals("trading-volume") && order.equals("descending")){
            getStocksQuery += "ST.tradingVolume from Stock as S left join IconImage I on S.iconImageIdx = I.iconImageIdx " +
                    "left join StockTransaction ST on S.stockIdx = ST.stockIdx " +
                    "inner join (select S.name, max(ST.transactionTime) as maxtime from Stock as S join StockTransaction ST " +
                    "on S.stockIdx = ST.stockIdx group by S.name) " +
                    "dt on S.name = dt.name and ST.transactionTime = dt.maxtime order by ST.tradingVolume desc limit 100";
        }
         */
        if(StringUtils.equals(sort, "trading-volume") && StringUtils.equals(order, ASCENDING_PARAM)){
            getStocksQuery += "cast(((ST.price-F.price)/F.price*100) as decimal(10,1)) as rateOfChange, ST.tradingVolume " +
                    "from Stock as S left join IconImage I on S.iconImageIdx = I.iconImageIdx " +
                    "left join StockTransaction ST on S.stockIdx = ST.stockIdx " +
                    "inner join (select S.name, max(ST.transactionTime) as maxtime " +
                    "from Stock as S join StockTransaction ST on S.stockIdx = ST.stockIdx group by S.name) dt " +
                    "on S.name = dt.name and ST.transactionTime = dt.maxtime " +
                    "left join (select * from (select stockIdx, price " +
                    "from StockTransaction where date(transactionTime) = curdate() - 1 " +
                    "order by stockTransactionIdx desc limit 18446744073709551615) res group by stockIdx) as F " +
                    "on ST.stockIdx = F.stockIdx order by tradingVolume asc limit 100";
        }
        else if(StringUtils.equals(sort, "trading-volume")){
            getStocksQuery += "cast(((ST.price-F.price)/F.price*100) as decimal(10,1)) as rateOfChange, ST.tradingVolume " +
                    "from Stock as S left join IconImage I on S.iconImageIdx = I.iconImageIdx " +
                    "left join StockTransaction ST on S.stockIdx = ST.stockIdx " +
                    "inner join (select S.name, max(ST.transactionTime) as maxtime " +
                    "from Stock as S join StockTransaction ST on S.stockIdx = ST.stockIdx group by S.name) dt " +
                    "on S.name = dt.name and ST.transactionTime = dt.maxtime " +
                    "left join (select * from (select stockIdx, price " +
                    "from StockTransaction where date(transactionTime) = curdate() - 1 " +
                    "order by stockTransactionIdx desc limit 18446744073709551615) res group by stockIdx) as F " +
                    "on ST.stockIdx = F.stockIdx order by tradingVolume desc limit 100";
        }

        //시가 총액
        /*
        if(sort.equals("market-cap") && order.equals("ascending")){
            getStocksQuery += "S.issuedShares * ST.price as marketCap " +
                    "from Stock as S left join IconImage I on S.iconImageIdx = I.iconImageIdx left join StockTransaction ST " +
                    "on S.stockIdx = ST.stockIdx " +
                    "inner join (select S.name, max(ST.transactionTime) as maxtime " +
                    "from Stock as S join StockTransaction ST on S.stockIdx = ST.stockIdx group by S.name) dt " +
                    "on S.name = dt.name and ST.transactionTime = dt.maxtime order by marketCap asc limit 100";
        }
        if(sort.equals("market-cap") && order.equals("descending")){
            getStocksQuery += "S.issuedShares * ST.price as marketCap " +
                    "from Stock as S left join IconImage I on S.iconImageIdx = I.iconImageIdx left join StockTransaction ST " +
                    "on S.stockIdx = ST.stockIdx " +
                    "inner join (select S.name, max(ST.transactionTime) as maxtime " +
                    "from Stock as S join StockTransaction ST on S.stockIdx = ST.stockIdx group by S.name) dt " +
                    "on S.name = dt.name and ST.transactionTime = dt.maxtime order by marketCap desc limit 100";
        }
        */

        if(StringUtils.equals(sort, "market-cap") && StringUtils.equals(order, ASCENDING_PARAM)){
            getStocksQuery += "cast(((ST.price-F.price)/F.price*100) as decimal(10,1)) as rateOfChange, " +
                    "S.issuedShares * ST.price as marketCap " +
                    "from Stock as S left join IconImage I on S.iconImageIdx = I.iconImageIdx " +
                    "left join StockTransaction ST on S.stockIdx = ST.stockIdx " +
                    "inner join (select S.name, max(ST.transactionTime) as maxtime " +
                    "from Stock as S join StockTransaction ST on S.stockIdx = ST.stockIdx group by S.name) dt " +
                    "on S.name = dt.name and ST.transactionTime = dt.maxtime " +
                    "left join (select * from (select stockIdx, price " +
                    "from StockTransaction where date(transactionTime) = curdate() - 1 " +
                    "order by stockTransactionIdx desc limit 18446744073709551615) res group by stockIdx) as F " +
                    "on ST.stockIdx = F.stockIdx order by marketCap desc limit 100";
        }
        else if(StringUtils.equals(sort, "market-cap")){
            getStocksQuery += "cast(((ST.price-F.price)/F.price*100) as decimal(10,1)) as rateOfChange, " +
                    "S.issuedShares * ST.price as marketCap " +
                    "from Stock as S left join IconImage I on S.iconImageIdx = I.iconImageIdx " +
                    "left join StockTransaction ST on S.stockIdx = ST.stockIdx " +
                    "inner join (select S.name, max(ST.transactionTime) as maxtime " +
                    "from Stock as S join StockTransaction ST on S.stockIdx = ST.stockIdx group by S.name) dt " +
                    "on S.name = dt.name and ST.transactionTime = dt.maxtime " +
                    "left join (select * from (select stockIdx, price " +
                    "from StockTransaction where date(transactionTime) = curdate() - 1 " +
                    "order by stockTransactionIdx desc limit 18446744073709551615) res group by stockIdx) as F " +
                    "on ST.stockIdx = F.stockIdx order by marketCap asc limit 100";
        }

        //등락폭
        if(StringUtils.equals(sort, "fluctuation") && StringUtils.equals(order, ASCENDING_PARAM)){
            getStocksQuery += "cast(((ST.price-F.price)/F.price*100) as decimal(10,1)) as rateOfChange " +
                    "from Stock as S left join IconImage I on S.iconImageIdx = I.iconImageIdx " +
                    "left join StockTransaction ST on S.stockIdx = ST.stockIdx " +
                    "inner join (select S.name, max(ST.transactionTime) as maxtime " +
                    "from Stock as S join StockTransaction ST on S.stockIdx = ST.stockIdx group by S.name) dt " +
                    "on S.name = dt.name and ST.transactionTime = dt.maxtime " +
                    "left join (select * from " +
                    "(select stockIdx, price from StockTransaction " +
                    "where date(transactionTime) = curdate() - 1 order by stockTransactionIdx desc limit 18446744073709551615) " +
                    "res group by stockIdx) as F on ST.stockIdx = F.stockIdx order by rateOfChange asc limit 100";
        }
        else if(StringUtils.equals(sort, "fluctuation")){
            getStocksQuery += "cast(((ST.price-F.price)/F.price*100) as decimal(10,1)) as rateOfChange " +
                    "from Stock as S left join IconImage I on S.iconImageIdx = I.iconImageIdx " +
                    "left join StockTransaction ST on S.stockIdx = ST.stockIdx " +
                    "inner join (select S.name, max(ST.transactionTime) as maxtime " +
                    "from Stock as S join StockTransaction ST on S.stockIdx = ST.stockIdx group by S.name) dt " +
                    "on S.name = dt.name and ST.transactionTime = dt.maxtime " +
                    "left join (select * from " +
                    "(select stockIdx, price from StockTransaction " +
                    "where date(transactionTime) = curdate() - 1 order by stockTransactionIdx desc limit 18446744073709551615) " +
                    "res group by stockIdx) as F on ST.stockIdx = F.stockIdx order by rateOfChange desc limit 100";
        }

        try{
            return this.jdbcTemplate.query(getStocksQuery,
                    (rs, rowNum) -> new StockBox(
                            rs.getLong("stockIdx"),
                            rs.getString("name"),
                            rs.getInt("price"),
                            //calculateRateOfChange(rs.getInt("stockIdx")),
                            rs.getFloat("rateOfChange"),
                            "어제",
                            rs.getString("iconImage")
                    ));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }

    }

    //stockIdx 기준 오름차순, 내림차순 조회
    public List<StockBox> getStockBoxesByIdx(String order) {
        /*
        String getStocksByIdxQuery = "select S.stockIdx, ST.stockTransactionIdx, S.name, ST.price, I.iconImage " +
                "from Stock as S left join IconImage I on S.iconImageIdx = I.iconImageIdx left join StockTransaction ST on S.stockIdx = ST.stockIdx " +
                "inner join (select S.name, max(ST.transactionTime) as maxtime from Stock as S join StockTransaction ST on S.stockIdx = ST.stockIdx " +
                "group by S.name) dt on S.name = dt.name and ST.transactionTime = dt.maxtime order by S.stockIdx ";

         */
        String getStocksByIdxQuery = "select S.stockIdx, ST.stockTransactionIdx, S.name, ST.price, I.iconImage, " +
                "cast(((ST.price-F.price)/F.price*100) as decimal(10,1)) as rateOfChange " +
                "from Stock as S left join IconImage I on S.iconImageIdx = I.iconImageIdx " +
                "left join StockTransaction ST on S.stockIdx = ST.stockIdx " +
                "inner join (select S.name, max(ST.transactionTime) as maxtime " +
                "from Stock as S join StockTransaction ST on S.stockIdx = ST.stockIdx group by S.name) dt " +
                "on S.name = dt.name and ST.transactionTime = dt.maxtime " +
                "left join (select * from (select stockIdx, price " +
                "from StockTransaction where date(transactionTime) = curdate() - 1 " +
                "order by stockTransactionIdx desc limit 18446744073709551615) res group by stockIdx) as F " +
                "on ST.stockIdx = F.stockIdx order by stockIdx ";


        if(StringUtils.equals(order, ASCENDING_PARAM)){
            getStocksByIdxQuery += "ASC " + LIST_LIMIT_QUERY;
        }
        else {
            getStocksByIdxQuery += "DESC " + LIST_LIMIT_QUERY;
        }

        try {
            return this.jdbcTemplate.query(getStocksByIdxQuery,
                    (rs, rowNum) -> new StockBox(
                            rs.getLong("stockIdx"),
                            rs.getString("name"),
                            rs.getInt("price"),
                            //calculateRateOfChange(rs.getInt("stockIdx")),
                            rs.getFloat("rateOfChange"),
                            "어제",
                            rs.getString("iconImage")
                    ));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }

    }

    //특정 주식 정보 조회
    public GetStockInfoRes getStockInfo(int stockIdx) {

        /*
        String getStockInfoQuery = "select S.stockIdx, ST.stockTransactionIdx, S.name, ST.price, S.issuedShares, ST.tradingVolume, I.iconImage, ST.transactionTime " +
                "from Stock as S left join IconImage I on S.iconImageIdx = I.iconImageIdx " +
                "left join StockTransaction ST on S.stockIdx = ST.stockIdx inner join (select S.name, max(ST.transactionTime) as maxtime " +
                "from Stock as S join StockTransaction ST on S.stockIdx = ST.stockIdx group by S.name) " +
                "dt on S.name = dt.name and ST.transactionTime = dt.maxtime where S.stockIdx = ?";

         */

        String getStockInfoQuery = "select S.stockIdx, ST.stockTransactionIdx, S.name, ST.price, S.issuedShares, ST.tradingVolume, ST.transactionTime, I.iconImage, " +
                "cast(((ST.price-F.price)/F.price*100) as decimal(10,1)) as rateOfChange " +
                "from Stock as S left join IconImage I on S.iconImageIdx = I.iconImageIdx " +
                "left join StockTransaction ST on S.stockIdx = ST.stockIdx " +
                "inner join (select S.name, max(ST.transactionTime) as maxtime " +
                "from Stock as S join StockTransaction ST on S.stockIdx = ST.stockIdx group by S.name) dt " +
                "on S.name = dt.name and ST.transactionTime = dt.maxtime " +
                "left join (select * from (select stockIdx, price " +
                "from StockTransaction where date(transactionTime) = curdate() - 1 " +
                "order by stockTransactionIdx desc limit 18446744073709551615) res group by stockIdx) as F " +
                "on ST.stockIdx = F.stockIdx where ST.stockIdx = ?";

        int getStockInfoParams = stockIdx;
        try {
            return this.jdbcTemplate.queryForObject(getStockInfoQuery,
                    (rs, rowNum) -> new GetStockInfoRes(
                            rs.getLong("stockIdx"),
                            rs.getLong("stockTransactionIdx"),
                            rs.getString("name"),
                            rs.getInt("price"),
                            rs.getLong("issuedShares"),
                            rs.getInt("tradingVolume"),
                            //calculateRateOfChange(stockIdx),
                            rs.getFloat("rateOfChange"),
                            "어제",
                            rs.getString("iconImage"),
                            rs.getString("transactionTime")),
                    getStockInfoParams);
        } catch (EmptyResultDataAccessException e) { // 쿼리문에 해당하는 결과가 없을 때
            return null;
        }
    }

    //오늘 종가
    public int getClosingPriceToday(int stockIdx) {
        try {
            String getClosingPriceTodayQuery = "select price from StockTransaction where stockIdx = ? order by stockTransactionIdx DESC limit 1";
            int getClosingPriceTodayParam = stockIdx;
            return this.jdbcTemplate.queryForObject(getClosingPriceTodayQuery, int.class, getClosingPriceTodayParam);
        }catch (NullPointerException e) { // 쿼리문에 해당하는 결과가 없을 때
            return 0;
        }
    }

    //어제 종가
    public int getClosingPriceYesterday(int stockIdx) {
        try {
            String getClosingPriceYesterdayQuery = "select price from StockTransaction where date(transactionTime) = curdate() - 1 " +
                    "and stockIdx = ? order by stockTransactionIdx DESC limit 1";
            int getClosingPriceYesterdayParam = stockIdx;
            return this.jdbcTemplate.queryForObject(getClosingPriceYesterdayQuery, int.class, getClosingPriceYesterdayParam);
        }catch (NullPointerException e) { // 쿼리문에 해당하는 결과가 없을 때
            return 0;
        }
    }

    //등락폭 계산
    public String calculateRateOfChange(int stockIdx){
        double ClosingPriceToday = getClosingPriceToday(stockIdx);
        double ClosingPriceYesterday = getClosingPriceYesterday(stockIdx);
        double result = (ClosingPriceToday - ClosingPriceYesterday) / ClosingPriceYesterday * 100;
        //System.out.println(ClosingPriceToday);
        //System.out.println(ClosingPriceYesterday);
        //System.out.println(result);
        String RateOfChange = String.format("%.1f", result);

        if(result > 0){
            RateOfChange = "+" + RateOfChange;
        }
        //System.out.println(RateOfChange);
        return RateOfChange;
    }


    //특정 주식 누적 가격 조회 (그래프용)
    public List<GetStockPricesRes> getStockPrices(int stockIdx) {
        String getStockPricesQuery = "select price, transactionTime from StockTransaction where stockIdx = ?";
        int getStockPricesParam = stockIdx;
        return this.jdbcTemplate.query(getStockPricesQuery,
                (rs, rowNum) -> new GetStockPricesRes(
                        rs.getInt("price"),
                        rs.getString("transactionTime")
                ), getStockPricesParam);
    }

    // 나의 자산 조회 API
    public List<StockBox> getStockBox(int userIdx) {
        String getStockBoxQuery =
                "SELECT S.name AS assetName, ST.price, II.iconImage," +
                        "SUT.saleCheck, SUT.updatedAt" +
                        "FROM StockUserTransaction AS SUT" +
                        "INNER JOIN StockTransaction AS ST ON ST.stockTransactionIdx = SUT.stockTransactionIdx" +
                        "INNER JOIN Stock AS S ON S.stockIdx = ST.stockIdx" +
                        "INNER JOIN IconImage AS II ON II.iconImageIdx = S.iconImageIdx" +
                        "WHERE userIdx = ?";

    }
}
