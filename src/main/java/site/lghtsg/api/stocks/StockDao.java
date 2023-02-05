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

        String getStockBoxesQuery =
                "select S.stockIdx,\n" +
                "       S.name,\n" +
                "       S.issuedShares,\n" +
                "       ST.tradingVolume,\n" +
                "       ST.price,\n" +
                "       ST2.price           as closingPrice,\n" +
                "       ST.transactionTime,\n" +
                "       II.iconImage\n" +
                "from Stock as S\n" +
                "         join StockTodayTrans ST on ST.stockTransactionIdx = S.lastTransactionIdx\n" +
                "         join StockTransaction ST2 on ST2.stockTransactionIdx = S.s2LastTransactionIdx\n" +
                "         join IconImage as II on S.iconImageIdx = II.iconImageIdx;";

        return this.jdbcTemplate.query(getStockBoxesQuery, stockBoxRowMapper());

    }

    //특정 주식 정보 조회
    public StockBox getStockInfo(long stockIdx) {

        String getStockInfoQuery =
                "select S.stockIdx,\n" +
                "       S.name,\n" +
                "       S.issuedShares,\n" +
                "       ST.tradingVolume,\n" +
                "       ST.price,\n" +
                "       ST2.price           as closingPrice,\n" +
                "       ST.transactionTime,\n" +
                "       II.iconImage\n" +
                "from Stock as S\n" +
                "         join StockTodayTrans ST on ST.stockTransactionIdx = S.lastTransactionIdx\n" +
                "         join StockTransaction ST2 on ST2.stockTransactionIdx = S.s2LastTransactionIdx\n" +
                "         join IconImage as II on S.iconImageIdx = II.iconImageIdx\n" +
                "where S.stockIdx = ?;";

        try {
            return this.jdbcTemplate.queryForObject(getStockInfoQuery, stockBoxRowMapper(), stockIdx);
        } catch (EmptyResultDataAccessException e) { // 쿼리문에 해당하는 결과가 없을 때
            return null;
        }
    }

    //특정 주식 누적 가격 조회 (그래프용)
    public List<StockTransactionData> getStockPrices(long stockIdx) {
        String getStockPricesQuery = "select price, transactionTime from StockTransaction where stockIdx = ?";
        return this.jdbcTemplate.query(getStockPricesQuery, transactionRowMapper(), stockIdx);
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
                return stockBox;
            }
        };
    }

    private RowMapper<StockTransactionData> transactionRowMapper() {
        return new RowMapper<StockTransactionData>() {
            @Override
            public StockTransactionData mapRow(ResultSet rs, int rowNum) throws SQLException {
                StockTransactionData stockTransactionData = new StockTransactionData();
                stockTransactionData.setPrice(rs.getLong("price"));
                stockTransactionData.setDatetime(rs.getString("transactionTime"));
                return stockTransactionData;
            }
        };
    }
}
