package site.lghtsg.api.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.BaseResponse;
import site.lghtsg.api.users.model.*;
import site.lghtsg.api.utils.JwtService;
import site.lghtsg.api.utils.RedisService;
import site.lghtsg.api.utils.S3Uploader;

import javax.mail.MessagingException;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static site.lghtsg.api.config.BaseResponseStatus.*;
import static site.lghtsg.api.config.Constant.DEFAULT_PROFILE_IMG_URL;
import static site.lghtsg.api.config.Constant.PARAM_DEFAULT;
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
    @Autowired
    private ImageUploadService imageUploadService;

    public UserController(UserProvider userProvider, UserService userService, JwtService jwtService,
                          EmailService emailService) {
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
        // 프로필 이미지 값 null 인 경우 default image url 로 set
        if(postUserReq.getProfileImg() == null) {
            postUserReq.setProfileImg(DEFAULT_PROFILE_IMG_URL);
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
     * Email 인증 APㅌI
     * [Post] /users/sign-up/email-check
     */
    @ResponseBody
    @PostMapping("/sign-up/email-check")
    public BaseResponse<String> EmailCheck(@RequestBody EmailCheckReq emailCheckReq) throws MessagingException, UnsupportedEncodingException {
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
     * 비밀번호 수정 (로그인 못했을 때) API
     * [PATCH] /users/changeInfo/pw-not-login
     */
    @ResponseBody
    @PatchMapping("/changeInfo/pw-not-login")
    public BaseResponse<String> modifyUserPasswordNotLogin(@RequestBody PatchUserPasswordNotLoginReq patchUserPassword) {
        try {
            userService.modifyUserPasswordNotLogin(patchUserPassword);

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
    @ResponseBody
    @PostMapping("/log-out")
    public BaseResponse<String> logOut(@RequestBody String accessToken) {
        try {
            int userIdx = jwtService.getUserIdx();
            String accessToken1 = jwtService.getJwt();
            userService.logout(userIdx, accessToken1);
            String result = "로그아웃 완료";
            return new BaseResponse<>(result);
        } catch (BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

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
     * 특정 사용자 수익율 조회 API
     * [GET] /users/roe
     */
    @ResponseBody
    @GetMapping("/roe")
    public BaseResponse<GetUserROERes> getUserROE(){
        try{
            int userIdx = jwtService.getUserIdx();
            GetUserROERes getUserROERes = userProvider.getUserROERes(userIdx);
            return new BaseResponse<>(getUserROERes);
        }
        catch(BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * [한] :
     * 사용자 개인정보 반환 api
     * 필요가 없을 것 같아 개인정보 확인 화면 페이지를 뺐었으나,
     * 기능 추가를 위해 만들어두는 것이 맞아 api 추가하였음.
     */
    @ResponseBody
    @GetMapping("/info")
    public BaseResponse<GetUserInfo> getUserInfo(){
        try{
            int userIdx = jwtService.getUserIdx();
            GetUserInfo getUserInfo = userProvider.getUserInfo(userIdx);
            return new BaseResponse<>(getUserInfo);
        }
        catch(BaseException baseException){
            return new BaseResponse<>(baseException.getStatus());
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
    public BaseResponse<String> buyMyAsset(@RequestBody PostMyAssetReq postMyAssetReq) {
        // validation
        if(validatePostMyAssetReq(postMyAssetReq) == 0){
            return new BaseResponse<>("잘못된 입력");
        }
        try {
            int userIdx = jwtService.getUserIdx();
            userService.buyMyAsset(userIdx, postMyAssetReq);
            String result = "구매 완료";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * TODO : sales라는 변수 - 현재 거래에서 발생한 손익(수익)율을 따져서 받아와야 함. Service까지 대충 작업 완료
     * 자산 판매 API
     * [POST] /users/my-asset/sell
     */
    @ResponseBody
    @PostMapping("/my-asset/sell")
    public BaseResponse<String> sellMyAsset(@RequestBody PostMyAssetReq postMyAssetReq) {
        // validation
        if(validatePostMyAssetReq(postMyAssetReq) == 0){
            return new BaseResponse<>("잘못된 입력");
        }
        try {
            int userIdx = jwtService.getUserIdx();
            // 자산 판매
            userService.sellMyAsset(userIdx, postMyAssetReq);
            String result = "판매 완료";
            return new BaseResponse<>(result);
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

    /**
     * 구매 / 판매할때 클라이언트에서 받아오는 값 validation 수행
     * @param postMyAssetReq
     * @return
     */
    public int validatePostMyAssetReq(PostMyAssetReq postMyAssetReq) {
        if (postMyAssetReq.getAssetIdx() == 0) return 0;
        if (postMyAssetReq.getCategory() == null) {
            postMyAssetReq.setCategory(PARAM_DEFAULT);
            return 0;
        }
        if (postMyAssetReq.getPrice() == 0L) return 0;
        return 1;
    }

    /**
     * 단일 자산 화면 사용자 거래 이력 제공 API - 주식
     * parameters : category = { stock, realestate, resell }
     *              assetIdx = { 1, ... }
     * [GET] /users/transactionHistory?category=stock&assetIdx={idx}
     * [GET] /users/transactionHistory?category=realestate&assetIdx={idx}
     * [GET] /users/transactionHistory?category=resell&assetIdx={idx}
     */
    @ResponseBody
    @GetMapping("/transactionHistory")
    public BaseResponse<List<GetUserTransactionHistoryRes>> userTransactionHistory(@RequestParam String category, @RequestParam long assetIdx){
        // TODO : assetIdx : 존재하는 idx 인지 validation, 혹은 잘못된 idx 왔을 때 에러 구분 필요
        try {
            // category validation
            if(category == null) category = PARAM_DEFAULT;

            int userIdx = jwtService.getUserIdx();
            return new BaseResponse<>(userProvider.getUserTransactionHistory(category, userIdx, assetIdx));
        }
        catch(BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * access token 재발급 API
     * [POST] /users/token/re-issue
     */
    @ResponseBody
    @PostMapping("/token/re-issue")
    public BaseResponse<Token> accessTokenReIssue(@RequestParam(required = false) String userIdx) {
        try {
            Token token = new Token();
            token.setAccessToken(jwtService.getJwt());
            jwtService.validateRefreshToken(token);
            token.setAccessToken(jwtService.reIssueAccessToken(token));
            return new BaseResponse<>(token);
        } catch (BaseException exception) {
                    return new BaseResponse<>(exception.getStatus());
        }
    }

    @ResponseBody
    @PostMapping(value="/upload-image",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<PostProfileImgRes> imageUploader(@RequestParam(value="image")MultipartFile image) {
        try{
            PostProfileImgRes postProfileImgRes = new PostProfileImgRes();

            // 이미지 s3 업로드
            String url = imageUploadService.upload(image);

            // url 반환
            postProfileImgRes.setUrl(url);
            return new BaseResponse<>(postProfileImgRes);
        }
        catch (BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }


    @ResponseBody
    @GetMapping("/proImg")
    public BaseResponse<GetProfileImgRes> getUserImageUrl(){
        try{
            int userIdx = jwtService.getUserIdx();
            System.out.println(userIdx);
            GetProfileImgRes getProfileImgRes = userProvider.getUserImageUrl(userIdx);
            return new BaseResponse<>(getProfileImgRes);
        }
        catch(BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 개발 시 현재 사용자가 제대로 등록되었는지 리스트를 확인하기 위한 용도
     */
    @ResponseBody
    @GetMapping("")
    public BaseResponse<List<GetUserInfoRes>> getUserList(){
        try{
            List<GetUserInfoRes> getUserInfoList = userProvider.getUserList();
            return new BaseResponse<>(getUserInfoList);
        }
        catch(BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }
}

