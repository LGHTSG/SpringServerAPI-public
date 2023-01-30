package site.lghtsg.api.users;

import org.apache.commons.codec.binary.StringUtils;
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
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 회원정보 수정 (프로필 사진)
    public void modifyUserProfileImg(PatchUserProfileImgReq patchUserProfileImgReq) throws BaseException {
        try {
            int result = userDao.modifyUserProfileImg(patchUserProfileImgReq);
            if(result == 0) {
                throw new BaseException(MODIFY_FAIL_PROFILEIMAGE);
            }
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
            if(result == 0) {
                throw new BaseException(DELETE_FAIL_USER);
            }
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
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
        if(result == 0) { // 자산 구매에서 실패했다면
            throw new BaseException(PURCHASE_FAIL_ASSET);
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
            // 자산 판매 Dao
            userDao.sellMyAsset(userIdx, postMyAssetReq);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
        // 수익율 계산
        updateTableSales(userIdx, postMyAssetReq, previousTransaction);
    }

    public int buyValidation(int userIdx, PostMyAssetReq postMyAssetReq) throws BaseException{
        Asset previousTransaction;
        // 구매는 이전 거래가 없는 경우 허용
        try {
            previousTransaction = userProvider.getPreviousTransaction(userIdx, postMyAssetReq);
        } catch(BaseException e){
            if(e.getStatus().equals(NO_PREVIOUS_USER_TRANSACTION)) return 1;
            else throw e;
        }
        // 이전 거래가 구매라면 구매 불가
        if(previousTransaction.getSellCheck() == 0) {
            throw new BaseException(PURCHASE_FAIL_ASSET);
        }
        return 1;
    }

    public int sellValidation(int userIdx, PostMyAssetReq postMyAssetReq) throws BaseException {
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
        return 1;
    }

    // Sales 갱신
    public void updateTableSales(int userIdx, PostMyAssetReq postMyAssetReq, Asset previousTransaction) throws BaseException {
        try {
            // 이번 거래 손익율
            double profitRatio = Math.round((double)(postMyAssetReq.getPrice() - previousTransaction.getPrice()) / previousTransaction.getPrice() * 1000) / 10.0;
            // 출력
            System.out.println(profitRatio);
            userDao.updateTableSales(userIdx, profitRatio);
        } catch (Exception exception) {
            throw new BaseException(FAIL_TO_INSERT_SALES);
        }
    }

    // 자산 리스트에서 제거
    public void deleteMyAssetList(int userIdx, PostMyAssetReq postMyAssetReq) throws BaseException {
        try {
            userDao.changeMyAssetList(userIdx, postMyAssetReq);
        }catch (Exception exception) {
            throw new BaseException(DELETE_FAIL_ASSET_LIST);
        }
    }

}
