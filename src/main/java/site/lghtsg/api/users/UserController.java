package site.lghtsg.api.users;

import io.jsonwebtoken.Jwt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.BaseResponse;
import site.lghtsg.api.stocks.StockProvider;
import site.lghtsg.api.users.model.*;
import site.lghtsg.api.utils.JwtService;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;
import java.util.List;

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
    @Autowired
    private final EmailService emailService;

    public UserController(UserProvider userProvider, UserService userService, JwtService jwtService, EmailService emailService) {
        this.userProvider = userProvider;
        this.userService = userService;
        this.jwtService = jwtService;
        this.emailService = emailService;
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
     * Email 인증 API
     * [Post] /users/sign-up/emailCheck
     */
    @ResponseBody
    @PostMapping("/sign-up/emailCheck")
    public BaseResponse<String> EmailCheck(@RequestBody EmailCheckReq emailCheckReq) throws MessagingException, UnsupportedEncodingException  {
        String authCode = emailService.sendEmail(emailCheckReq.getEmail());
        return new BaseResponse<>(authCode);
    }

    /**
     * 로그인 API
     * [POST] /users/log-in
     */
    @ResponseBody
    @PostMapping("/log-in")
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
    @PatchMapping("/changeInfo/pw")
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
    @PatchMapping("/changeInfo/proImg")
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
    @PatchMapping("/delete-user")
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
     * 나의 자산 조회 API
     * [GET] /users/my-asset
     */
    @ResponseBody
    @GetMapping("/my-asset")
    public BaseResponse<List<GetMyAssetRes>> getMyAsset(@RequestParam(required = false) String sort) {
        try {
            int userIdx = jwtService.getUserIdx();

            List<GetMyAssetRes> getMyStock = StockProvider.stockBox(userIdx);
            // List<GetMyAssetRes> getMyResell = StockProvider.resellBox(user.getUserIdx());
            // List<GetMyAssetRes> getMyRealEstate = StockProvider.realestateBox(user.getUserIdx());


            return new BaseResponse<>(getMyAssetRes);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
}


