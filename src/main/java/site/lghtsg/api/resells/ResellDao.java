package site.lghtsg.api.resells;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import site.lghtsg.api.resells.model.GetResellRes;
import site.lghtsg.api.resells.model.GetResellTransactionRes;

import javax.sql.DataSource;
import java.util.*;

@Repository
public class ResellDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<GetResellRes> getResells(String order){
        String getResellsQuery = "select * from Resell order by resellIdx ";
        if(order.equals("ascending")){
            getResellsQuery += "ASC";
        }

        if (order.equals("descending")){
            getResellsQuery += "DESC";
        }

        return this.jdbcTemplate.query(getResellsQuery,
                (rs, row) -> new GetResellRes(
                        rs.getInt("resellIdx"),
                        rs.getString("name"),
                        calculateChangeOfRate(rs.getInt("resellIdx")).get(0),
                        rs.getInt("releasedPrice"),
                        rs.getString("releasedDate"),
                        rs.getString("color"),
                        rs.getString("brand"),
                        rs.getString("productNum"),
                        calculateChangeOfRate(rs.getInt("resellIdx")).get(1),
                        "최근 거래가 기준",
                        rs.getString("image1"),
                        rs.getString("image2"),
                        rs.getString("image3")
                )
        );
    }

    public List<GetResellRes> getResellsByRate(){
        String getResellsQuery = "select * from Resell";

        return this.jdbcTemplate.query(getResellsQuery,
                (rs, row) -> new GetResellRes(
                        rs.getInt("resellIdx"),
                        rs.getString("name"),
                        calculateChangeOfRate(rs.getInt("resellIdx")).get(0),
                        rs.getInt("releasedPrice"),
                        rs.getString("releasedDate"),
                        rs.getString("color"),
                        rs.getString("brand"),
                        rs.getString("productNum"),
                        calculateChangeOfRate(rs.getInt("resellIdx")).get(1),
                        "최근 거래가 기준",
                        rs.getString("image1"),
                        rs.getString("image2"),
                        rs.getString("image3")
                )
        );
    }

    public GetResellRes getResell(int resellIdx){
        String getResellQuery = "select * from Resell where resellIdx = ?";
        int getResellParams = resellIdx;

        return this.jdbcTemplate.queryForObject(getResellQuery,
                (rs, rowNum) -> new GetResellRes(
                        rs.getInt("resellIdx"),
                        rs.getString("name"),
                        calculateChangeOfRate(rs.getInt("resellIdx")).get(0),
                        rs.getInt("releasedPrice"),
                        rs.getString("releasedDate"),
                        rs.getString("color"),
                        rs.getString("brand"),
                        rs.getString("productNum"),
                        calculateChangeOfRate(rs.getInt("resellIdx")).get(1),
                        "최근 거래가 기준",
                        rs.getString("image1"),
                        rs.getString("image2"),
                        rs.getString("image3")),
                getResellParams);
    }

    public List<GetResellTransactionRes> getResellTransaction(int resellIdx){
        String getResellTransactionQuery = "select * from ResellTransaction where resellIdx = ?";
        int getResellTransactionParams = resellIdx;
        return this.jdbcTemplate.query(getResellTransactionQuery,
                (rs, rowNum) -> new GetResellTransactionRes(
                        rs.getInt("resellIdx"),
                        rs.getInt("price"),
                        rs.getTimestamp("transactionTime")),
                getResellTransactionParams);
    }

    public List<GetResellTransactionRes> getResellTransactionHistory(int resellIdx){
        String getResellTransactionHistoryQuery = "select * from ResellTransaction where resellIdx = ? order by createdAt desc LIMIT 2";
        int getResellTransactionHistory = resellIdx;
        return this.jdbcTemplate.query(getResellTransactionHistoryQuery,
                (rs, rowNum) -> new GetResellTransactionRes(
                        rs.getInt("resellIdx"),
                        rs.getInt("price"),
                        rs.getTimestamp("transactionTime")),
                getResellTransactionHistory);
    }

    public List<String> calculateChangeOfRate(int resellIdx){
        List<GetResellTransactionRes> resellTransactionHistory = getResellTransactionHistory(resellIdx);
        List<String> result = new ArrayList<>();
        int currentPrice = resellTransactionHistory.get(0).getPrice();
        int latestPrice = resellTransactionHistory.get(1).getPrice();

        double changeOfRate = (double) (currentPrice - latestPrice) / latestPrice * 100;
        String changeOfRateS = String.format("%.1f",changeOfRate);

        if(changeOfRate > 0){
            changeOfRateS = "+" + changeOfRateS;
        }
        result.add(String.valueOf(currentPrice));
        result.add(changeOfRateS);
        return result;
    }
}
