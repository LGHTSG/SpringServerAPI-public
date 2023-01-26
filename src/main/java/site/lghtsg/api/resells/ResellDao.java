package site.lghtsg.api.resells;

import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import site.lghtsg.api.common.model.Box;
import site.lghtsg.api.realestates.model.RealEstateBox;
import site.lghtsg.api.resells.model.GetResellInfoRes;
import site.lghtsg.api.resells.model.GetResellTransactionRes;
import site.lghtsg.api.resells.model.GetResellBoxRes;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;

@Repository
public class ResellDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<GetResellBoxRes> getResellBoxes() {
        String getResellBoxesQuery = "select rs.resellIdx, rs.name, rst.price, rst2.price, ii.iconImage\n" +
                "from Resell as rs,\n" +
                "     ResellTransaction as rst,\n" +
                "     ResellTransaction as rst2,\n" +
                "     IconImage as ii\n" +
                "where rst.resellTransactionIdx = rs.lastTransactionIdx\n" +
                "  and rst2.resellTransactionIdx = rs.s2LastTransactionIdx\n" +
                "  and rs.iconImageIdx = ii.iconImageIdx";


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
                "     ResellTransaction as rst,\n" +
                "     ResellTransaction as rst2,\n" +
                "     IconImage as ii\n" +
                "where rst.resellTransactionIdx = rs.lastTransactionIdx\n" +
                "  and rst2.resellTransactionIdx = rs.s2LastTransactionIdx\n" +
                "  and rs.iconImageIdx = ii.iconImageIdx\n" +
                "  and rs.resellIdx = ?";
        long getResellParams = resellIdx;

        return this.jdbcTemplate.queryForObject(getResellQuery, resellInfoResRowMapper(), getResellParams);
    }

    public GetResellBoxRes getResellBox(long resellIdx) {
        String getResellQuery = "select * from Resell where resellIdx = ?";
        long getResellParams = resellIdx;

        return this.jdbcTemplate.queryForObject(getResellQuery, resellBoxResRowMapper(), getResellParams);
    }

    public List<GetResellTransactionRes> getResellTransaction(long resellIdx) {
        String getResellTransactionQuery = "select * from ResellTransaction where resellIdx = ?";
        long getResellTransactionParams = resellIdx;
        return this.jdbcTemplate.query(getResellTransactionQuery, (rs, rowNum) -> new GetResellTransactionRes(rs.getInt("resellIdx"), rs.getInt("price"), rs.getString("transactionTime")), getResellTransactionParams);
    }

    public List<Integer> getResellTransactionForPriceAndRateOfChange(long resellIdx) {
        String getResellTransactionHistoryQuery = "select price from ResellTransaction where resellIdx = ?";
        long getResellTransactionHistory = resellIdx;
        return this.jdbcTemplate.query(getResellTransactionHistoryQuery, (rs, rowNum) -> rs.getInt("price"), getResellTransactionHistory);
    }

    private RowMapper<GetResellBoxRes> resellBoxResRowMapper() {
        return new RowMapper<GetResellBoxRes>() {
            @Override
            public GetResellBoxRes mapRow(ResultSet rs, int rowNum) throws SQLException {
                GetResellBoxRes getResellBoxRes = new GetResellBoxRes();
                getResellBoxRes.setIdx(rs.getLong("resellIdx"));
                getResellBoxRes.setName(rs.getString("name"));
                getResellBoxRes.setRateCalDateDiff("최근 거래가 기준");
                getResellBoxRes.setIconImage(rs.getString("iconImage"));
                getResellBoxRes.setPrice(rs.getLong("rst.price"));
                getResellBoxRes.setLastPrice(rs.getLong("rst2.price"));
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