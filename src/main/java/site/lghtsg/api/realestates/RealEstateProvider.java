package site.lghtsg.api.realestates;

import org.springframework.stereotype.Service;
import site.lghtsg.api.common.model.Box;
import site.lghtsg.api.common.model.CompareByPrice;
import site.lghtsg.api.common.model.CompareByRate;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.realestates.model.RealEstateBox;
import site.lghtsg.api.realestates.model.RealEstateInfo;
import site.lghtsg.api.realestates.model.RealEstateTransactionData;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

        // 2. 정렬
        // sortRealEstateBoxes() - Throws BaseException
        realEstateBoxes = sortRealEstateBoxes(realEstateBoxes, sort, order);

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
        try {
            // 1. 정렬
            if (sort.equals(SORT_FLUCTUATION_PARAM)){   // 증감율 기준
                realEstateBoxes.sort(new CompareByRate());
            } else if (sort.equals(SORT_PRICE_PARAM)) { // 가격 기준
                realEstateBoxes.sort(new CompareByPrice());
            } else if(!sort.equals(PARAM_DEFAULT)){     // 기준이 없는(잘못입력) 경우
                throw new BaseException(INCORRECT_REQUIRED_ARGUMENT);
            }

            // 2. 차순
            if (order.equals(ASCENDING_PARAM)) {        // 오름차순
                Collections.reverse(realEstateBoxes);
            } else if (!order.equals(PARAM_DEFAULT) && !order.equals(DESCENDING_PARAM)){    // 기준이 없는(잘못입력) 경우
                throw new BaseException(INCORRECT_REQUIRED_ARGUMENT);
            }

            return realEstateBoxes;
        }
        catch(Exception e) {
            throw new BaseException(DATALIST_SORTING_ERROR);
        }
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
        try {
             realEstateInfo = realEstateDao.getRealEstateInfo(realEstateIdx);
            if(realEstateInfo == null) throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
        }
        catch(Exception ignored){
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
            if(realEstateTransactionData == null) throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
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
            if(realEstateTransactionData == null) throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
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
        try {
            if(keyword.equals(PARAM_DEFAULT)) return realEstateDao.getAllRegionNames();
            else return realEstateDao.getRegionNamesWithKeyword(keyword);
        }
        catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
