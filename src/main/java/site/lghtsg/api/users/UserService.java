package site.lghtsg.api.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.Secret.Secret;
import site.lghtsg.api.users.model.*;
import site.lghtsg.api.utils.AES128;
import site.lghtsg.api.utils.JwtService;
import static site.lghtsg.api.config.BaseResponseStatus.*;

@Service
public class UserService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserDao userDao;
    private final UserProvider userProvider;
    private final JwtService jwtService;

    @Autowired
    public UserService(UserDao userDao, UserProvider userProvider, JwtService jwtService) {
        this.userDao = userDao;
        this.userProvider = userProvider;
        this.jwtService = jwtService;
    }

    // 회원가입 [POST]
    public PostUserRes createUser(PostUserReq postUserReq) throws BaseException {
        // 이메일 중복 확인
        if (userProvider.checkEmail(postUserReq.getEmail()) == 1) {
            throw new BaseException(EXISTS_EMAIL);
        }
        String password;
        try {
            password = new AES128(Secret.USER_INFO_PASSWORD_KEY).encrypt(postUserReq.getPassword());
            postUserReq.setPassword(password);
        } catch (Exception ignored) { // 암호화가 실패하였을 경우 에러 발생
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }
        try {
            int userIdx = userDao.createUser(postUserReq);
            return new PostUserRes(userIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 회원정보 수정 (비밀번호)
    public void modifyUserPassword(PatchUserPasswordReq patchUserPasswordReq) throws BaseException {
        String password;
        String pastPassword;
        try {
            password = new AES128(Secret.USER_INFO_PASSWORD_KEY).encrypt(patchUserPasswordReq.getPassword());
            patchUserPasswordReq.setPassword(password);
        } catch (Exception ignored) { // 암호화가 실패하였을 경우 에러 발생
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }
        try {
            // 비밀번호 변경 전, 비밀번호가 일치하는지 확인한다.
            if (patchUserPasswordReq.getPassword().equals(userDao.getOnlyPwd(patchUserPasswordReq.getUserIdxByJwt()))) {
                int result = userDao.modifyUserPassword(patchUserPasswordReq);
                if (result == 0) {
                    throw new BaseException(MODIFY_FAIL_PASSWORD);
                } else {
                    throw new BaseException(NOT_MATCH_PASSWORD);
                }
            }
        } catch (Exception exception) {
            throw new BaseException((DATABASE_ERROR));
        }
    }

}
