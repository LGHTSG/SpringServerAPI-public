package site.lghtsg.api.resells.dataUploader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import site.lghtsg.api.resells.dataUploader.model.Resell;
import site.lghtsg.api.resells.dataUploader.model.ResellTransaction;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class ResellUploadDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void uploadResell(Resell resell) {
        String createResellQuery = "insert into Resell (name, releasedPrice, releasedDate, color, brand, productNum, image1, image2, image3, iconImageIdx, productCode) VALUES (?,?,?,?,?,?,?,?,?,?,?)";// 실행될 동적 쿼리문
        Object[] createResellParams = new Object[]{resell.getName(), resell.getReleasedPrice(), resell.getReleasedDate(), resell.getColor(), resell.getBrand(), resell.getProductNum(), resell.getImage1(), resell.getImage2(), resell.getImage3(), resell.getIconImageIdx(), resell.getProductCode()};
        this.jdbcTemplate.update(createResellQuery, createResellParams);
    }

    public void uploadResellTransaction(ResellTransaction resellTransaction) {
        String createResellQuery = "insert into ResellTransaction (resellIdx, price, transactionTime) VALUES(?,?,?)"; // 실행될 동적 쿼리문
        Object[] createResellTransactionParams = new Object[]{resellTransaction.getResellIdx(), resellTransaction.getPrice(), resellTransaction.getTransactionTime()};
        this.jdbcTemplate.update(createResellQuery, createResellTransactionParams);
    }

    public List<Integer[]> getResellIdxAndProductCode() {
        String getProductCodeQuery = "select resellIdx, productCode from Resell";
        return this.jdbcTemplate.query(getProductCodeQuery, (rs, rowNum) -> (new Integer[]{rs.getInt("resellIdx"), rs.getInt("productCode")}));
    }

    public void updateResellTodayTransByHour(int resellIdx, int price, String transactionTime) {
        String createResellTransactionQuery = "insert into ResellTodayTrans (resellIdx, price) VALUES (?,?)";
        this.jdbcTemplate.update(createResellTransactionQuery, resellIdx, price);
    }

    public void updateResellTransactionByHour(int resellIdx, int price, String transactionTime) {
        String updateResellTransactionQuery = "update ResellTransaction SET price = ? where resellIdx = ? and transactionTime = ?";
        this.jdbcTemplate.update(updateResellTransactionQuery, price, resellIdx, transactionTime);
    }

    public List<Integer> getTransactionToday(int resellIdx, String transactionTime) {
        String getProductCodeQuery = "select price from ResellTodayTrans where resellIdx = ? and transactionTime = ? ";
        return this.jdbcTemplate.query(getProductCodeQuery, (rs, rowNum) -> rs.getInt("price"), resellIdx, transactionTime);
    }

    public void truncateResellTodayTrans(int resellIdx, String transactionTime) {
        String truncateResellTodayTransQuery = "delete from ResellTodayTrans where resellIdx = ? and transactionTime = ? ";
        this.jdbcTemplate.update(truncateResellTodayTransQuery, resellIdx, transactionTime);
    }

    public void startTodayTransaction(int resellIdx, int price, String transactionTime) {
        String createResellTodayTransQuery = "insert into ResellTransaction (resellIdx, price, transactionTime) VALUES (?,?,?)";
        this.jdbcTemplate.update(createResellTodayTransQuery, price, resellIdx, transactionTime);
    }


}
