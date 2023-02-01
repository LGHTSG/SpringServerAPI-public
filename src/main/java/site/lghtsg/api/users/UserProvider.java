package site.lghtsg.api.users;

import com.fasterxml.jackson.databind.ser.Serializers;
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

import site.lghtsg.api.config.Secret.Secret;
import site.lghtsg.api.realestates.RealEstateProvider;
import site.lghtsg.api.utils.AES128;
import site.lghtsg.api.utils.JwtService;
import site.lghtsg.api.users.model.*;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class UserProvider {

    private final UserDao userDao;
    private final JwtService jwtService;
    @Autowired
    private RealEstateProvider realEstateProvider;

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
            String jwt = jwtService.createJwt(userIdx);     // userIdx를 바탕으로 jwt 발급
            return new PostLoginRes(userIdx, jwt);

        } else { // 비밀번호가 다르다면 에러메세지를 출력한다.
            throw new BaseException(FAILED_TO_LOGIN);
        }
    }

    // 주식 자산 조회
    public List<GetMyAssetRes> myAsset(int userIdx) throws BaseException {
        try {
            List<GetMyAssetRes> realEstateAsset = userDao.getRealEstateAsset(userIdx);
            realEstateAsset.stream().forEach(GetMyAssetRes -> GetMyAssetRes.setCategory("realestate"));

            List<GetMyAssetRes> resellAsset = userDao.getResellAsset(userIdx);
            resellAsset.stream().forEach(GetMyAssetRes -> GetMyAssetRes.setCategory("resell"));

            List<GetMyAssetRes> stockAsset = userDao.getStockAsset(userIdx);
            stockAsset.stream().forEach(GetMyAssetRes -> GetMyAssetRes.setCategory("stock"));

            stockAsset.addAll(resellAsset);
            stockAsset.addAll(realEstateAsset);
            stockAsset = calculateRateOfChange(stockAsset);

            // updatedAt 기준으로 정렬
            Collections.sort(stockAsset, new ListComparator());

            return stockAsset;
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
    public class ListComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            String testString1 = ((GetMyAssetRes) o1).getUpdatedAt();
            String testString2 = ((GetMyAssetRes) o2).getUpdatedAt();
            return testString1.compareTo(testString2);
        }
    }

    public static List<GetMyAssetRes> calculateRateOfChange(List<GetMyAssetRes> assetList) throws BaseException{
        try {
            double price, s2Price;
            long currentTime, s2DateTime, timeDiff, diffMonth;
            long divideBy = (long) MILLISECONDS * SECONDS * MINUTES * HOURS * DAYS;
            Date s2Date;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (int i = 0, lim = assetList.size(); i < lim; i++) {
                // 거래 기록이 1개만 있는 경우
                if (assetList.get(i).getS2TransactionTime().isEmpty()) {
                    assetList.get(i).setRateOfChange(0.0);
                    assetList.get(i).setRateCalDateDiff(SINGLE_TRANSACTION_HISTORY);
                    continue;
                }
                System.out.println(assetList.get(i).getS2TransactionTime());

                // 증감울 계산
                price = assetList.get(i).getPrice();
                s2Price = assetList.get(i).getS2Price();

                assetList.get(i).setRateOfChange(Math.round((price - s2Price) / s2Price * 100 * 10) / 10.0);

                if (assetList.get(i).getCategory().equals("stock")) {
                    assetList.get(i).setRateCalDateDiff("어제");
                    continue;
                } else if (assetList.get(i).getCategory().equals("resell")) {
                    assetList.get(i).setRateCalDateDiff("이전 거래 대비");
                    continue;
                }

                // 증감율 게산 기간 계산 (부동산 단독 기능)
                s2Date = sdf.parse(assetList.get(i).getS2TransactionTime());

                currentTime = System.currentTimeMillis();
                s2DateTime = s2Date.getTime();

                timeDiff = currentTime - s2DateTime;
                diffMonth = timeDiff / divideBy;
                assetList.get(i).setRateCalDateDiff(processDateDiffOutput(diffMonth));
            }
        } catch (Exception e) {
            throw new BaseException(DATALIST_CAL_RATE_ERROR);
        }
        return assetList;
    }

    /**
     * 사용자 자산 거래 기록 반환
     */
    public List<GetUserTransactionHistoryRes> getUserTransactionHistory(String category, int userIdx, long assetIdx) throws BaseException{
        List<GetUserTransactionHistoryRes> getUserTransactionHistoryRes;

        if(!category.equals("stock") && !category.equals("realestate") && !category.equals("resell")){
            throw new BaseException(WRONG_PARAMETER_INPUT);
        }

        try{
            if(category.equals("stock")){
                getUserTransactionHistoryRes = userDao.getStockTransactionHistory(assetIdx, userIdx);
            } else if(category.equals("realestate")){
                getUserTransactionHistoryRes = userDao.getRealEstateTransactionHistory(assetIdx, userIdx);
            } else {
                getUserTransactionHistoryRes = userDao.getResellTransactionHistory(assetIdx, userIdx);
            }
        }
        catch(Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
        return getUserTransactionHistoryRes;
    }
}
