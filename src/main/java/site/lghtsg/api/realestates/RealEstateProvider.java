package site.lghtsg.api.realestates;

import org.springframework.stereotype.Service;
import site.lghtsg.api.common.model.*;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.realestates.model.RealEstateBox;
import site.lghtsg.api.realestates.model.RealEstateInfo;
import site.lghtsg.api.realestates.model.RealEstateRemains;
import site.lghtsg.api.realestates.model.RealEstateTransactionData;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;

import static site.lghtsg.api.config.BaseResponseStatus.*;
import static site.lghtsg.api.config.Constant.*;

@Service
public class RealEstateProvider {

    private final RealEstateDao realEstateDao;

    public RealEstateProvider(RealEstateDao realEstateDao) {
        this.realEstateDao = realEstateDao;
    }

    /**
     * ==========================================================================================
     * 부동산 리스트 반환
     * @return List<RealEstateBox>
     * @throws BaseException
     */

    public List<RealEstateBox> getRealEstateBoxes(String sort, String order, String area) throws BaseException {
        List<RealEstateBox> realEstateBoxes;
        // 1. 데이터 가져오기
        try {
            if(area.equals(PARAM_DEFAULT)) realEstateBoxes = realEstateDao.getAllRealEstateBoxes();
            else realEstateBoxes = realEstateDao.getRealEstateBoxesInArea(area);
        }
        catch (Exception ignored) {
            throw new BaseException(DATABASE_ERROR);
        }
        if(realEstateBoxes.size() == 0) throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
        // 2. 증감율 계산
        realEstateBoxes = calculateRateOfChange(realEstateBoxes);

        // 2. 정렬
        realEstateBoxes = sortRealEstateBoxes(realEstateBoxes, sort, order);

        // 출력 wrapping - 추후 고려
        return realEstateBoxes;
    }

    /**
     * ==========================================================================================
     * TODO : 정렬 로직을 클래스에 내포시키는게 깔끔
        -> List<RealEstateBox>를 하나의 클래스로 덮어야 하는데 굳이 그럴까 싶어 고려중
        TODO : 2. var 입력값 없는 경우에 대해 validation 처리를 한꺼번에 하긴 하는데... 코드가 너무 난잡해진 기분.
                입력이 꼭 와야하는 필수 요소 (입력이 잘못된 경우) -> 이런 validation 은 controller 에서 컷 하고,
                여기에서는 걍 에러처리를 안하는게 맞지 않니.
     * RealEstateBoxes 리스트를 정렬 기준에 맞게 정렬 후 반환
     * @param realEstateBoxes
     * @param sort
     * @param order
     * @return List<RealEstateBox>
     * @throws BaseException
     */
    static List<RealEstateBox> sortRealEstateBoxes(List<RealEstateBox> realEstateBoxes, String sort, String order) throws BaseException {
        // 1. order 값 validation
        if (!order.equals(PARAM_DEFAULT) && !order.equals(DESCENDING_PARAM) && !order.equals(ASCENDING_PARAM)){    // 기준이 없는(잘못입력) 경우
            throw new BaseException(INCORRECT_REQUIRED_ARGUMENT);
        }

        // 2. sort 값 validation & comparator 초기화
        Comparator comparator = new CompareByIdx(order);
        if (sort.equals(SORT_FLUCTUATION_PARAM)){   // 증감율 기준
            comparator = new CompareByRate(order);
        } else if (sort.equals(SORT_PRICE_PARAM)) { // 가격 기준
            comparator = new CompareByPrice(order);
        } else if(!sort.equals(PARAM_DEFAULT)){     // 기준이 없는(잘못입력) 경우
            throw new BaseException(INCORRECT_REQUIRED_ARGUMENT);
        }

        // 3. 정렬
        try {
            realEstateBoxes.sort(comparator);
        }
        catch(Exception e) {
            throw new BaseException(DATALIST_SORTING_ERROR);
        }
        return realEstateBoxes;
    }

    static List<RealEstateBox> calculateRateOfChange(List<RealEstateBox> realEstateBoxes) throws BaseException {
        try {
            double price, s2Price;
            long currentTime, s2DateTime, timeDiff, diffMonth;
            long divideBy = (long)MILLISECONDS * SECONDS * MINUTES * HOURS * DAYS;
            Date s2Date;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (int i = 0, lim = realEstateBoxes.size(); i < lim; i++) {
                // 거래 기록이 1개만 있는 경우
                if(realEstateBoxes.get(i).getS2TransactionTime() == null){
                    realEstateBoxes.get(i).setRateOfChange(0.0);
                    realEstateBoxes.get(i).setRateCalDateDiff(SINGLE_TRANSACTION_HISTORY);
                    continue;
                }

                // 증감울 계산
                price = realEstateBoxes.get(i).getPrice();
                s2Price = realEstateBoxes.get(i).getS2Price();

                realEstateBoxes.get(i).setRateOfChange(Math.round((price - s2Price) / s2Price * 100 * 10) / 10.0);

                // 증감율 게산 기간 계산 (부동산 단독 기능)
                s2Date = sdf.parse(realEstateBoxes.get(i).getS2TransactionTime());

                currentTime = System.currentTimeMillis();
                s2DateTime = s2Date.getTime();

                timeDiff = currentTime - s2DateTime;
                diffMonth = timeDiff / divideBy;
                realEstateBoxes.get(i).setRateCalDateDiff(processDateDiffOutput(diffMonth));
            }
        }
        catch(Exception e){
            throw new BaseException(DATALIST_CAL_RATE_ERROR);
        }
        return realEstateBoxes;
    }

    public static String processDateDiffOutput(long diffMonth){
        if(diffMonth <= 3) return WITHIN_3_MONTHS;
        else if(diffMonth <= 6) return WITHIN_6_MONTHS;
        else if(diffMonth <= 12) return WITHIN_1_YEAR;
        else return MORE_THAN_1_YEAR;
    }

    /**
     * ==========================================================================================
     * TODO : 메모리때문에 새로운 리스트 생성은 못하고, 일단은 현재 방식 전달 유지
     * 프론트 전달할 때 필요없는 데이터 제거 - 모든 파트 구분없이 Box 헝태로 Wrapping 해 제공
     * @param realEstateBoxes
     * @return
     */
    static List<Box> outputWrapper(List<RealEstateBox> realEstateBoxes){
        return null;
    }

    static RealEstateInfo boxToInfoWrapper(RealEstateBox realEstateBox){
        return new RealEstateInfo(
                realEstateBox.getIdx(),
                realEstateBox.getName(),
                realEstateBox.getRateOfChange(),
                realEstateBox.getRateCalDateDiff(),
                realEstateBox.getIconImage(),
                realEstateBox.getPrice());
    }

    /**
     * ==========================================================================================
     * 하나의 부동산 정보를 반환
     * @param realEstateIdx
     * @return RealEstateInfo
     * @throws BaseException
     */
    public RealEstateInfo getRealEstateInfo(long realEstateIdx) throws BaseException {
        // 가지고 있는 realEstateIdx 인지 validation - REQUESTED_DATA_FAIL_TO_EXIST
        RealEstateInfo realEstateInfo;
        List<RealEstateBox> realEstateBoxes = new ArrayList<>();
        try {
             realEstateBoxes.add(realEstateDao.getRealEstateBox(realEstateIdx));
            if(realEstateBoxes.size() == 0) throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);

            // 데이터 없는 경우 처리했으니 리스트로 계산 후 반환
            realEstateBoxes = calculateRateOfChange(realEstateBoxes);
            realEstateInfo = boxToInfoWrapper(realEstateBoxes.get(0));
        }
        catch(Exception ignored){
            System.out.println(ignored.getMessage());
            throw new BaseException(DATABASE_ERROR);
        }
        return realEstateInfo;
    }

    /**
     * ==========================================================================================
     * 특정 지역 내 부동산 누적 거래 데이터 전체 반환
     * @param area
     * @return List<RealEstateTransactionData>
     */
    public List<RealEstateTransactionData> getAreaRealEstatePrices(String area) throws BaseException{
        List<RealEstateTransactionData> realEstateTransactionData;
        try {
            realEstateTransactionData = realEstateDao.getRealEstatePricesInArea(area);
            if(realEstateTransactionData.size() == 0) throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
        }
        catch(Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
        return realEstateTransactionData;
    }

    /**
     * ==========================================================================================
     * 특정 부동산 거래 데이터 반환
     * @param realEstateIdx
     * @return List<RealEstateTransactionData>
     * @throws BaseException
     */
    public List<RealEstateTransactionData> getRealEstatePrices(long realEstateIdx) throws BaseException{
        List<RealEstateTransactionData> realEstateTransactionData;
        try {
            realEstateTransactionData = realEstateDao.getRealEstatePrices(realEstateIdx);
            if(realEstateTransactionData.size() == 0) throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
        }
        catch(Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
        return realEstateTransactionData;
    }

    /**
     * ==========================================================================================
     * @param keyword
     * @return regionNames
     * @throws BaseException
     */
    public List<String> getRegionNames(String keyword) throws BaseException {
        List<String> regionNames;
        try {
            if(keyword.equals(PARAM_DEFAULT)) regionNames = realEstateDao.getAllRegionNames();
            else regionNames = realEstateDao.getRegionNamesWithKeyword(keyword);
            if(regionNames.size() == 0) throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
        }
        catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
        return regionNames;
    }
    // 매일 업데이트 되는 부동산의 거래 기록을 가져와 각 지역별 같은 날 1개의 가격만 존재할 수 있도록 처리한다.
    // 동 단위는 불러와서 처리하고, 구, 시 단위는 해당 테이블에 미리 계산 처리해둔다.
    // 데이터 초기화 용
    public void areaPriceCacheUploader() throws BaseException{
        // 1. 가격 데이터를 불러온다
        List<String> areaList = new ArrayList<>();
        try {
            String path = "src/main/java/site/lghtsg/api/common/AreaPriceCacheList.txt";
            String line = "";
            System.out.println("check");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));

            while( (line = bufferedReader.readLine()) != null ){
                areaList.add(line);
            }
        }catch(Exception e){
            // throw new
            throw new BaseException(FILE_READ_ERROR);
        }

        // 쿼리문을 어떻게 날릴지...
        // 일단 과거 데이터만 올리고 자동업로드는 추가로 하자
        // 1. 각 지역별로 데이터 가지고 오기 / 가지고 온 데이터로 값 초기화

        int test_lim = 10; // 테스트 용 지역 길이 제한 - 서울시 다음부터 시작
        // 테이블 값 업데이트만 남았음
        for (int i = 10, ilim = areaList.size(); i < ilim; i++) {
            String area = areaList.get(i);
            List<RealEstateTransactionData> prices = realEstateDao.getRealEstatePricesInArea(area);
            if(prices.size() == 0) throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);

            // 시간 순 정렬
            prices.sort(Comparator.comparing(TransactionData::getDatetime));
            // 시간 순 중복 제거 (평균) - 가격 평균을...
            // 날짜가 바뀌는 시점 누적한 평균값을 리스트에 추가한다.
            // 비교용 마지막 더미 데이터
            RealEstateTransactionData lastData = new RealEstateTransactionData();
            lastData.setDatetime("THIS-IS-NOT");
            prices.add(lastData);
            String[] area_div = area.split("_");
            System.out.println(area_div.length);
            int cnt_minimum = getCountMinimum(area_div.length);

            List<RealEstateTransactionData> result = new ArrayList<>();
            long cnt = 0, priceSum = 0;
            String now = "", next = "";
            for(int j = 0, jlim = prices.size(); j < jlim - 1; j++){
                now = prices.get(j).getDatetime();
                next = prices.get(j + 1).getDatetime();

                priceSum += prices.get(j).getPrice();
                cnt += 1;

                if(now.compareTo(next) != 0) {
                    RealEstateTransactionData data = new RealEstateTransactionData();
                    data.setDatetime(now);
                    if(cnt <= cnt_minimum) priceSum = 0; // 거래 기록이 너무 적을 경우 평균값과 멀어지므로 0으로 가격을 무효시킨다
                    data.setPrice(priceSum / cnt);
                    result.add(data);
                    cnt = 0;
                    priceSum = 0;
                }
            }

            // 서울시에 대해서는 계산 끝. 테이블 초기화도 완료
//            if(i == 0) {
//                realEstateDao.initAreaPriceCacheRow(result, area);
//                continue;
//            }

            // 서울 지역 row 초기화
//            try {
//                realEstateDao.initAreaPriceCacheRow(result, area);
//            }catch(Exception e ){
//                throw new BaseException(DATABASE_ERROR);
//            }

            // 데이터 업데이트 파트 - 각 일자의 가격마다 업데이트 쿼리를 짜서 날려야한다.

            List<RealEstateRemains> remains = new ArrayList<>();
            for(RealEstateTransactionData r : result) {
                try{
                    // 해당 row가 존재한다면
                    realEstateDao.checkDateExists(r);
                    // 기존 row에 추가
                    realEstateDao.updateAreaCacheTable(r, area);
                }catch(Exception e){ // 존재하지 않는다면
                    // 에러 발생시 잡을 수 있는가? - 안될듯
                    try {
                        realEstateDao.insertAreaCacheTable(r, area);
                    }
                    catch(Exception e2){
                        RealEstateRemains rm = new RealEstateRemains();
                        rm.setArea(area);
                        rm.setDatetime(r.getDatetime());
                        rm.setPrice(r.getPrice());
                        remains.add(rm);
                    }
                }
            }

            for(RealEstateRemains rm : remains){
                System.out.println(rm.getArea() + " / " + rm.getDatetime() + " : " + rm.getPrice());
            }


            // 아직 null 과 0이 왜 동시에 존재히는지 파악하지 못했다.

        }
    }

    static int getCountMinimum(int area_level){
        if(area_level >= 2) return 0;
        return 4;
    }
}
