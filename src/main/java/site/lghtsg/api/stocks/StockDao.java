package site.lghtsg.api.stocks;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import site.lghtsg.api.stocks.model.GetStockInfoRes;
import site.lghtsg.api.stocks.model.GetStockPricesRes;
import site.lghtsg.api.stocks.model.GetStockRes;

import javax.sql.DataSource;
import java.util.List;


@Repository
public class StockDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {this.jdbcTemplate = new JdbcTemplate(dataSource);}



    // sort 기준 오름차순, 내림차순 조회
    public List<GetStockRes> getStocks(String sort, String order) {

        String getStocksQuery = "select S.stockIdx, ST.stockTransactionIdx, S.name, ST.price, I.iconImage, ";
        if(sort.equals("trading-volume") && order.equals("ascending")){
            getStocksQuery += "ST.tradingVolume from Stock as S left join IconImage I on S.iconImageIdx = I.iconImageIdx " +
                    "left join StockTransaction ST on S.stockIdx = ST.stockIdx " +
                    "inner join (select S.name, max(ST.transactionTime) as maxtime from Stock as S join StockTransaction ST on S.stockIdx = ST.stockIdx group by S.name) " +
                    "dt on S.name = dt.name and ST.transactionTime = dt.maxtime order by ST.tradingVolume ASC limit 100";
        }
        if(sort.equals("trading-volume") && order.equals("descending")){
            getStocksQuery += "ST.tradingVolume from Stock as S left join IconImage I on S.iconImageIdx = I.iconImageIdx " +
                    "left join StockTransaction ST on S.stockIdx = ST.stockIdx " +
                    "inner join (select S.name, max(ST.transactionTime) as maxtime from Stock as S join StockTransaction ST on S.stockIdx = ST.stockIdx group by S.name) " +
                    "dt on S.name = dt.name and ST.transactionTime = dt.maxtime order by ST.tradingVolume DESC limit 100";
        }


        return this.jdbcTemplate.query(getStocksQuery,
                (rs, rowNum) -> new GetStockRes(
                        rs.getInt("stockIdx"),
                        rs.getInt("stockTransactionIdx"),
                        rs.getString("name"),
                        rs.getInt("price"),
                        //calculateRateOfChange(rs.getInt("stockIdx")),
                        "어제",
                        rs.getString("iconImage")
                ));
    }

    //stockIdx 기준 오름차순, 내림차순 조회
    public List<GetStockRes> getStocksByIdx(String order) {
        String getStocksByIdxQuery = "select S.stockIdx, ST.stockTransactionIdx, S.name, ST.price, I.iconImage " +
                "from Stock as S left join IconImage I on S.iconImageIdx = I.iconImageIdx left join StockTransaction ST on S.stockIdx = ST.stockIdx " +
                "inner join (select S.name, max(ST.transactionTime) as maxtime from Stock as S join StockTransaction ST on S.stockIdx = ST.stockIdx " +
                "group by S.name) dt on S.name = dt.name and ST.transactionTime = dt.maxtime order by S.stockIdx ";

        if(order.equals("ascending")){
            getStocksByIdxQuery += "ASC limit 100";
        }

        if (order.equals("descending")){
            getStocksByIdxQuery += "DESC limit 100";
        }

        return this.jdbcTemplate.query(getStocksByIdxQuery,
                (rs, rowNum) -> new GetStockRes(
                        rs.getInt("stockIdx"),
                        rs.getInt("stockTransactionIdx"),
                        rs.getString("name"),
                        rs.getInt("price"),
                        //calculateRateOfChange(stockIdx),
                        "어제",
                        rs.getString("iconImage")
                ));
    }

    //특정 주식 정보 조회
    public GetStockInfoRes getStockInfo(int stockIdx) {

        String getStockInfoQuery = "select S.stockIdx, ST.stockTransactionIdx, S.name, ST.price, S.issuedShares, ST.tradingVolume, I.iconImage, ST.transactionTime " +
                "from Stock as S left join IconImage I on S.iconImageIdx = I.iconImageIdx " +
                "left join StockTransaction ST on S.stockIdx = ST.stockIdx inner join (select S.name, max(ST.transactionTime) as maxtime " +
                "from Stock as S join StockTransaction ST on S.stockIdx = ST.stockIdx group by S.name) " +
                "dt on S.name = dt.name and ST.transactionTime = dt.maxtime where S.stockIdx = ?";
        int getStockInfoParams = stockIdx;
        try {
            return this.jdbcTemplate.queryForObject(getStockInfoQuery,
                    (rs, rowNum) -> new GetStockInfoRes(
                            rs.getInt("stockIdx"),
                            rs.getInt("stockTransactionIdx"),
                            rs.getString("name"),
                            rs.getInt("price"),
                            rs.getLong("issuedShares"),
                            rs.getInt("tradingVolume"),
                            calculateRateOfChange(stockIdx),
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
            String getClosingPriceTodayQuery = "select price from StockTransaction where stockIdx = ? order by stockTransactionIdx DESC limit 1;";
            int getClosingPriceTodayParam = stockIdx;
            return this.jdbcTemplate.queryForObject(getClosingPriceTodayQuery, int.class, getClosingPriceTodayParam);
        }catch (NullPointerException e) { // 쿼리문에 해당하는 결과가 없을 때
            return 0;
        }
    }

    //어제 종가
    public int getClosingPriceYesterday(int stockIdx) {
        try {
            String getClosingPriceYesterdayQuery = "select price from StockTransaction where date(transactionTime)" +
                    " = curdate() - interval 1 day and stockIdx = ? order by stockTransactionIdx DESC limit 1;";
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
}
