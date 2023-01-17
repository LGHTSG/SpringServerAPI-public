package site.lghtsg.api.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.BaseResponse;
import site.lghtsg.api.users.model.*;
import site.lghtsg.api.utils.JwtService;

import javax.sound.midi.Patch;

import static site.lghtsg.api.config.BaseResponseStatus.*;
import static site.lghtsg.api.utils.ValidationRegex.isRegexEmail;

@RestController
@RequestMapping("/users")
public class UserController {
    final Logger logger = LoggerFactory.getLogger(this.getClass()); // Log를 남기기: 일단은 모르고 넘어가셔도 무방합니다.

    @Autowired
    private final UserProvider userProvider;
    @Autowired
    private final UserService userService;
    @Autowired
    private final JwtService jwtService;


    public UserController(UserProvider userProvider, UserService userService, JwtService jwtService) {
        this.userProvider = userProvider;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /**
     * 회원가입 API
     * [POST] /users/sign-up
     */
    @ResponseBody
    @PostMapping("/sign-up")
    public BaseResponse<PostUserRes> createUser(@RequestBody PostUserReq postUserReq) {
        // 이메일 빈칸 확인
        if (postUserReq.getEmail() == null) {
            return new BaseResponse<>(EMPTY_EMAIL);
        }
        // 이메일 정규표현식 확인 ( email@~.~ )
        if (!isRegexEmail(postUserReq.getEmail())) {
            return new BaseResponse<>(INVALID_EMAIL);
        }
        // 이메일 중복 확인은 [Service - Provider - Dao] 에서 합니다.
        try {
            PostUserRes postUserRes = userService.createUser(postUserReq);
            return new BaseResponse<>(postUserRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 로그인 API
     * [POST] /users/log-in
     */
    @ResponseBody
    @PostMapping("log-in")
    public BaseResponse<PostLoginRes> logIn(@RequestBody PostLoginReq postLoginReq) {
        try {
            PostLoginRes postLoginRes = userProvider.logIn(postLoginReq);
            return new BaseResponse<>(postLoginRes);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 회원정보 수정 (비밀번호) API
     * [PATCH] /users/changeInfo/pw
     */
    @ResponseBody
    @PatchMapping("changeInfo/pw")
    public BaseResponse<String> modifyUserPassword(@RequestBody PatchUserPasswordReq patchUserPasswordReq) {
        try {
            int userIdx = jwtService.getUserIdx();
            patchUserPasswordReq.setUserIdx(userIdx);
            userService.modifyUserPassword(patchUserPasswordReq);

            String result = "비밀번호 변경 완료!";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 회원정보 수정 (프로필 이미지) API
     * [PATCH] /users/changeInfo/proImg
     */
    @ResponseBody
    @PatchMapping("changeInfo/proImg")
    public BaseResponse<String> modifyUserProfileImg(@RequestBody PatchUserProfileImgReq patchUserProfileImgReq) {
        try {
            int userIdx = jwtService.getUserIdx();

            patchUserProfileImgReq.setUserIdx(userIdx);
            userService.modifyUserProfileImg(patchUserProfileImgReq);

            String result = "프로필 사진 변경 완료!";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 로그아웃 API
     * [POST] /users/log-out
     */

    /**
     * 회원탈퇴 API
     * [PATCH] /users/delete-user
     */
    @ResponseBody
    @PatchMapping("changeInfo/delete-user")
    public BaseResponse<String> deleteUser(@RequestBody PatchUserDeleteReq patchUserDeleteReq) {
        try {
            int userIdx = jwtService.getUserIdx();

            patchUserDeleteReq.setUserIdx(userIdx);
            userService.deleteUser(patchUserDeleteReq);

            String result = "회원 탈퇴가 정상적으로 처리되었습니다.";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     *
     *
     */
}
