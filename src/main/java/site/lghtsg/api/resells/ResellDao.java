package site.lghtsg.api.resells;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import site.lghtsg.api.resells.model.GetResellInfoRes;
import site.lghtsg.api.resells.model.GetResellTransactionRes;
import site.lghtsg.api.resells.model.GetResellBoxRes;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class ResellDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<GetResellBoxRes> getResellBoxes() {
        String getResellBoxesQuery =  "select rs.resellIdx, rs.name, rst.price as price, rst2.price as s2Price, rs.image1,  rst2.transactionTime\n" +
                "from Resell as rs,\n" +
                "     ResellTodayTrans as rst,\n" +
                "     ResellTransaction as rst2\n" +
                "where rst.resellTransactionIdx = rs.lastTransactionIdx\n" + "  and rst2.resellTransactionIdx = rs.s2LastTransactionIdx";


        return this.jdbcTemplate.query(getResellBoxesQuery,resellBoxResRowMapper());
    }

    public GetResellInfoRes getResellInfo(long resellIdx) {
        String getResellQuery = "select rs.resellIdx,\n" +
                "       rs.name,\n" +
                "       rs.releasedPrice,\n" +
                "       rs.releasedDate,\n" +
                "       rs.color,\n" +
                "       rs.brand,\n" +
                "       rs.productNum,\n" +
                "       rs.image1,\n" +
                "       rs.image2,\n" +
                "       rs.image3,\n" +
                "       ii.iconImage,\n" +
                "       rst.price,\n" +
                "       rst2.price\n" +
                "from Resell as rs,\n" +
                "     ResellTodayTrans as rst,\n" +
                "     ResellTransaction as rst2,\n" +
                "     IconImage as ii\n" +
                "where rst.resellTransactionIdx = rs.lastTransactionIdx\n" +
                "  and rst2.resellTransactionIdx = rs.s2LastTransactionIdx\n" +
                "  and rs.iconImageIdx = ii.iconImageIdx\n" +
                "  and rs.resellIdx = ?";

        try {
            return this.jdbcTemplate.queryForObject(getResellQuery, resellInfoResRowMapper(), resellIdx);
        }
        catch (IncorrectResultSizeDataAccessException error) {
            return null;
        }
    }

    public GetResellBoxRes getResellBox(long resellIdx) {
        String getResellQuery = "select * from Resell where resellIdx = ?";

        try {
            return this.jdbcTemplate.queryForObject(getResellQuery, resellBoxResRowMapper(), resellIdx);
        }
        catch (IncorrectResultSizeDataAccessException error) {
            return null;
        }
    }

    public List<GetResellTransactionRes> getResellTransaction(long resellIdx) {
        String getResellTransactionQuery = "select * from ResellTransaction where resellIdx = ?";
        return this.jdbcTemplate.query(getResellTransactionQuery, (rs, rowNum) -> new GetResellTransactionRes(rs.getInt("price"), rs.getString("transactionTime")), resellIdx);
    }

    public List<Integer> getResellTransactionForPriceAndRateOfChange(long resellIdx) {
        String getResellTransactionHistoryQuery = "select price from ResellTransaction where resellIdx = ?";
        return this.jdbcTemplate.query(getResellTransactionHistoryQuery, (rs, rowNum) -> rs.getInt("price"), resellIdx);
    }

    private RowMapper<GetResellBoxRes> resellBoxResRowMapper() {
        return new RowMapper<GetResellBoxRes>() {
            @Override
            public GetResellBoxRes mapRow(ResultSet rs, int rowNum) throws SQLException {
                GetResellBoxRes getResellBoxRes = new GetResellBoxRes();
                getResellBoxRes.setIdx(rs.getLong("resellIdx"));
                getResellBoxRes.setName(rs.getString("name"));
                getResellBoxRes.setRateCalDateDiff("최근 거래가 기준");
                getResellBoxRes.setTransactionTime(rs.getString("transactionTime"));
                getResellBoxRes.setImageUrl(rs.getString("image1"));
                getResellBoxRes.setPrice(rs.getLong("price"));
                getResellBoxRes.setLastPrice(rs.getLong("s2Price"));
                return getResellBoxRes;
            }
        };
    }

    private RowMapper<GetResellInfoRes> resellInfoResRowMapper() {
        return new RowMapper<GetResellInfoRes>() {
            @Override
            public GetResellInfoRes mapRow(ResultSet rs, int rowNum) throws SQLException {
                GetResellInfoRes getResellInfoRes = new GetResellInfoRes();
                getResellInfoRes.setResellIdx(rs.getLong("resellIdx"));
                getResellInfoRes.setName(rs.getString("name"));
                getResellInfoRes.setReleasedPrice(rs.getString("releasedPrice"));
                getResellInfoRes.setReleasedDate(rs.getString("releasedDate"));
                getResellInfoRes.setColor(rs.getString("color"));
                getResellInfoRes.setBrand(rs.getString("brand"));
                getResellInfoRes.setProductNum(rs.getString("productNum"));
                getResellInfoRes.setRateCalDateDiff("최근 거래가 기준");
                getResellInfoRes.setImage1(rs.getString("image1"));
                getResellInfoRes.setImage2(rs.getString("image2"));
                getResellInfoRes.setImage3(rs.getString("image3"));
                getResellInfoRes.setIconImage(rs.getString("iconImage"));
                getResellInfoRes.setPrice(rs.getLong("rst.price"));
                getResellInfoRes.setLastPrice(rs.getLong("rst2.price"));
                return getResellInfoRes;
            }
        };
    }
}