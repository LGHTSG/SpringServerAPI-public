package site.lghtsg.api.users;

import org.apache.commons.collections4.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.lghtsg.api.config.BaseException;
import static site.lghtsg.api.config.BaseResponseStatus.*;
import static site.lghtsg.api.config.Constant.*;
import static site.lghtsg.api.config.Constant.SINGLE_TRANSACTION_HISTORY;
import static site.lghtsg.api.realestates.RealEstateProvider.processDateDiffOutput;

import site.lghtsg.api.config.BaseResponse;
import site.lghtsg.api.config.Secret.Secret;
import site.lghtsg.api.utils.AES128;
import site.lghtsg.api.utils.JwtService;
import site.lghtsg.api.users.model.*;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class UserProvider {

    private final UserDao userDao;
    private final JwtService jwtService;

    final Logger loger = LoggerFactory.getLogger(this.getClass());

    /**
     * @brief [Dao 로부터 반환받는 값 에러 처리 규칙]
     * 1. Dao 에서 jdbcTemplate.query 사용하여 List<> 로 반환받는 경우 : List.size() == 0 으로 반환값 존재 여부 판단
     * 2. Dao 에서 jdbcTemplate.queryForObject 사용하여 객체로 반환받는 경우 : 객체 == null 으로 존재 여부 판단
     *    (Dao 에서 queryForObject 사용 시 IncorrectResultSizeDataAccessException 발생하면 null 반환하도록 하였음)
     * 3. 그 이외 에러 (sql 문 에러 등) : BaseException(DATABASE_ERROR) 반환
     */

    @Autowired
    public UserProvider(UserDao userDao, JwtService jwtService) {
        this.userDao = userDao;
        this.jwtService = jwtService;
    }

    // 보유 현금 조회
    public Long myCash(int userIdx) throws BaseException {
        long totalCash;
        try {
            totalCash = userDao.getCurrentCash(userIdx);
            return totalCash;
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 자산의 총 현재가치 조회(현금 포함)
    public Long getMyValueOfAssets(int userIdx) throws BaseException {
        try {
            long totalValue = 0;

            // 현금
            totalValue += userDao.getCurrentCash(userIdx);

            // 부동산, 주식, 리셀 (현재가)
            totalValue += userDao.getRealEstateAssetPrices(userIdx);
            totalValue += userDao.getStockAssetPrices(userIdx);
            totalValue += userDao.getResellAssetPrices(userIdx);

            return totalValue;
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
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
        User user;
        try {
            userDao.checkEmail(postLoginReq.getEmail());
            user = userDao.getPassword(postLoginReq);
        } catch(Exception e){
            throw new BaseException(FAILED_TO_LOGIN);
        }
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
            String accessToken = jwtService.createAccessToken(userIdx);     // userIdx를 accessToken 발급
            String refreshToken = jwtService.createRefreshToken(userIdx);   // userIdx로 refreshToken 발급
            return new PostLoginRes(userIdx, accessToken, refreshToken);

        } else { // 비밀번호가 다르다면 에러메세지를 출력한다.
            throw new BaseException(FAILED_TO_LOGIN);
        }
    }

    // 사용자 profile 이미지 url 전송
    public GetProfileImgRes getUserImageUrl(int userIdx) throws BaseException{
        GetProfileImgRes getProfileImgRes;
        try{
            getProfileImgRes = userDao.getUserProfileImgUrl(userIdx);
        }catch(Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
        if(getProfileImgRes == null) throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
        return getProfileImgRes;
    }

    // 사용자 수익율 조회
    public GetUserROERes getUserROERes(int userIdx) throws BaseException{
        GetUserROERes getUserROERes;
        try{
            getUserROERes = userDao.getUserROERes(userIdx);
        }catch(Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
        if(getUserROERes == null){
            throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
        }
        return getUserROERes;
    }

    // 사용자 개인정보 조회
    public GetUserInfo getUserInfo(int userIdx) throws BaseException {
        GetUserInfo getUserInfo;
        try{
            getUserInfo = userDao.getUserInfo(userIdx);
        }
        catch(Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
        if(getUserInfo == null) {
            throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
        }
        return getUserInfo;
    }

    // 주식 자산 조회
    public List<GetMyAssetRes> myAsset(int userIdx) throws BaseException {
        try {
            List<GetMyAssetRes> realEstateAsset = userDao.getRealEstateAsset(userIdx);
            realEstateAsset.forEach(GetMyAssetRes -> GetMyAssetRes.setCategory(ASSET_CATEGORY_REALESTATE));
            System.out.println("realEstate");

            List<GetMyAssetRes> resellAsset = userDao.getResellAsset(userIdx);
            resellAsset.forEach(GetMyAssetRes -> GetMyAssetRes.setCategory(ASSET_CATEGORY_RESELL));
            System.out.println("resell");

            List<GetMyAssetRes> stockAsset = userDao.getStockAsset(userIdx);
            stockAsset.forEach(GetMyAssetRes -> GetMyAssetRes.setCategory(ASSET_CATEGORY_STOCK));
            System.out.println("stock");

            stockAsset.addAll(resellAsset);
            stockAsset.addAll(realEstateAsset);
            calculateRateOfChange(stockAsset);

            System.out.println("check");
            // updatedAt 기준으로 정렬
            // 다른 Provider 에서는 sort 메서드를 따로 만들어서 자체적으로 BaseException 을 던졌다.
            // 얘는 굳이 메서드를 둘 필요가 없어 직접 BaseException 을 던지도록 했다.
            try {
                stockAsset.sort(new AssetComparator());
            }catch(Exception e) {
                throw new BaseException(DATALIST_SORTING_ERROR);
            }
            // 나의 자산은 자산의 없는 상태의 화면도 떠야 함으로 에러로 처리하지 않는다.
//            if(stockAsset.size() == 0){
//                throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
//            }
            return stockAsset;

        } catch(BaseException be) {
            throw be;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 구매하려는 자산의 과거 거래 이력을 가져옴
     * @param userIdx
     * @param postMyAssetReq
     * @return
     * @throws BaseException
     */
    public Asset getPreviousTransaction(int userIdx, PostMyAssetReq postMyAssetReq) throws BaseException {
        List<Asset> result;
        try {
            result = userDao.getPreviousTransaction(userIdx, postMyAssetReq);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
        // 이전 거래가 없다면
        if(result.size() == 0){
            throw new BaseException(NO_PREVIOUS_USER_TRANSACTION);
        } else if(result.size() > 1){ // transactionStatus == 1이 2개 이상이라면
            throw new BaseException(USER_TRANSACTION_DATA_ERROR);
        }
        return result.get(0);
    }


/*
    // 수익율 계산
    public double getProfitRate(PostMyAssetReq postMyAssetReq) throws BaseException {
        try {
            int price = userDao.getPriceOfAsset(postMyAssetReq);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
*/
    // 정렬을 위한 class
    static class AssetComparator implements Comparator<GetMyAssetRes> {
        @Override
        public int compare(GetMyAssetRes o1, GetMyAssetRes o2) {
            String testString1 = o1.getUpdatedAt();
            String testString2 = o2.getUpdatedAt();
            return testString1.compareTo(testString2);
        }
    }

    public static void calculateRateOfChange(List<GetMyAssetRes> assetList) throws BaseException{
        try {
            double price, s2Price;
            long currentTime, s2DateTime, timeDiff, diffMonth;
            long divideBy = (long) MILLISECONDS * SECONDS * MINUTES * HOURS * DAYS;
            Date s2Date;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (GetMyAssetRes getMyAssetRes : assetList) {
                // 거래 기록이 1개만 있는 경우
                if (getMyAssetRes.getS2TransactionTime().isEmpty()) {
                    getMyAssetRes.setRateOfChange(0.0);
                    getMyAssetRes.setRateCalDateDiff(SINGLE_TRANSACTION_HISTORY);
                    continue;
                }
                System.out.println(getMyAssetRes.getS2TransactionTime());

                // 증감울 계산
                price = getMyAssetRes.getPrice();
                s2Price = getMyAssetRes.getS2Price();

                getMyAssetRes.setRateOfChange(Math.round((price - s2Price) / s2Price * 100 * 10) / 10.0);

                if (getMyAssetRes.getCategory().equals("stock")) {
                    getMyAssetRes.setRateCalDateDiff("어제");
                    continue;
                } else if (getMyAssetRes.getCategory().equals("resell")) {
                    getMyAssetRes.setRateCalDateDiff("이전 거래 대비");
                    continue;
                }

                // 증감율 게산 기간 계산 (부동산 단독 기능)
                s2Date = sdf.parse(getMyAssetRes.getS2TransactionTime());

                currentTime = System.currentTimeMillis();
                s2DateTime = s2Date.getTime();

                timeDiff = currentTime - s2DateTime;
                diffMonth = timeDiff / divideBy;
                getMyAssetRes.setRateCalDateDiff(processDateDiffOutput(diffMonth));
            }
        } catch (Exception e) {
            throw new BaseException(DATALIST_CAL_RATE_ERROR);
        }
    }

    /**
     * 사용자 자산 거래 기록 반환
     */
    public List<GetUserTransactionHistoryRes> getUserTransactionHistory(String category, int userIdx, long assetIdx) throws BaseException{
        List<GetUserTransactionHistoryRes> getUserTransactionHistoryRes;

        try{
            if(category.equals(ASSET_CATEGORY_STOCK)){
                getUserTransactionHistoryRes = userDao.getStockTransactionHistory(assetIdx, userIdx);
            } else if(category.equals(ASSET_CATEGORY_REALESTATE)){
                getUserTransactionHistoryRes = userDao.getRealEstateTransactionHistory(assetIdx, userIdx);
            } else if (category.equals(ASSET_CATEGORY_RESELL)){
                getUserTransactionHistoryRes = userDao.getResellTransactionHistory(assetIdx, userIdx);
            } else throw new BaseException(WRONG_PARAMETER_INPUT);
        } catch(BaseException be) {
            throw be;
        } catch(Exception e){
            throw new BaseException(DATABASE_ERROR);
        }

        if(getUserTransactionHistoryRes.size() == 0) throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
        return getUserTransactionHistoryRes;
    }

    /**
     * 현재 등록되어 있는 사용자 리스트 반환
     */
    public List<GetUserInfoRes> getUserList() throws BaseException {
        try{
            return userDao.getUserInfoList();
        }
        catch(Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
