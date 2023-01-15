package site.lghtsg.api.realestates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.realestates.model.GetRealEstateBox;
import site.lghtsg.api.realestates.model.RealEstateData;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class RealEstateDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // 이메일 확인
//    public int checkEmail(String email) {
//        String checkEmailQuery = "select exists(select email from User where email = ?)"; // User Table에 해당 email 값을 갖는 유저 정보가 존재하는가?
//        String checkEmailParams = email; // 해당(확인할) 이메일 값
//        return this.jdbcTemplate.queryForObject(checkEmailQuery,
//                int.class,
//                checkEmailParams); // checkEmailQuery, checkEmailParams를 통해 가져온 값(intgud)을 반환한다. -> 쿼리문의 결과(존재하지 않음(False,0),존재함(True, 1))를 int형(0,1)으로 반환됩니다.
//    }
//
//    // 회원정보 변경
//    public int modifyUserName(PatchUserReq patchUserReq) {
//        String modifyUserNameQuery = "update User set nickname = ? where userIdx = ? "; // 해당 userIdx를 만족하는 User를 해당 nickname으로 변경한다.
//        Object[] modifyUserNameParams = new Object[]{patchUserReq.getNickname(), patchUserReq.getUserIdx()}; // 주입될 값들(nickname, userIdx) 순
//
//        return this.jdbcTemplate.update(modifyUserNameQuery, modifyUserNameParams); // 대응시켜 매핑시켜 쿼리 요청(생성했으면 1, 실패했으면 0)
//    }
//
//    public int modifyUserPhoneNum(PatchUserReq patchUserReq) {
//        String modifyUserNameQuery = "update User set phoneNum = ? where userIdx = ? "; // 해당 userIdx를 만족하는 User의 해당 phoneNum 변경한다.
//        Object[] modifyUserNameParams = new Object[]{patchUserReq.getPhoneNum(), patchUserReq.getUserIdx()}; // 주입될 값들(phoneNum, userIdx) 순
//
//        return this.jdbcTemplate.update(modifyUserNameQuery, modifyUserNameParams); // 대응시켜 매핑시켜 쿼리 요청(생성했으면 1, 실패했으면 0)
//    }

    // realEstate 에 저장된 모든 부동산 리스트를 가져온다.
    public List<GetRealEstateBox> getAllRealEstateBox() {
        String getRealEstateBoxQuery = "select re.realEstateIdx, re.name, re.iconImage, ret.price\n" +
                "from RealEstate as re\n" +
                "join (select realEstateIdx, price, max(transactionTime) as transactionTime\n" +
                "      from RealEstateTransaction\n" +
                "      group by realEstateIdx) as ret\n" +
                "on ret.realEstateIdx = re.realEstateIdx;";

        return this.jdbcTemplate.query(getRealEstateBoxQuery,
                (rs, rowNum) -> new GetRealEstateBox(
                        rs.getInt("realEstateIdx"),
                        rs.getString("name"),
                        rs.getString("iconImage"),
                        rs.getString("rateOfChange"), //
                        rs.getString("rateCalDiff"), //
                        rs.getInt("price")
                )
        );
    }

    public int getLatestPrice(int realEstateIdx) {
        // 굳이 이렇게 따로 해야할까
        // sql 쿼리로 그냥 지금 join해서 가장 최신 prices 도 가져올 수 있고
        // 그러면 유일하게 안들어가는게 rateOfChange / rateCal... -> privider에서 그냥 lamda로 추가하면 될 문제.
        // 고로 이 함수는 필요 x
        String getLatestPriceQuery =
                "select price " +
                "from RealEstateTransaction " +
                "where (price, transactionTime) in (" +
                    "select price, max(transactionTime) as transactionTime " +
                    "from RealEstate group by price" +
                ")" +
                "order by transactionTime desc";



    }

    // User 테이블에 존재하는 전체 유저들의 정보 조회
    public List<GetUserRes> getUsers() {
        System.out.println("dao");
        String getUsersQuery = "select * from User"; //User 테이블에 존재하는 모든 회원들의 정보를 조회하는 쿼리
        return this.jdbcTemplate.query(getUsersQuery,
                getUserResRowMapper()  // RowMapper(위의 링크 참조): 원하는 결과값 형태로 받기
        ); // 복수개의 회원정보들을 얻기 위해 jdbcTemplate 함수(Query, 객체 매핑 정보)의 결과 반환(동적쿼리가 아니므로 Parmas부분이 없음)
    }

    // 해당 nickname을 갖는 유저들의 정보 조회
    public List<GetUserRes> getUsersByNickname(String nickname) {
        String getUsersByNicknameQuery = "select * from User where nickname =?"; // 해당 이메일을 만족하는 유저를 조회하는 쿼리문
        String getUsersByNicknameParams = nickname;
        return this.jdbcTemplate.query(getUsersByNicknameQuery,
                getUserResRowMapper(), // RowMapper(위의 링크 참조): 원하는 결과값 형태로 받기
                getUsersByNicknameParams); // 해당 닉네임을 갖는 모든 User 정보를 얻기 위해 jdbcTemplate 함수(Query, 객체 매핑 정보, Params)의 결과 반환
    }

    // 해당 userIdx를 갖는 유저조회
    public GetUserRes getUser(int userIdx) {
        String getUserQuery = "select * from User where userIdx = ?"; // 해당 userIdx를 만족하는 유저를 조회하는 쿼리문
        int getUserParams = userIdx;
        return this.jdbcTemplate.queryForObject(getUserQuery,
                getUserResRowMapper(), // RowMapper(위의 링크 참조): 원하는 결과값 형태로 받기
                getUserParams); // 한 개의 회원정보를 얻기 위한 jdbcTemplate 함수(Query, 객체 매핑 정보, Params)의 결과 반환
    }

    private RowMapper<GetUserRes> getUserResRowMapper(){
        return new RowMapper<GetUserRes>() {
            @Override
            public GetUserRes mapRow(ResultSet rs, int rowNum) throws SQLException {
                GetUserRes getUserRes = new GetUserRes();
                getUserRes.setUserIdx(rs.getInt("userIdx"));
                getUserRes.setEmail(rs.getString("email"));
                getUserRes.setNickname(rs.getString("nickname"));
                getUserRes.setPassword(rs.getString("password"));
                getUserRes.setPhoneNum(rs.getString("phoneNUm"));
                return getUserRes;
            }
        };
    }
}
