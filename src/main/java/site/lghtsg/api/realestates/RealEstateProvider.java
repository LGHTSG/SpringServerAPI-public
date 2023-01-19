package site.lghtsg.api.realestates;

import org.springframework.stereotype.Service;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.BaseResponse;
import site.lghtsg.api.realestates.model.RealEstateBox;
import site.lghtsg.api.realestates.model.RealEstateInfo;
import site.lghtsg.api.realestates.model.RealEstateTransactionData;

import java.util.List;

import static site.lghtsg.api.config.BaseResponseStatus.DATABASE_ERROR;

@Service
public class RealEstateProvider {

    private final RealEstateDao realEstateDao;

    public RealEstateProvider(RealEstateDao realEstateDao){
        this.realEstateDao = realEstateDao;
    }

    /**
     * 부동산 리스트 반환
     * @return
     * @throws BaseException
     */
    // 데이터 불러와서 걍 sort에 따라 정렬해주는 함수.
    // 여기 box 에서 rateCalDateDiff, rateOfChange 넣어주기
    // 계산하는 로직 들어가야 함.

    public List<RealEstateBox> getRealEstateBoxes(String sort, String order, String area) throws BaseException {
        List<RealEstateBox> realEstateBoxes;

        // 1. 정렬된 데이터 받아오기
        // 람다로 100개 데이터에 대해 기간 계산하여 추가
        try {
             if(area == null) realEstateBoxes = realEstateDao.getAllRealEstateBox(sort, order);
             else realEstateBoxes = realEstateDao.getRealEstateBoxesInArea(area, sort, order);
        }
        catch(Exception ignored){
            throw new BaseException(DATABASE_ERROR);
        }

        return realEstateBoxes;
    }

    // 하나의 box 반환 - 리스트 구성 인자
    // TODO : updatedAt 추가
    public RealEstateBox getRealEstateBox(long realEstateIdx) throws BaseException {

        // 가지고 있는 realEstateIdx인지 validation 필요
        RealEstateBox realEstateBox;
        try {
            realEstateBox = realEstateDao.getRealEstateBox(realEstateIdx);
        }
        catch(Exception ignored){
            throw new BaseException(DATABASE_ERROR);
        }
        return realEstateBox;
    }

    // 하나의 info를 반환 - 테이블 원소 그대로
    public RealEstateInfo getRealEstateInfo(long realEstateIdx) throws BaseException {
        RealEstateInfo realEstateInfo;
        try {
            realEstateInfo = realEstateDao.getRealEstateInfo(realEstateIdx);
        }
        catch(Exception ignored){
            throw new BaseException(DATABASE_ERROR);
        }
        return realEstateInfo;
    }

    /**
<<<<<<< HEAD
     * 특정 지역 내 부동산 누적 거래 데이터 전체 반환
     * @param area
     * @return
     */
    public List<RealEstateTransactionData> getRealEstatePricesInArea(String area) throws BaseException{
        List<RealEstateTransactionData> realEstateTransactionData;
        try {
            realEstateTransactionData = realEstateDao.getRealEstatePricesInArea(area);
        }
        catch(Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
        return realEstateTransactionData;
    }

    public List<RealEstateTransactionData> getRealEstatePrices(long realEstateIdx) throws BaseException{
        List<RealEstateTransactionData> realEstateTransactionData;
        try {
            realEstateTransactionData = realEstateDao.getRealEstatePrices(realEstateIdx);
        }
        catch(Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
        return realEstateTransactionData;
    }

    /**
     *
     * @param keyword
     * @return regionNames
     * @throws BaseException
     */
    public List<String> getRegionNames(String keyword) throws BaseException {
        List<String> result;
        try {
            if(keyword == null) result = realEstateDao.getAllRegionNames();
            else result = realEstateDao.getRegionNamesWithKeyword(keyword);
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
        return result;
    }

}
