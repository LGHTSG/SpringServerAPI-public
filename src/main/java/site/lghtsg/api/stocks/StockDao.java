package site.lghtsg.api.stocks;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import site.lghtsg.api.stocks.model.*;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;



@Repository
public class StockDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {this.jdbcTemplate = new JdbcTemplate(dataSource);}



    // 전체 리스트 조회
    public List<StockBox> getAllStockBoxes() {

        String getStockBoxesQuery = "select S.stockIdx, S.name, I.iconImage, ST.transactionTime, " +
                "ST.price, S.issuedShares, ST.tradingVolume, F.price as closingPrice\n" +
                "from Stock as S left join IconImage I on S.iconImageIdx = I.iconImageIdx\n" +
                "    left join StockTransaction ST on S.stockIdx = ST.stockIdx\n" +
                "    inner join (select S.name, max(ST.transactionTime) as maxtime\n" +
                "                from Stock as S join StockTransaction ST on S.stockIdx = ST.stockIdx group by S.name) dt\n" +
                "        on S.name = dt.name and ST.transactionTime = dt.maxtime\n" +
                "    left join (select * from (select stockIdx, price\n" +
                "                              from StockTransaction where date(transactionTime) = curdate() - 1\n" +
                "                                                    order by stockTransactionIdx desc limit 18446744073709551615) res group by stockIdx) as F\n" +
                "        on ST.stockIdx = F.stockIdx";

        return this.jdbcTemplate.query(getStockBoxesQuery, stockBoxRowMapper());

    }

    public List<StockBox> getUserStockBoxes(long userIdx){
        String getUserStockBoxesQuery =
                "select S.stockIdx,\n" +
                        "       S.name,\n" +
                        "       ST.price,\n" +
                        "       ST2.price           as closingPrice,\n" +
                        "       ST.transactionTime,\n" +
                        "       SUT.updatedAt,\n" +
                        "       SUT.saleCheck,\n" +
                        "       II.iconImage\n" +
                        "from Stock as S\n" +
                        "         join StockTransaction ST on ST.stockTransactionIdx = S.lastTransactionIdx\n" +
                        "         join StockTransaction ST2 on ST2.stockTransactionIdx = S.s2LastTransactionIdx\n" +
                        "         join IconImage as II on S.iconImageIdx = II.iconImageIdx\n" +
                        "         join StockUserTransaction SUT on S.stockIdx = (select st.stockIdx\n" +
                        "                                                           from StockTransaction as st\n" +
                        "                                                           where st.stockTransactionIdx = SUT.stockTransactionIdx)\n" +
                        "where SUT.userIdx = ?\n" +
                        "  and SUT.transactionStatus = 1;";
        return this.jdbcTemplate.query(getUserStockBoxesQuery, stockBoxRowMapper(), userIdx);
    }

    //특정 주식 정보 조회
    public StockBox getStockInfo(long stockIdx) {

        String getStockInfoQuery = "select S.stockIdx, S.name, I.iconImage, ST.transactionTime, " +
                "ST.price, S.issuedShares, ST.tradingVolume, F.price as closingPrice\n" +
                "from Stock as S left join IconImage I on S.iconImageIdx = I.iconImageIdx\n" +
                "    left join StockTransaction ST on S.stockIdx = ST.stockIdx\n" +
                "    inner join (select S.name, max(ST.transactionTime) as maxtime\n" +
                "                from Stock as S join StockTransaction ST on S.stockIdx = ST.stockIdx group by S.name) dt\n" +
                "        on S.name = dt.name and ST.transactionTime = dt.maxtime\n" +
                "    left join (select * from (select stockIdx, price\n" +
                "                              from StockTransaction where date(transactionTime) = curdate() - 1\n" +
                "                                                    order by stockTransactionIdx desc limit 18446744073709551615) res group by stockIdx) as F\n" +
                "        on ST.stockIdx = F.stockIdx where S.stockIdx = ?";

        long getStockInfoParams = stockIdx;
        try {
            return this.jdbcTemplate.queryForObject(getStockInfoQuery, stockBoxRowMapper(), getStockInfoParams);
        } catch (EmptyResultDataAccessException e) { // 쿼리문에 해당하는 결과가 없을 때
            return null;
        }
    }

    //특정 주식 누적 가격 조회 (그래프용)
    public List<StockTransactionData> getStockPrices(long stockIdx) {
        String getStockPricesQuery = "select price, transactionTime from StockTransaction where stockIdx = ?";
        long getStockPricesParam = stockIdx;
        return this.jdbcTemplate.query(getStockPricesQuery, transactionRowMapper(), getStockPricesParam);
    }

    private RowMapper<StockBox> stockBoxRowMapper(){
        return new RowMapper<StockBox>() {
            @Override
            public StockBox mapRow(ResultSet rs, int rowNum) throws SQLException {
                StockBox stockBox = new StockBox();
                stockBox.setIdx(rs.getLong("stockIdx"));
                stockBox.setName(rs.getString("name"));
                stockBox.setPrice(rs.getInt("price"));
                stockBox.setRateCalDateDiff("어제");
                stockBox.setIssuedShares(rs.getLong("issuedShares"));
                stockBox.setTradingVolume(rs.getInt("tradingVolume"));
                stockBox.setIconImage(rs.getString("iconImage"));
                stockBox.setTransactionTime(rs.getString("transactionTime"));
                stockBox.setClosingPrice(rs.getLong("closingPrice"));
                stockBox.setUpdatedAt(rs.getString("updatedAt"));
                return stockBox;
            }
        };
    }

    private RowMapper<StockTransactionData> transactionRowMapper() {
        return new RowMapper<StockTransactionData>() {
            @Override
            public StockTransactionData mapRow(ResultSet rs, int rowNum) throws SQLException {
                StockTransactionData stockTransactionData = new StockTransactionData(
                        rs.getInt("price"),
                        rs.getString("transactionTime")
                );
                return stockTransactionData;
            }
        };
    }
}
