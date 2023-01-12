package site.lghtsg.api.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import site.lghtsg.api.users.model.PostUserReq;

import javax.sql.DataSource;

@Repository
public class UserDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {this.jdbcTemplate = new JdbcTemplate(dataSource);}

    // 회원가입
    public int createUser(PostUserReq postUserReq) {
        String createUserQuery = "insert into User +" +
                "(userName, email, emailCheck, password, profileImg, termsCheck)"+
                "values (?,?,?,?,?,?)";
        Object[] createUserParams = new Object[]{postUserReq.getUserName(), postUserReq.getEmail(), postUserReq.getEmailCheck(),
        postUserReq.getPassword(), postUserReq.getProfileImg(), postUserReq.getTermsCheck()};
        return this.jdbcTemplate.update(createUserQuery, createUserParams);
    }

    // 이메일 확인
    // 이메일 확인
    public int checkEmail(String email) {
        String checkEmailQuery = "select exists(select email from User where email = ?)"; // User Table에 해당 email 값을 갖는 유저 정보가 존재하는가?
        String checkEmailParams = email; // 해당(확인할) 이메일 값
        return this.jdbcTemplate.queryForObject(checkEmailQuery,
                int.class,
                checkEmailParams);
    }

}
