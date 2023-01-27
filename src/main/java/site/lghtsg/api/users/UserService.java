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
    public void postMyAsset(int userIdx, PostMyAssetReq postMyAssetReq) throws BaseException {
        try {
            // 리스트 상태 변경 Dao
            userDao.changeMyAssetList(userIdx, postMyAssetReq);
            // 자산 구매 Dao
            int result = userDao.postMyAsset(userIdx, postMyAssetReq);

            if(result == 0) {
                throw new BaseException(PURCHASE_FAIL_ASSET);
            }
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 자산 판매
    public Asset saleMyAsset(int userIdx, PostMyAssetReq postMyAssetReq) throws BaseException {
        try {
            Asset asset = userProvider.checkMyAssetIdx(postMyAssetReq);
            // 리스트 상태 변경 Dao
            userDao.changeMyAssetList(userIdx, postMyAssetReq);
            // 판매 transactionIdx를 가장 최신 transactionIdx로 setting
            postMyAssetReq.setTransactionIdx(asset.getTransactionIdx());
            // 자산 판매 Dao
            int result = userDao.saleMyAsset(userIdx, postMyAssetReq);
            if(result == 0) {
                throw new BaseException(SALE_FAIL_ASSET);
            }
            return asset;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 자산 리스트에서 제거
    public void deleteMyAssetList(int userIdx, PostMyAssetReq postMyAssetReq) throws BaseException {
        try {
            int result = userDao.changeMyAssetList(userIdx, postMyAssetReq);
            if(result == 0) {
                throw new BaseException(DELETE_FAIL_ASSET_LIST);
            }
        }catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // TODO : 수정 필요
    // Sales 갱신
    public void updateTableSales(int userIdx, PostMyAssetReq postMyAssetReq, Asset asset, int purchaseTransactionIdx) throws BaseException {
        try {
            long purchasePrice = userProvider.getPrice(purchaseTransactionIdx, postMyAssetReq.getCategory());
            long sellPrice = asset.getPrice();

            // 수익율 계산- (판매가격 - 구매가격) / 구매가격 * 100 %
            double sales = ((sellPrice-purchasePrice)/purchasePrice)*100;

            int result = userDao.updateTableSales(userIdx, sales);
            if(result == 0) {
                throw new BaseException(FAIL_TO_INSERT_SALES);
            }
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
