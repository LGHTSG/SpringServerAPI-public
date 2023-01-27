package site.lghtsg.api.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.BaseResponse;
import site.lghtsg.api.users.model.*;
import site.lghtsg.api.utils.JwtService;

import javax.mail.MessagingException;

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
     * [Post] /users/sign-up/email-check
     */
    @ResponseBody
    @PostMapping("/sign-up/email-check")
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

            List<GetMyAssetRes> resultOfAsset = userProvider.myAsset(userIdx);

            return new BaseResponse<>(resultOfAsset);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 자산 구매 API
     * [POST] /users/my-asset/purchase
     */
    @ResponseBody
    @PostMapping("/my-asset/purchase")
    public BaseResponse<String> postMyAsset(@RequestBody PostMyAssetReq postMyAssetReq) {
        try {
            int userIdx = jwtService.getUserIdx();
            userService.postMyAsset(userIdx, postMyAssetReq);
            String result = "구매 완료";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * TODO : sales라는 변수 - 현재 거래에서 발생한 손익(수익)율을 따져서 받아와야 함. Service까지 대충 작업 완료
     * 자산 판매 API
     * [POST] /users/my-asset/sale
     */
    @ResponseBody
    @PostMapping("/my-asset/sale")
    public BaseResponse<String> saleMyAsset(@RequestBody PostMyAssetReq postMyAssetReq) {
        try {
            int userIdx = jwtService.getUserIdx();
            // 이 전에 구매한 자산이 있는지 확인 ->
            //      numOfAsset이 1일 때만 진행 가능, 나머지는 오류
            int numOfAsset = userProvider.checkMyAsset(userIdx, postMyAssetReq);
            if(numOfAsset == 1) {
                // 판매 코드
                userService.saleMyAsset(userIdx, postMyAssetReq);
                // Sales 갱신
                userService.updateTableSales(userIdx);

                String result = "판매 완료";
                return new BaseResponse<>(result);
            } else {
                throw new BaseException(NOT_EXIST_ASSET);
            }
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 리스트 제거 API
     * [PATCH] /users/my-asset/delete-list
     */
    @ResponseBody
    @PatchMapping("/my-asset/delete-list")
    public BaseResponse<String> deleteMyAssetList(@RequestBody PostMyAssetReq postMyAssetReq) {
        try {
            int userIdx = jwtService.getUserIdx();
            userService.deleteMyAssetList(userIdx, postMyAssetReq);
            String result = "리스트에서 제거 완료";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
}


