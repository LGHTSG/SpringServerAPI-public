package site.lghtsg.api.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import site.lghtsg.api.event.model.GetUserInfoForRank;
import site.lghtsg.api.users.model.*;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static site.lghtsg.api.config.Constant.*;


@Repository
public class UserDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {this.jdbcTemplate = new JdbcTemplate(dataSource);}

    // 보유주식 현재가 조회
    public long getStockAssetPrices(int userIdx) {
        String getPricesQuery =
                "select sum(STTT.price)\n" +
                        "from StockUserTransaction SUT\n" +
                        "   join Stock ST on SUT.stockIdx = ST.stockIdx and SUT.userIdx = ? and SUT.transactionStatus = 1 and SUT.sellCheck = 0\n" +
                        "   join StockTodayTrans STTT on ST.lastTransactionIdx = STTT.stockTransactionIdx";

        Long totalPrice = this.jdbcTemplate.queryForObject(getPricesQuery, long.class, userIdx);
        return (totalPrice == null) ? 0L : totalPrice;
    }

    // 보유 리셀 현재가 조회
    public long getResellAssetPrices(int userIdx) {
        String getPricesQuery =
                "select sum(RTT.price)\n" +
                        "from ResellUserTransaction RUT\n" +
                        "   join Resell R on RUT.resellIdx = R.resellIdx and RUT.userIdx = ? and RUT.transactionStatus = 1 and RUT.sellCheck = 0\n" +
                        "   join ResellTodayTrans RTT on R.lastTransactionIdx = RTT.resellTransactionIdx";

        Long totalPrice = this.jdbcTemplate.queryForObject(getPricesQuery, long.class, userIdx);
        return (totalPrice == null) ? 0L : totalPrice;
    }

    // 보유 부동산 현재가 조회
    public long getRealEstateAssetPrices(int userIdx) {
        String getPricesQuery =
                "select sum(RETT.price)\n" +
                        "from RealEstateUserTransaction REUT\n" +
                        "   join RealEstate RE on REUT.realEstateIdx = RE.realEstateIdx and REUT.userIdx = ? and REUT.transactionStatus = 1 and REUT.sellCheck = 0\n" +
                        "   join RealEstateTransaction RETT on RE.lastTransactionIdx = RETT.realEstateTransactionIdx";

        Long totalPrice = this.jdbcTemplate.queryForObject(getPricesQuery, long.class, userIdx);
        return (totalPrice == null) ? 0L : totalPrice;
    }

    // 보유 현금 조회
    public long getCurrentCash(int userIdx) {
        String getTotalCashQuery = "select currentCash from Sales where userIdx = ?";

        try {
            Long cash =  this.jdbcTemplate.queryForObject(getTotalCashQuery, long.class, userIdx);
            return (cash == null) ? 0L : cash;
        } catch (EmptyResultDataAccessException e) {
            return 0L;
        }
    }

    //자산 구매 시 자산 감소
    public int updateBuyMyAsset(int userIdx, PostMyAssetReq postMyAssetReq) {
        String updateMyAssetQuery = "update Sales set currentCash = currentCash - ? where userIdx = ?";

        Object[] updateMyAssetParams = new Object[]{postMyAssetReq.getPrice(), userIdx};
        return this.jdbcTemplate.update(updateMyAssetQuery, updateMyAssetParams);
    }

    //자산 판매 시 자산 증가
    public int updateSellMyAsset(int userIdx, double thisTransProfit) {
        String updateMyAssetQuery = "update Sales as S set S.currentCash = S.currentCash * (? + 100) / 100 where userIdx = ?;";

        Object[] updateMyAssetParams = new Object[]{thisTransProfit, userIdx};
        return this.jdbcTemplate.update(updateMyAssetQuery, updateMyAssetParams);
    }


    // 회원가입
    public int createUser(PostUserReq postUserReq) {
        String createUserQuery = "insert into User " +
                "(userName, email, emailCheck, password, profileImg) " +
                "values (?,?,?,?,?); ";
        String createSales = "INSERT INTO Sales (userIdx) (select last_insert_id() as userIdx from User limit 1);";
        Object[] createUserParams = new Object[]{postUserReq.getUserName(), postUserReq.getEmail(),
                postUserReq.getEmailCheck(), postUserReq.getPassword(), postUserReq.getProfileImg()};
        int userIdx = this.jdbcTemplate.update(createUserQuery, createUserParams);
        this.jdbcTemplate.update(createSales);
        return userIdx;
    }

    // 이메일 확인
    public int checkEmail(String email) {
        String checkEmailQuery = "select exists(select email from User where email = ?)";
        String checkEmailParams = email; // 해당(확인할) 이메일 값
        return this.jdbcTemplate.queryForObject(checkEmailQuery,
                int.class,
                checkEmailParams);
    }

    // userIdx로 회원이 존재하는지 확인
    public int checkUserExist(int userIdx) {
        String checkUserExistQuery = "select exists(select userIdx from User where userIdx = ?)";
        int checkUserExistParams = userIdx;
        return this.jdbcTemplate.queryForObject(checkUserExistQuery,
                int.class,
                checkUserExistParams);
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

    // 회원 프로필 이미지 url 반환
    public GetProfileImgRes getUserProfileImgUrl(int userIdx){
        String getUserProfileImgUrl = "select profileImg from User where userIdx = ?;";
        try{
            return this.jdbcTemplate.queryForObject(getUserProfileImgUrl,
                    (rs, row) -> new GetProfileImgRes(rs.getString("profileImg")), userIdx);
        } catch (EmptyResultDataAccessException e) { // 쿼리문에 해당하는 결과가 없을 때
            return null;
        }
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
                "select S.stockIdx as idx,\n" +
                        "       S.name,\n" +
                        "       STT.price,\n" +
                        "       ST.price           as s2Price,\n" +
                        "       STT.transactionTime,\n" +
                        "       ST.transactionTime as s2TransactionTime,\n" +
                        "       SUT.sellCheck,\n" +
                        "       SUT.updatedAt,\n" +
                        "       II.iconImage\n" +
                        "from Stock as S\n" +
                        "         join StockUserTransaction SUT\n" +
                        "              on S.stockIdx = SUT.stockIdx and SUT.userIdx = ? and SUT.transactionStatus = 1\n" +
                        "         join StockTodayTrans STT on S.lastTransactionIdx = STT.stockTransactionIdx\n" +
                        "         join StockTransaction ST on S.s2LastTransactionIdx = ST.stockTransactionIdx\n" +
                        "         join IconImage II on S.iconImageIdx = II.iconImageIdx;";

        return this.jdbcTemplate.query(getStockAssetQuery, getMyAssetRowMapper(), userIdx);
    }

    // 리셀 자산 조회
    public List<GetMyAssetRes> getResellAsset(int userIdx) {
        String getResellAssetQuery =
                "select R.resellIdx as idx,\n" +
                        "       R.name,\n" +
                        "       RTT.price,\n" +
                        "       RT.price           as s2Price,\n" +
                        "       RTT.transactionTime,\n" +
                        "       RT.transactionTime as s2TransactionTime,\n" +
                        "       RUT.sellCheck,\n" +
                        "       RUT.updatedAt,\n" +
                        "       R.image1 as iconImage\n" +
                        "from Resell as R\n" +
                        "         join ResellUserTransaction RUT\n" +
                        "              on R.resellIdx = RUT.resellIdx and RUT.userIdx = ? and RUT.transactionStatus = 1\n" +
                        "         join ResellTodayTrans RTT on R.lastTransactionIdx = RTT.resellTransactionIdx\n" +
                        "         join ResellTodayTrans RT on R.s2LastTransactionIdx = RT.resellTransactionIdx;";

        return this.jdbcTemplate.query(getResellAssetQuery, getMyAssetRowMapper(), userIdx);
    }

    // 부동산 자산 조회
    public List<GetMyAssetRes> getRealEstateAsset(int userIdx) {
        String getRealEstateAssetQuery =
                "select RE.realEstateIdx     as idx,\n" +
                        "       RE.name,\n" +
                        "       RETT.price,\n" +
                        "       RET.price           as s2Price,\n" +
                        "       RETT.transactionTime,\n" +
                        "       RET.transactionTime as s2TransactionTime,\n" +
                        "       REUT.sellCheck,\n" +
                        "       REUT.updatedAt,\n" +
                        "       II.iconImage\n" +
                        "from RealEstate as RE\n" +
                        "         join RealEstateUserTransaction REUT\n" +
                        "              on RE.realEstateIdx = REUT.realEstateIdx and REUT.userIdx = ? and REUT.transactionStatus = 1\n" +
                        "         join RealEstateTransaction RETT on RE.lastTransactionIdx = RETT.realEstateTransactionIdx\n" +
                        "         join RealEstateTransaction RET on RE.s2LastTransactionIdx = RET.realEstateTransactionIdx\n" +
                        "         join IconImage II on RE.iconImageIdx = II.iconImageIdx;\n";

        return this.jdbcTemplate.query(getRealEstateAssetQuery, getMyAssetRowMapper(), userIdx);
    }

    public GetUserInfo getUserInfo(int userIdx) {
        String getUserInfoQuery = "select userName, email, profileImg from User where userIdx = ?;";
        try{
            return this.jdbcTemplate.queryForObject(getUserInfoQuery,
                    (rs, row) -> new GetUserInfo(rs.getString("userName"), rs.getString("email"), rs.getString("profileImg"))
                    , userIdx);
        }
        catch(EmptyResultDataAccessException e){
            return null;
        }
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
                postMyAssetQuery = "insert into RealEstateUserTransaction(userIdx, realEstateIdx, price, transactionTime) values (?,?,?,?)";
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
                sellMyAssetQuery = "INSERT INTO StockUserTransaction(userIdx, stockIdx, price, transactionTime, sellCheck) VALUES (?,?,?,?,1);";
                break;
            case "resell":
                sellMyAssetQuery = "INSERT INTO ResellUserTransaction(userIdx, resellIdx, price, transactionTime, sellCheck) VALUES (?,?,?,?,1);";
                break;
            case "realestate":
                sellMyAssetQuery = "INSERT INTO RealEstateUserTransaction(userIdx, realEstateIdx, price, transactionTime, sellCheck) VALUES (?,?,?,?,1);";
                break;
        }

        Object[] sellMyAssetParams = new Object[]{userIdx, postMyAssetReq.getAssetIdx(), postMyAssetReq.getPrice(), postMyAssetReq.getTransactionTime()};
        return this.jdbcTemplate.update(sellMyAssetQuery, sellMyAssetParams);
    }

    public GetUserROERes getUserROERes(int userIdx){
        String getUserROEQuery = "select totalSale, numOfTransaction from Sales where userIdx = ?;";
        try{
            return this.jdbcTemplate.queryForObject(getUserROEQuery,
                    (rs, rowNum) -> new GetUserROERes(rs.getDouble("totalSale") / rs.getInt("numOfTransaction")), userIdx);
        }
        catch(EmptyResultDataAccessException e){
            return null;
        }
    }

    public List<Asset> getPreviousTransaction(int userIdx, PostMyAssetReq postMyAssetReq){
        String getPreviousTransactionQuery = "";

        switch (postMyAssetReq.getCategory()) {
            case "stock":
                getPreviousTransactionQuery = "select SUT.stockIdx as idx, SUT.sellCheck, SUT.transactionTime, SUT.price\n" +
                        "                        from StockUserTransaction as SUT\n" +
                        "                        where SUT.userIdx = ? and SUT.stockIdx = ?\n" +
                        "order by SUT.updatedAt desc\n" +
                        "limit 1;";
                break;
            case "resell":
                getPreviousTransactionQuery =
                        "select RUT.resellIdx as idx, RUT.sellCheck, RUT.transactionTime, RUT.price\n" +
                                "                        from ResellUserTransaction as RUT\n" +
                                "                        where RUT.userIdx = ? and RUT.resellIdx = ?\n" +
                                "order by RUT.updatedAt desc\n" +
                                "limit 1;";
                break;
            case "realestate":
                getPreviousTransactionQuery =
                        "select REUT.realEstateIdx as idx, REUT.sellCheck, REUT.transactionTime, REUT.price\n" +
                                "                        from RealEstateUserTransaction as REUT\n" +
                                "                        where REUT.userIdx = ? and REUT.realestateIdx = ?\n" +
                                "order by REUT.updatedAt desc\n" +
                                "limit 1;";
                break;
            default:
                break;
        }

        Object [] getPreviousTransactionParam = new Object[] {userIdx, postMyAssetReq.getAssetIdx()};
        return this.jdbcTemplate.query(getPreviousTransactionQuery, assetRowMapper(), getPreviousTransactionParam);
    }

    // 리스트 노출 상태 변경
    // switch -> if 한 이유는 상수와의 비교를 위해서. 상수를 쓰는 이유는 같은 내용을 중복해서 사용하게 되었을 때 변경할 코드를 줄이기 위함
    public int changeMyAssetList(int userIdx, PostMyAssetReq postMyAssetReq) {

        String changeMyAssetListQuery = "", category = postMyAssetReq.getCategory();

        if(category.equals(ASSET_CATEGORY_STOCK)) {
            changeMyAssetListQuery = "UPDATE StockUserTransaction SET transactionStatus=0 where useridx=? and stockidx = ? and transactionstatus=1";
        } else if(category.equals(ASSET_CATEGORY_RESELL)){
            changeMyAssetListQuery = "UPDATE ResellUserTransaction SET transactionStatus=0 WHERE userIdx=? AND resellIdx = ? AND transactionStatus=1";
        } else {
            changeMyAssetListQuery = "UPDATE RealEstateUserTransaction SET transactionStatus=0 WHERE userIdx=? AND realEstateIdx = ? AND transactionStatus=1";
        }

        Object[] changeMyAssetListParams = new Object[]{userIdx, postMyAssetReq.getAssetIdx()};
        return this.jdbcTemplate.update(changeMyAssetListQuery, changeMyAssetListParams);
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
                "select transactionTime,\n" +
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
                "select transactionTime,\n" +
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
                "select transactionTime,\n" +
                "       price,\n" +
                "       sellCheck\n" +
                "from ResellUserTransaction as RUT\n" +
                "where RUT.resellIdx = ?\n" +
                "and RUT.userIdx = ?;";
        Object[] getTransactionHistoryParams = new Object[] {resellIdx, userIdx};
        return this.jdbcTemplate.query(getTransactionHistoryQuery, transactionHistoryRowMapper(), getTransactionHistoryParams);
    }

    public List<GetUserInfoRes> getUserInfoList(){
        String getUserInfoQuery = "select U.userName, email from User as U;";
        return this.jdbcTemplate.query(getUserInfoQuery,
                (rs, row) -> new GetUserInfoRes(rs.getString("userName"), rs.getString("email")));
    }

    public int countUserPreviousTransOnCategory(int userIdx, PostMyAssetReq postMyAssetReq){
        String category = postMyAssetReq.getCategory();
        int assetIdx = postMyAssetReq.getAssetIdx();
        String query;
        if(category.equals(ASSET_CATEGORY_STOCK)){
            query = "select count(*) / 2 from StockUserTransaction where userIdx = ? and stockIdx = ?;";
        }
        else if(category.equals(ASSET_CATEGORY_REALESTATE)){
            query = "select count(*) / 2 from RealEstateUserTransaction where userIdx = ? and realEstateIdx = ?";
        }
        else {
            query = "select count(*) / 2 from ResellUserTransaction where userIdx = ? and resellIdx = ?;";
        }
        Object[] params = new Object[]{userIdx, assetIdx};
        return this.jdbcTemplate.queryForObject(query, int.class, params);
    }

    public List<GetUserInfoForRank> getUserInfoForRankList(){
        String query = "select U.userIdx, U.userName, U.profileImg, S.currentCash as userAsset from User as U, Sales as S where U.userIdx = S.userIdx;";
        return this.jdbcTemplate.query(query, userRankRowMapper());
    }

    private RowMapper<GetUserInfoForRank> userRankRowMapper(){
        return new RowMapper<GetUserInfoForRank>() {
            @Override
            public GetUserInfoForRank mapRow(ResultSet rs, int rowNum) throws SQLException {
                GetUserInfoForRank getUserInfoForRank = new GetUserInfoForRank();
                getUserInfoForRank.setUserIdx(rs.getInt("userIdx"));
                getUserInfoForRank.setUserName(rs.getString("userName"));
                getUserInfoForRank.setProfileImg(rs.getString("profileImg"));
                getUserInfoForRank.setUserAsset(rs.getLong("userAsset"));
                return getUserInfoForRank;
            }
        };
    }

    private RowMapper<Asset> assetRowMapper(){
        return new RowMapper<Asset>() {
            @Override
            public Asset mapRow(ResultSet rs, int rowNum) throws SQLException {
                GetMyAssetRes getMyAssetRes = new GetMyAssetRes();
                Asset asset = new Asset();
                asset.setAssetIdx(rs.getInt("idx"));
                asset.setSellCheck(rs.getInt("sellCheck"));
                asset.setPrice(rs.getLong("price"));
                asset.setTransactionTime(rs.getString("transactionTime"));
                return asset;
            }
        };
    }

    private RowMapper<GetMyAssetRes> getMyAssetRowMapper(){
        return new RowMapper<GetMyAssetRes>() {
            @Override
            public GetMyAssetRes mapRow(ResultSet rs, int rowNum) throws SQLException {
                GetMyAssetRes getMyAssetRes = new GetMyAssetRes();
                getMyAssetRes.setAssetIdx(rs.getInt("idx"));
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
