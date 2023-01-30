package site.lghtsg.api.resells.dataUploader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import site.lghtsg.api.resells.dataUploader.model.Resell;
import site.lghtsg.api.resells.dataUploader.model.ResellTransaction;

import javax.sql.DataSource;

@Repository
public class ResellUploadDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void uploadResell(Resell resell) {
        String createResellQuery = "insert into Resell (name, releasedPrice, releasedDate, color, brand, productNum, image1, image2, image3, iconImageIdx) VALUES (?,?,?,?,?,?,?,?,?,?)";// 실행될 동적 쿼리문

        Object[] createResellParams = new Object[]{
                resell.getName(),
                resell.getReleasedPrice(),
                resell.getReleasedDate(),
                resell.getColor(),
                resell.getBrand(),
                resell.getProductNum(),
                resell.getImage1(),
                resell.getImage2(),
                resell.getImage3(),
                resell.getIconImageIdx()
        };

        this.jdbcTemplate.update(createResellQuery, createResellParams);
    }

    public void uploadResellTransaction(ResellTransaction resellTransaction) {
        String createResellQuery = "insert into ResellTransaction (resellIdx, price, transactionTime) VALUES(?,?,?)"; // 실행될 동적 쿼리문

        Object[] createResellTransactionParams = new Object[]{
                resellTransaction.getResellIdx(),
                resellTransaction.getPrice(),
                resellTransaction.getTransactionTime()
        };


        this.jdbcTemplate.update(createResellQuery, createResellTransactionParams);
    }
}
