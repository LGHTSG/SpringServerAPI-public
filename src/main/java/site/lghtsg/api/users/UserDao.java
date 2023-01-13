package site.lghtsg.api.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import site.lghtsg.api.users.model.*;

import javax.sql.DataSource;

@Repository
public class UserDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {this.jdbcTemplate = new JdbcTemplate(dataSource);}

    // 회원가입
    public int createUser(PostUserReq postUserReq) {
        String createUserQuery = "insert into User" +
                "(userName, email, emailCheck, password, profileImg, termsCheck)" +
                "values (?,?,?,?,?,?)";
        Object[] createUserParams = new Object[]{postUserReq.getUserName(), postUserReq.getEmail(), postUserReq.getEmailCheck(),
        postUserReq.getPassword(), postUserReq.getProfileImg(), postUserReq.getTermsCheck()};
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

    // 로그인
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

}
