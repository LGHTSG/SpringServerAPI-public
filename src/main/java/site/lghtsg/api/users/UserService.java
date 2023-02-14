package site.lghtsg.api.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.Secret.Secret;
import site.lghtsg.api.users.model.*;
import site.lghtsg.api.utils.AES128;
import site.lghtsg.api.utils.JwtService;
import site.lghtsg.api.utils.RedisService;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static site.lghtsg.api.config.BaseResponseStatus.*;

@Service
public class UserService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserDao userDao;
    private final UserProvider userProvider;
    private final JwtService jwtService;
    private final RedisService redisService;
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public UserService(UserDao userDao, UserProvider userProvider, JwtService jwtService, RedisTemplate redisTemplate, RedisService redisService) {
        this.userDao = userDao;
        this.userProvider = userProvider;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
        this.redisService = redisService;
    }

    /**
     * @문제점 :
     *  category 구분을 나의 자산 리스트를 읽어올때는 provider 에서, 나의 자산 리스트에 저장할때는 dao 에서 한다.
     *  때문에 category 입력이 제대로 이루어졌는지 validation 이 골치아파졌다.
     *  타 파트에서도 변수 입력 validation 은 provider / service 에서 진행했기에,
     *  우선은 데이터 나의 자산 조회만 validation 진행하고, 자산 구매 / 판매 시 오는 카테고리 validation 은 프론트를 믿는다.
     * TODO : 시간 남을 때 리팩토링 작업 필요 (아무래도 changeMyAsset 등의 dao 메서드를 각 카테고리마다 구분해야 할 듯 싶다)
     */

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
        // 비밀번호 암호화
        try {
            // 이전 비밀번호 암호화
            pastPassword = new AES128(Secret.USER_INFO_PASSWORD_KEY).encrypt(patchUserPasswordReq.getPastPassword());
            patchUserPasswordReq.setPastPassword(pastPassword);
        } catch (Exception ignored) { // 암호화가 실패하였을 경우 에러 발생
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }
        try {
            // 변경할 비밀번호 암호화
            password = new AES128(Secret.USER_INFO_PASSWORD_KEY).encrypt(patchUserPasswordReq.getPassword());
            patchUserPasswordReq.setPassword(password);
        } catch (Exception ignored) { // 암호화가 실패하였을 경우 에러 발생
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }
        // 비밀번호 변경 전, 이전 비밀번호가 일치하는지 확인한다.
        if (!patchUserPasswordReq.getPastPassword().equals(userProvider.checkPassword(patchUserPasswordReq.getUserIdx()))) {
            throw new BaseException(NOT_MATCH_PASTPASSWORD);
        }
        try {
            int result = userDao.modifyUserPassword(patchUserPasswordReq);
            // 변경 실패
            if (result == 0) {
                throw new BaseException(MODIFY_FAIL_PASSWORD);
            }
        } catch(BaseException be) {
            throw be;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 회원정보 수정 (비밀번호 - 로그인 못했을 때)
    public void modifyUserPasswordNotLogin(PatchUserPasswordNotLoginReq patchUserPassword) throws BaseException {
        String password;
        // 비밀번호 암호화
        try {
            // 변경할 비밀번호 암호화
            password = new AES128(Secret.USER_INFO_PASSWORD_KEY).encrypt(patchUserPassword.getPassword());
            patchUserPassword.setPassword(password);
        } catch (Exception ignored) { // 암호화가 실패하였을 경우 에러 발생
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }
        try {
            int result = userDao.modifyUserPasswordNotLogin(patchUserPassword);
            // 변경 실패
            if (result == 0) {
                throw new BaseException(MODIFY_FAIL_PASSWORD);
            }
        } catch (BaseException be){
            throw be;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 회원정보 수정 (프로필 사진)
    public void modifyUserProfileImg(PatchUserProfileImgReq patchUserProfileImgReq) throws BaseException {
        try {
            int result = userDao.modifyUserProfileImg(patchUserProfileImgReq);
            if(result == 0) throw new BaseException(MODIFY_FAIL_PROFILEIMAGE);

        } catch (BaseException be){
            throw be;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 회원 탈퇴
    public void deleteUser(PatchUserDeleteReq patchUserDeleteReq) throws BaseException {
        String password;
        try {
            password = new AES128(Secret.USER_INFO_PASSWORD_KEY).encrypt(patchUserDeleteReq.getPassword());
            patchUserDeleteReq.setPassword(password);
        } catch (Exception ignored) { // 암호화가 실패하였을 경우 에러 발생
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }
        // 회원 탈퇴하기 전, 비밀번호가 일치하는지 확인한다.
        if (!patchUserDeleteReq.getPassword().equals(userProvider.checkPassword(patchUserDeleteReq.getUserIdx()))) {
            throw new BaseException(NOT_MATCH_PASSWORD);
        }
        try {
            int result = userDao.withdrawUser(patchUserDeleteReq);
            if(result == 0) throw new BaseException(DELETE_FAIL_USER);
        } catch (BaseException be){
            throw be;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 자산 구매
    public void buyMyAsset(int userIdx, PostMyAssetReq postMyAssetReq) throws BaseException {
        int result;
        // 구매 가능한 자산인지 validation
        buyValidation(userIdx, postMyAssetReq);
        try {
            // transactionStatus 수정
            userDao.changeMyAssetList(userIdx, postMyAssetReq);

            // 자산 구매 Dao
            result = userDao.buyMyAsset(userIdx, postMyAssetReq);

            // TODO : [이벤트] 사용자는 자신의 자산에서 해당 자산에 대해 구매 가능한만큼 전부 산다는 가정으로 거래가 진행되므로,
            // TODO : 구매시 자산가격을 감소시키지 않고 판매시에 계산된 수익율을 사용자의 자산에 반영하는 식으로 계산하려고 한다.
//            // 자산 구매 시 자산 감소
//            userDao.updateBuyMyAsset(userIdx, postMyAssetReq);
            // 구매 실패
            if (result == 0) throw new BaseException(PURCHASE_FAIL_ASSET);
        } catch (BaseException be){
            throw be;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 자산 판매
    public void sellMyAsset(int userIdx, PostMyAssetReq postMyAssetReq) throws BaseException {
        int result;
        // 판매 가능한 자산인지 validation
        sellValidation(userIdx, postMyAssetReq);
        // 과거 거래 기록 가지고오기
        Asset previousTransaction = userProvider.getPreviousTransaction(userIdx, postMyAssetReq);

        try {
            // 리스트 상태 변경 Dao
            userDao.changeMyAssetList(userIdx, postMyAssetReq);
            // 수익율 계산
            double thisTransProfit = updateTableSales(userIdx, postMyAssetReq, previousTransaction);
            // TODO : [EVENT] 수익율 기준으로 사용자 자산계산 - 해당 파트 (주식, 부동산, 리셀) 의 거래가 총 2번 이하인 경우에만 자산 반영
            int limit = userDao.countUserPreviousTransOnCategory(userIdx, postMyAssetReq);
            System.out.println("limit : " + limit);
            // 계산된 수익율을 기반으로 사용자 자산 변경
            if(limit < 2) userDao.updateSellMyAsset(userIdx, thisTransProfit);
            // 자산 판매 Dao
            userDao.sellMyAsset(userIdx, postMyAssetReq);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
        // 수익율 계산
        updateTableSales(userIdx, postMyAssetReq, previousTransaction);
    }

    public void buyValidation(int userIdx, PostMyAssetReq postMyAssetReq) throws BaseException{
        Asset previousTransaction;
        // 구매는 이전 거래가 없는 경우만 허용(이벤트)
        try {
            previousTransaction = userProvider.getPreviousTransaction(userIdx, postMyAssetReq);
        } catch(BaseException e){
            if(e.getStatus().equals(NO_PREVIOUS_USER_TRANSACTION)) return; // 과거 거래 기록이 없는 경우도 구매는 허용
            else throw e;
        }

        // 이전 거래가 존재한다면 구매 불가
        throw new BaseException(EVENT_ERROR_DUPLICATE_PURCHASE);
//        // 이전 거래가 구매라면 구매 불가
//        if(previousTransaction.getSellCheck() == 0) {
//            throw new BaseException(PURCHASE_FAIL_ASSET);
//        }
    }

    public void sellValidation(int userIdx, PostMyAssetReq postMyAssetReq) throws BaseException {
        // 이전 거래가 없다면 판매 불가 (getPreviousTransaction에서 오류 반환)
        Asset previousTransaction = userProvider.getPreviousTransaction(userIdx, postMyAssetReq);
        // 이전 거래가 판매라면 판매 불가
        if(previousTransaction.getSellCheck() == 1){
            throw new BaseException(SELL_FAIL_ASSET);
        }
        // 판매하려는 시간이 구매 이전이라면 판매 불가
        String previousTransactionTime = previousTransaction.getTransactionTime();
        if(previousTransactionTime.compareTo(postMyAssetReq.getTransactionTime()) > 0){
            throw new BaseException(SELL_AHEAD_OF_PREVIOUS_PURCHACE);
        }
    }

    // Sales 갱신
    public double updateTableSales(int userIdx, PostMyAssetReq postMyAssetReq, Asset previousTransaction) throws BaseException {
        double profitRatio;
        try {
            // 이번 거래 손익율
            profitRatio = Math.round((double)(postMyAssetReq.getPrice() - previousTransaction.getPrice()) / previousTransaction.getPrice() * 1000) / 10.0;
            // 출력
            System.out.println(profitRatio);
            userDao.updateTableSales(userIdx, profitRatio);
        } catch (Exception exception) {
            throw new BaseException(FAIL_TO_INSERT_SALES);
        }
        return profitRatio;
    }

    // 자산 리스트에서 제거
    public void deleteMyAssetList(int userIdx, PostMyAssetReq postMyAssetReq) throws BaseException {
        try {
            userDao.changeMyAssetList(userIdx, postMyAssetReq);
        }catch (Exception exception) {
            throw new BaseException(DELETE_FAIL_ASSET_LIST);
        }
    }

    // 로그아웃
    // 블랙리스트 올리는 작업 (테스트)
    // AccessToken 전달 시 현재 blacklist 에 있는지 validation
    public void logout(int userIdx, String accessToken) throws BaseException {
        try {
            String userIdxString = Integer.toString(userIdx);
            if (redisTemplate.opsForValue().get(userIdxString) != null) {
                redisTemplate.delete(userIdxString);
            }
            long expiration = jwtService.getExpiration(accessToken);
            redisTemplate.opsForValue().set(accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
            redisService.setValues(userIdxString, "blackList" + accessToken, Duration.ofMillis(expiration));
            redisService.deleteValues(userIdxString); // Redis에서 유저 리프레시 토큰 삭제
        }catch(Exception e){
            throw new BaseException(LOGOUT_REDIS_SERVICE_ERROR);
        }
    }


}
