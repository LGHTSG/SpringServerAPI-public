package site.lghtsg.api.realestates;

import org.springframework.stereotype.Service;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.BaseResponse;
import site.lghtsg.api.realestates.model.RealEstateBox;
import site.lghtsg.api.realestates.model.RealEstateInfo;

import java.util.Collections;
import java.util.List;

import static site.lghtsg.api.config.BaseResponseStatus.DATABASE_ERROR;
import static site.lghtsg.api.config.BaseResponseStatus.REALESTATE_SORTING_ERROR;
import static site.lghtsg.api.config.Constant.ASCENDING_PARAM;

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
        realEstateBoxes.stream().forEach(realEstateBox -> realEstateBox.setRateCalDateDiff("3달전"));
        realEstateBoxes.stream().forEach(realEstateBox -> realEstateBox.setRateOfChange("dummy"));

        return realEstateBoxes;
    }

    // 하나의 box 반환 - 리스트 구성 인자
    public RealEstateBox getRealEstateBox(int realEstateIdx) throws BaseException {
        return null;
    }

    // 하나의 info를 반환 - 테이블 원소 그대로
    public RealEstateInfo getRealEstateInfo(int realEstateIdx) throws BaseException {
        return null;
    }

    /**
     *
     * @param keyword
     * @return regionNames
     * @throws BaseException
     */
    public BaseResponse<List<String>> getRegionNames(String keyword) throws BaseException {
        try {
            List<String> result = realEstateDao.getRegionNames(keyword);

            return new BaseResponse<>(result);
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


}
