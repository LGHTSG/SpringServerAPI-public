package site.lghtsg.api.resells.dataUploader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import site.lghtsg.api.realestates.model.RealEstateTransactionData;
import site.lghtsg.api.resells.dataUploader.model.Resell;
import site.lghtsg.api.resells.dataUploader.model.ResellTodayTrans;
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

    public void updateResellTodayTransByHour(List<ResellTodayTrans> data, String transactionTime) {
        String createResellTransactionQuery = createTableRowInitQuery(data, transactionTime);
        this.jdbcTemplate.update(createResellTransactionQuery);
    }

    static String createTableRowInitQuery(List<ResellTodayTrans> data, String transactionTime) {
        StringBuilder ret = new StringBuilder();

        ret.append("insert into ResellTodayTrans (transactionTime, resellIdx, price) values ");

        for(ResellTodayTrans elem : data){
            ret.append("('" + transactionTime + "', '" + elem.getResellIdx() + "', " + elem.getPrice() + "), ");
        }
        ret.delete(ret.length() - 2, ret.length() - 1);
        ret.append(';');
        System.out.println(ret.toString());

        return ret.toString();
    }

    public void updateResellTransactionByHour(int resellIdx, int price, String transactionTime) {
        String updateResellTransactionQuery = "update ResellTransaction SET price = ? where resellIdx = ? and transactionTime = ?";
        this.jdbcTemplate.update(updateResellTransactionQuery, price, resellIdx, transactionTime);
    }

    public List<Integer> getTransactionToday(int resellIdx, String transactionTime) {
        String getProductCodeQuery = "select price from ResellTodayTrans where resellIdx = ? and transactionTime like ?";
        transactionTime = "%" + transactionTime + "%";
        return this.jdbcTemplate.query(getProductCodeQuery, (rs, rowNum) -> rs.getInt("price"), resellIdx, transactionTime);
    }

    public void truncateResellYesterdayTrans() {
        String truncateResellTodayTransQuery = "delete\n" +
                "from ResellTodayTrans\n" +
                "where resellTransactionIdx not in (select R.lastTransactionIdx\n" +
                "                                   from Resell as R\n" +
                "                                   union\n" +
                "                                   select R.s2LastTransactionIdx\n" +
                "                                   from Resell as R);";
        this.jdbcTemplate.update(truncateResellTodayTransQuery);
    }

    public void startTodayTransaction(int resellIdx, int price, String transactionTime) {
        String createResellTodayTransQuery = "insert into ResellTransaction (resellIdx, price, transactionTime) VALUES (?,?,?)";
        this.jdbcTemplate.update(createResellTodayTransQuery, resellIdx, price, transactionTime);
    }

    public int checkDuplicated(int productCode) {
        String checkDuplicatedQuery = "select exists(select productCode from Resell where productCode = ?)";
        return this.jdbcTemplate.queryForObject(checkDuplicatedQuery, int.class, productCode);
    }

    public int getProductCode(int resellIdx) {
        String getProductCodeQuery = "select productCode from Resell where resellIdx = ?";
        return this.jdbcTemplate.queryForObject(getProductCodeQuery, int.class, resellIdx);
    }

    public void updateLastTransactionIdx(){
        String updateLastTransactionIdxQuery = "update Resell as S\n" +
                "set S.lastTransactionIdx = (select STT.resellTransactionIdx\n" +
                "                            from ResellTodayTrans as STT\n"  +
                "                            where S.resellIdx = STT.resellIdx\n"  +
                "                           order by STT.resellTransactionIdx desc\n" +
                "                            limit 1)";
        this.jdbcTemplate.update(updateLastTransactionIdxQuery);
    }

    public void updateS2LastTransactionIdx(){
        String updateS2LastTransactionIdxQuery = "update Resell as S\n" +
                "set S.s2LastTransactionIdx = (select STT.resellTransactionIdx\n" +
                "                            from ResellTodayTrans as STT\n"  +
                "                            where S.resellIdx = STT.resellIdx\n" +
                "                           order by STT.resellTransactionIdx desc\n" +
                "                            limit 1,1)";
        this.jdbcTemplate.update(updateS2LastTransactionIdxQuery);
    }

    public int getLastTransactionPrice(int productCode){
        String getLastTransactionPriceQuery ="select rst.price\n" +
                "        from ResellTodayTrans as rst,\n" +
                "        Resell as rs\n" +
                "        where rst.resellTransactionIdx = rs.lastTransactionIdx\n" +
                "        and rs.productCode = ?";

        return this.jdbcTemplate.queryForObject(getLastTransactionPriceQuery, int.class, productCode);
    }


}