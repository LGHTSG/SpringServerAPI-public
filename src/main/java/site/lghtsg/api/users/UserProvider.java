package site.lghtsg.api.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.lghtsg.api.config.BaseException;
import static site.lghtsg.api.config.BaseResponseStatus.*;

import site.lghtsg.api.config.Secret.Secret;
import site.lghtsg.api.utils.AES128;
import site.lghtsg.api.utils.JwtService;
import site.lghtsg.api.users.model.*;

@Service
public class UserProvider {

    private final UserDao userDao;
    private final JwtService jwtService;

    final Logger loger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public UserProvider(UserDao userDao, JwtService jwtService) {
        this.userDao = userDao;
        this.jwtService = jwtService;
    }

    // 이메일 체크 (회원가입 +a 사용)
    public int checkEmail(String email) throws BaseException {
        try {
            return userDao.checkEmail(email);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 비밀번호 체크 (회원 정보 수정 때 사용)
    public String checkPassword(int userIdx) throws BaseException {
        try {
            return userDao.getOnlyPwd(userIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 로그인
    public PostLoginRes logIn(PostLoginReq postLoginReq) throws BaseException {
        User user = userDao.getPassword(postLoginReq);
        String password;
        try {
            password = new AES128(Secret.USER_INFO_PASSWORD_KEY).decrypt(user.getPassword());
        } catch (Exception ignored) {
            throw new BaseException(PASSWORD_DECRYPTION_ERROR);
        }

        // 탈퇴한 회원인지 확인 (테스트 X)
        if (user.getWithdrawCheck() != 0) throw new BaseException(WITHDRAW_USER);

        // 여기서부터 본격적인 로그인입니다.
        if (postLoginReq.getPassword().equals(password)) {  // password가 일치하는 지 확인 (encryption된 password)
            int userIdx = userDao.getPassword(postLoginReq).getUserIdx();
            String jwt = jwtService.createJwt(userIdx);     // userIdx를 바탕으로 jwt 발급
            return new PostLoginRes(userIdx,jwt);

        } else { // 비밀번호가 다르다면 에러메세지를 출력한다.
            throw new BaseException(FAILED_TO_LOGIN);
        }
    }
}
