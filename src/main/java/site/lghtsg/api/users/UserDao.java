package site.lghtsg.api.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import site.lghtsg.api.users.model.*;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


@Repository
public class UserDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {this.jdbcTemplate = new JdbcTemplate(dataSource);}

    // 회원가입
    public int createUser(PostUserReq postUserReq) {
        String createUserQuery = "insert into User" +
                "(userName, email, emailCheck, password, profileImg) " +
                "values (?,?,?,?,?); ";
        String createSales = "INSERT INTO Sales VALUES ()";
        Object[] createUserParams = new Object[]{postUserReq.getUserName(), postUserReq.getEmail(),
                postUserReq.getEmailCheck(), postUserReq.getPassword(), postUserReq.getProfileImg()};
        this.jdbcTemplate.update(createSales);
        return this.jdbcTemplate.update(createUserQuery, createUserParams);
    }

    // 이메일 확인
    public int checkEmail(String email) {
        String checkEmailQuery = "select exists(select email from User where email = ?)";
        String checkEmailParams = email; // 해당(확인할) 이메일 값
        return this.jdbcTemplate.queryForObject(checkEmailQuery,
                int.class,
                checkEmailParams);
    }

    // 로그인 : email에 해당되는 user의 암호와 탈퇴 여부 체크
    public User getPassword(PostLoginReq postLoginReq) {
        String getPasswordQuery = "select userIdx, password, email, withdrawCheck" +
                " from User where email = ? AND withdrawCheck = 0";
        // withdraw 가 1인 회원은 회원 탈퇴를 진행한 회원입니다.
        String getPasswordParams = postLoginReq.getEmail();

        return this.jdbcTemplate.queryForObject(getPasswordQuery,
                (rs, rowNum) -> new User(
                        rs.getInt("userIdx"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getInt("withdrawCheck")
                ),
                getPasswordParams
        );
    }

    // 암호화 된 비밀번호 확인
    public String getOnlyPwd(int userIdx) {
        String getOnlyPwdQuery = "select password from User where userIdx = ?";
        int getOnlyPwdParams = userIdx;

        return this.jdbcTemplate.queryForObject(getOnlyPwdQuery,
                String.class,
                getOnlyPwdParams);
    }

    // 회원정보 수정 (비밀번호)
    public int modifyUserPassword(PatchUserPasswordReq patchUserPasswordReq) {
        String modifyUserPasswordQuery =
                "update User set password = ?, updatedAt = default where userIdx = ?";
        Object[] modifyUserPasswordParams =
                new Object[]{patchUserPasswordReq.getPassword(), patchUserPasswordReq.getUserIdx()};

        return this.jdbcTemplate.update(modifyUserPasswordQuery, modifyUserPasswordParams);
    }

    // 회원정보 수정 (비밀번호 - 로그인 안됐을 때)
    public int modifyUserPasswordNotLogin(PatchUserPasswordNotLoginReq patchUserPasswordReq) {
        String modifyUserPasswordQuery =
                "update User set password = ?, updatedAt = default where email = ?";
        Object[] modifyUserPasswordParams =
                new Object[]{patchUserPasswordReq.getPassword(), patchUserPasswordReq.getEmail()};

        return this.jdbcTemplate.update(modifyUserPasswordQuery, modifyUserPasswordParams);
    }

    // 회원정보 수정 (프로필 사진)
    public int modifyUserProfileImg(PatchUserProfileImgReq patchUserProfileImgReq) {
        String modifyUserProfileImgQuery =
                "update User set profileImg = ?, updatedAt = default where userIdx = ?";
        Object[] modifyUserProfileImgParams =
                new Object[]{patchUserProfileImgReq.getProfileImg(), patchUserProfileImgReq.getUserIdx()};

        return this.jdbcTemplate.update(modifyUserProfileImgQuery, modifyUserProfileImgParams);
    }

    // 회원 탈퇴
    public int withdrawUser(PatchUserDeleteReq patchUserDeleteReq) {
        String withdrawUserQuery = "update User set withdrawCheck = 1, updatedAt = default where userIdx =?";
        Object[] withdrawUserParams = new Object[]{patchUserDeleteReq.getUserIdx()};

        return this.jdbcTemplate.update(withdrawUserQuery, withdrawUserParams);
    }

    // 주식 자산 조회
    public List<GetMyAssetRes> getStockAsset(int userIdx) {
        String getStockAssetQuery =
                "select ST.stockTransactionIdx as idx,\n" +
                        "       S.name,\n" +
                        "       ST.price,\n" +
                        "       ST2.price           as s2Price,\n" +
                        "       ST.transactionTime,\n" +
                        "       ST2.transactionTime as s2TransactionTime,\n" +
                        "       SUT.updatedAt,\n" +
                        "       SUT.Check,\n" +
                        "       II.iconImage\n" +
                        "from Stock as S\n" +
                        "         join StockTodayTrans ST on ST.stockTransactionIdx = S.lastTransactionIdx\n" +
                        "         join StockTransaction ST2 on ST2.stockTransactionIdx = S.s2LastTransactionIdx\n" +
                        "         join IconImage as II on S.iconImageIdx = II.iconImageIdx\n" +
                        "         join StockUserTransaction SUT on S.stockIdx = (select st.stockIdx\n" +
                        "                                                           from StockTransaction as st\n" +
                        "                                                           where st.stockTransactionIdx = SUT.stockTransactionIdx)\n" +
                        "where SUT.userIdx = ?\n" +
                        "  and SUT.transactionStatus = 1;";
        int getStockAssetParams = userIdx;

        return this.jdbcTemplate.query(getStockAssetQuery, assetRowMapper(), getStockAssetParams);
    }

    // 리셀 자산 조회
    public List<GetMyAssetRes> getResellAsset(int userIdx) {
        String getResellAssetQuery =
                "select RS.resellIdx as idx,\n" +
                        "       RS.name,\n" +
                        "       RST.price,\n" +
                        "       RST2.price           as s2Price,\n" +
                        "       RST.transactionTime,\n" +
                        "       RST2.transactionTime as s2TransactionTime,\n" +
                        "       RUT.updatedAt,\n" +
                        "       RUT.sellCheck,\n" +
                        "       II.iconImage\n" +
                        "from Resell as RS\n" +
                        "         join ResellTodayTrans as RST on RST.resellTransactionIdx = RS.lastTransactionIdx\n" +
                        "         join ResellTransaction as RST2 on RST2.resellTransactionIdx = RS.s2LastTransactionIdx\n" +
                        "         join IconImage as II on RS.iconImageIdx = II.iconImageIdx\n" +
                        "         join ResellUserTransaction RUT on RS.resellIdx = (select rst.resellIdx\n" +
                        "                                                           from ResellTransaction as rst\n" +
                        "                                                           where rst.resellTransactionIdx = RUT.resellTransactionIdx)\n" +
                        "where RUT.userIdx = ?\n" +
                        "  and RUT.transactionStatus = 1;";

        int getResellBoxParams = userIdx;

        return this.jdbcTemplate.query(getResellAssetQuery, assetRowMapper(), getResellBoxParams);
    }

    // 부동산 자산 조회
    public List<GetMyAssetRes> getRealEstateAsset(int userIdx) {
        String getRealEstateAssetQuery =
                "select RET.realEstateTransactionIdx as idx,\n" +
                        "       RE.name,\n" +
                        "       RET.price,\n" +
                        "       RET2.price           as s2Price,\n" +
                        "       RET.transactionTime,\n" +
                        "       RET2.transactionTime as s2TransactionTime,\n" +
                        "       REUT.updatedAt,\n" +
                        "       REUT.sellCheck,\n" +
                        "       II.iconImage\n" +
                        "from RealEstate as RE\n" +
                        "         join RealEstateTodayTrans as RET on RET.realEstateTransactionIdx = RE.lastTransactionIdx\n" +
                        "         join RealEstateTransaction as RET2 on RET2.realEstateTransactionIdx = RE.s2LastTransactionIdx\n" +
                        "         join IconImage as II on RE.iconImageIdx = II.iconImageIdx\n" +
                        "         join RealEstateUserTransaction REUT on RE.realEstateIdx = (select ret.realEstateIdx\n" +
                        "                                                                    from RealEstateTransaction as ret\n" +
                        "                                                                    where ret.realEstateTransactionIdx = REUT.realEstateTransactionIdx)\n" +
                        "where REUT.userIdx = ?\n" +
                        "  and REUT.transactionStatus = 1;";

        int getRealEstateParams = userIdx;

        return this.jdbcTemplate.query(getRealEstateAssetQuery, assetRowMapper(), getRealEstateParams);
    }

    // 자산 구매
    public int buyMyAsset(int userIdx, PostMyAssetReq postMyAssetReq) {
        String postMyAssetQuery = "";
        switch (postMyAssetReq.getCategory()){
            case "stock":
                postMyAssetQuery = "insert into StockUserTransaction(userIdx, stockIdx, price, transactionTime) values (?,?,?,?)";
                break;
            case "resell":
                postMyAssetQuery = "insert into ResellUserTransaction(userIdx, resellIdx, price, transactionTime) values (?,?,?,?)";
                break;
            case "realestate":
                postMyAssetQuery = "insert into RealEstateUserTransaction(userIdx, realEsateIdx, price, transactionTime) values (?,?,?,?)";
                break;
        }

        Object[] postMyAssetParams = new Object[]{userIdx, postMyAssetReq.getAssetIdx(), postMyAssetReq.getPrice(), postMyAssetReq.getTransactionTime()};
        return this.jdbcTemplate.update(postMyAssetQuery, postMyAssetParams);
    }

    // 자산 판매
    public int sellMyAsset(int userIdx, PostMyAssetReq postMyAssetReq) {
        String sellMyAssetQuery = "";
        // 판매
        // 이 곳에서는 복수의 s가 붙지 않습니다. (GET에서 category를 단수형으로 보내줌)
        switch (postMyAssetReq.getCategory()){
            case "stock":
                sellMyAssetQuery = "INSERT INTO StockUserTransaction(userIdx, stockIdx, price, transactionTime, sellCheck) VALUES (?,?,?,1);";
                break;
            case "resell":
                sellMyAssetQuery = "INSERT INTO ResellUserTransaction(userIdx, resellIdx, price, transactionTime, sellCheck) VALUES (?,?,?,1);";
                break;
            case "realestate":
                sellMyAssetQuery = "INSERT INTO RealEstateUserTransaction(userIdx, realEstateIdx, price, transactionTime, sellCheck) VALUES (?,?,?,1);";
                break;
        }

        Object[] sellMyAssetParams = new Object[]{userIdx, postMyAssetReq.getAssetIdx(), postMyAssetReq.getPrice(), postMyAssetReq.getTransactionTime()};
        return this.jdbcTemplate.update(sellMyAssetQuery, sellMyAssetParams);
    }

    public Asset getPreviousTransaction(int userIdx, PostMyAssetReq postMyAssetReq){
        String getPreviousTransactionQuery = "";

        switch (postMyAssetReq.getCategory()) {
            case "stock":
                getPreviousTransactionQuery = "select SUT.stockIdx as idx, SUT.sellCheck, SUT.transactionTime, SUT.price\n" +
                        "from StockUserTransaction as SUT\n" +
                        "where SUT.userIdx = ? and SUT.transactionStatus = 1 and SUT.stockIdx = ?;";
                break;
            case "resell":
                getPreviousTransactionQuery =
                        "select RUT.resellIdx as idx, RUT.sellCheck, RUT.transactionTime, RUT.price\n" +
                        "from ResellUserTransaction as RUT\n" +
                        "where RUT.userIdx = ? and RUT.transactionStatus = 1 and SUT.resellIdx = ?;";
                break;
            case "realestate":
                getPreviousTransactionQuery =
                        "select REUT.realEstateIdx as idx, REUT.sellCheck, REUT.transactionTime, REUT.price\n" +
                        "from RealEstateUserTransaction as REUT\n" +
                        "where REUT.userIdx = ? and REUT.transactionStatus = 1 and REUT.realestateIdx = ?;";
                break;
            default:
                break;
        }

        Object [] getPreviousTransactionParam = new Object[] {userIdx, postMyAssetReq.getAssetIdx()};
        return this.jdbcTemplate.queryForObject(getPreviousTransactionQuery, (rs, rowNum) -> new Asset(
                        rs.getInt("idx"),
                        rs.getString("transactionTime"),
                        rs.getLong("price"),
                        rs.getString("category"),
                        rs.getInt("sellCheck")
        ), getPreviousTransactionParam);
    }

    // 리스트 노출 상태 변경
    public int changeMyAssetList(int userIdx, PostMyAssetReq postMyAssetReq) {
        String changeMyAssetListQuery = "";
        switch (postMyAssetReq.getCategory()) {
            case "stock":
                changeMyAssetListQuery = "UPDATE StockUserTransaction SET transactionStatus=0 where useridx=? and stockidx = ? and transactionstatus=1";
            break;
            case "resell":
                changeMyAssetListQuery = "UPDATE ResellUserTransaction SET transactionStatus=0 WHERE userIdx=? AND resellIdx = ? AND transactionStatus=1";
                break;
            case "realestate":
                changeMyAssetListQuery = "UPDATE RealEstatelUserTransaction SET transactionStatus=0 WHERE userIdx=? AND realEstateIdx = ? AND transactionStatus=1";
                break;
            default:
                break;
        }
        Object[] sellMyAssetParams = new Object[]{userIdx, postMyAssetReq.getAssetIdx()};
        return this.jdbcTemplate.update(changeMyAssetListQuery, sellMyAssetParams);
    }

    public int updateTableSales(int userIdx, double sales) {
        String updateTableSalesQuery =
                "UPDATE Sales SET totalSale = totalSale + ?, numOfTransaction = numOfTransaction + 1 " +
                        "WHERE userIdx = ?";
        Object[] updateTableSalesParams = new Object[]{sales, userIdx};
        return this.jdbcTemplate.update(updateTableSalesQuery,updateTableSalesParams);
    }

    // 단일 자산 화면 개인 거래 내역 조회
    public List<GetUserTransactionHistoryRes> getStockTransactionHistory(long stockIdx, int userIdx){
        String getTransactionHistoryQuery =
                "select createdAt as transactionTime,\n" +
                "       price,\n" +
                "       sellCheck\n" +
                "from StockUserTransaction as SUT\n" +
                "where SUT.stockIdx = ?\n" +
                "and SUT.userIdx = ?;";
        Object[] getTransactionHistoryParams = new Object[] {stockIdx, userIdx};
        return this.jdbcTemplate.query(getTransactionHistoryQuery, transactionHistoryRowMapper(), getTransactionHistoryParams);
    }

    public List<GetUserTransactionHistoryRes> getRealEstateTransactionHistory(long realestateIdx, int userIdx){
        String getTransactionHistoryQuery =
                "select createdAt as transactionTime,\n" +
                "       price,\n" +
                "       sellCheck\n" +
                "from RealEstateUserTransaction as REUT\n" +
                "where REUT.realEstateIdx = ?\n" +
                "and REUT.userIdx = ?;";
        Object[] getTransactionHistoryParams = new Object[] {realestateIdx, userIdx};
        return this.jdbcTemplate.query(getTransactionHistoryQuery, transactionHistoryRowMapper(), getTransactionHistoryParams);
    }

    public List<GetUserTransactionHistoryRes> getResellTransactionHistory(long resellIdx, int userIdx){
        String getTransactionHistoryQuery =
                "select createdAt as transactionTime,\n" +
                "       price,\n" +
                "       sellCheck\n" +
                "from ResellUserTransaction as RUT\n" +
                "where RUT.resellIdx = ?\n" +
                "and RUT.userIdx = ?;";
        Object[] getTransactionHistoryParams = new Object[] {resellIdx, userIdx};
        return this.jdbcTemplate.query(getTransactionHistoryQuery, transactionHistoryRowMapper(), getTransactionHistoryParams);
    }

    private RowMapper<GetMyAssetRes> assetRowMapper(){
        return new RowMapper<GetMyAssetRes>() {
            @Override
            public GetMyAssetRes mapRow(ResultSet rs, int rowNum) throws SQLException {
                GetMyAssetRes getMyAssetRes = new GetMyAssetRes();
                getMyAssetRes.setTransactionIdx(rs.getInt("idx"));
                getMyAssetRes.setAssetName(rs.getString("name"));
                getMyAssetRes.setPrice(rs.getLong("price"));
                getMyAssetRes.setS2Price(rs.getLong("s2Price"));
                getMyAssetRes.setIconImage(rs.getString("iconImage"));
                getMyAssetRes.setSellCheck(rs.getInt("sellCheck"));
                getMyAssetRes.setUpdatedAt(rs.getString("updatedAt"));
                getMyAssetRes.setTransactionTime(rs.getString("transactionTime"));
                getMyAssetRes.setS2TransactionTime(rs.getString("s2TransactionTime"));
                return getMyAssetRes;
            }
        };
    }


    private RowMapper<GetUserTransactionHistoryRes> transactionHistoryRowMapper(){
        return new RowMapper<GetUserTransactionHistoryRes>() {
            @Override
            public GetUserTransactionHistoryRes mapRow(ResultSet rs, int rowNum) throws SQLException {
                GetUserTransactionHistoryRes getUserTransactionHistoryRes = new GetUserTransactionHistoryRes();
                getUserTransactionHistoryRes.setPrice(rs.getLong("price"));
                getUserTransactionHistoryRes.setTransactionTime(rs.getString("transactionTime"));
                getUserTransactionHistoryRes.setSellCheck(rs.getInt("sellCheck"));
                return getUserTransactionHistoryRes;
            }
        };
    }

}