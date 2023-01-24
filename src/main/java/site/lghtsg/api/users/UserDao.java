package site.lghtsg.api.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import site.lghtsg.api.users.model.*;

import javax.sql.DataSource;
import java.util.List;


@Repository
public class UserDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {this.jdbcTemplate = new JdbcTemplate(dataSource);}

    // 회원가입
    public int createUser(PostUserReq postUserReq) {
        String createUserQuery = "insert into User" +
                "(userName, email, emailCheck, password, profileImg)" +
                "values (?,?,?,?,?)";
        Object[] createUserParams = new Object[]{postUserReq.getUserName(), postUserReq.getEmail(),
                postUserReq.getEmailCheck(), postUserReq.getPassword(), postUserReq.getProfileImg()};
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

        System.out.println("주식 쿼리문 시작");
        String getStockAssetQuery =
                "SELECT S.name AS assetName, ST.price, ST.price*100 AS rateOfChange," +
                        "ST.price-100 AS rateCalDateDiff, II.iconImage," +
                        "SUT.saleCheck, SUT.updatedAt" +
                        "FROM StockUserTransaction AS SUT" +
                        "INNER JOIN StockTransaction AS ST ON ST.stockTransactionIdx = SUT.stockTransactionIdx" +
                        "INNER JOIN Stock AS S ON S.stockIdx = ST.stockIdx" +
                        "INNER JOIN IconImage AS II ON II.iconImageIdx = S.iconImageIdx" +
                        "WHERE userIdx = ?";
        int getStockAssetParams = userIdx;

        System.out.println("주식 쿼리문 완료 및 배치 시작");
        return this.jdbcTemplate.query(getStockAssetQuery,
                (rs, rowNum) -> new GetMyAssetRes(
                        rs.getString("assetName"),
                        rs.getInt("price"),
                        rs.getFloat("rateOfChange"),
                        rs.getString("rateCalDateDiff"),
                        rs.getString("iconImage"),
                        rs.getInt("saleCheck"),
                        rs.getString("updatedAt")),
                getStockAssetParams);
    }

    // 리셀 자산 조회
    public List<GetMyAssetRes> getResellAsset(int userIdx) {
        String getResellAssetQuery =
                "SELECT R.name AS assetName, RT.price, II.iconImage," +
                        "RUT.saleCheck, RUT.updatedAt" +
                        "FROM ResellUserTransaction AS RUT" +
                        "INNER JOIN ResellTransaction AS RT ON RT.stockTransactionIdx = RUT.stockTransactionIdx" +
                        "INNER JOIN Resell AS R ON R.stockIdx = RT.stockIdx" +
                        "INNER JOIN IconImage AS II ON II.iconImageIdx = R.iconImageIdx" +
                        "WHERE userIdx = ?";
        int getResellBoxParams = userIdx;

        return this.jdbcTemplate.query(getResellAssetQuery,
                (rs, rowNum) -> new GetMyAssetRes(
                        rs.getString("assetName"),
                        rs.getInt("price"),
                        rs.getFloat("rateOfChange"),
                        rs.getString("rateCalDateDiff"),
                        rs.getString("iconImage"),
                        rs.getInt("saleCheck"),
                        rs.getString("updatedAt")),
                getResellBoxParams);
    }

    // 부동산 자산 조회
    public List<GetMyAssetRes> getRealEstateAsset(int userIdx) {
        String getRealEstateAssetQuery =
                "SELECT RE.name AS assetName, RET.price, II.iconImage," +
                        "REUT.saleCheck, REUT.updatedAt" +
                        "FROM RealEstateUserTransaction AS REUT" +
                        "INNER JOIN RealEstateTransaction AS RET ON RET.stockTransactionIdx = REUT.stockTransactionIdx" +
                        "INNER JOIN RealEstate AS RE ON RE.stockIdx = RET.stockIdx" +
                        "INNER JOIN IconImage AS II ON II.iconImageIdx = RE.iconImageIdx" +
                        "WHERE userIdx = ?";
        int getRealEstateParams = userIdx;

        return this.jdbcTemplate.query(getRealEstateAssetQuery,
                (rs, rowNum) -> new GetMyAssetRes(
                        rs.getString("assetName"),
                        rs.getInt("price"),
                        rs.getFloat("rateOfChange"),
                        rs.getString("rateCalDateDiff"),
                        rs.getString("iconImage"),
                        rs.getInt("saleCheck"),
                        rs.getString("updatedAt")),
                getRealEstateParams);
    }
}
