package site.lghtsg.api.resells;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import site.lghtsg.api.common.model.CompareByIdx;
import site.lghtsg.api.common.model.CompareByRate;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.BaseResponseStatus;
import site.lghtsg.api.realestates.model.RealEstateBox;
import site.lghtsg.api.resells.model.GetResellInfoRes;
import site.lghtsg.api.resells.model.GetResellTransactionRes;
import site.lghtsg.api.resells.model.GetResellBoxRes;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static site.lghtsg.api.config.BaseResponseStatus.*;
import static site.lghtsg.api.config.Constant.*;

@Service
public class ResellProvider {
    private final site.lghtsg.api.resells.ResellDao resellDao;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * @brief [Dao 로부터 반환받는 값 에러 처리 규칙]
     * 1. Dao 에서 jdbcTemplate.query 사용하여 List<> 로 반환받는 경우 : List.size() == 0 으로 반환값 존재 여부 판단
     * 2. Dao 에서 jdbcTemplate.queryForObject 사용하여 객체로 반환받는 경우 : 객체 == null 으로 존재 여부 판단
     *    (Dao 에서 queryForObject 사용 시 IncorrectResultSizeDataAccessException 발생하면 null 반환하도록 하였음)
     * 3. 그 이외 에러 (sql 문 에러 등) : BaseException(DATABASE_ERROR) 반환
     */

    public ResellProvider(site.lghtsg.api.resells.ResellDao resellDao) {
        this.resellDao = resellDao;
    }

    public List<GetResellBoxRes> getResellBoxes(String sort, String order) throws BaseException {
        List<GetResellBoxRes> getResellBoxesRes;
        try {
            getResellBoxesRes = resellDao.getResellBoxes();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }

        calculateResellBoxesPriceAndRateOFChange(getResellBoxesRes); // 증감율 계산
        sortResellBoxesRes(getResellBoxesRes, sort, order);          // 정렬

        // 요청한 데이터가 없는 경우
        if (getResellBoxesRes.size() == 0) {
            throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
        }
        return getResellBoxesRes;
    }

    public void calculateResellBoxesPriceAndRateOFChange(List<GetResellBoxRes> getResellBoxesRes) throws BaseException {
        try {
            for (GetResellBoxRes getResellBoxRes : getResellBoxesRes) {
                Long lastPrice = getResellBoxRes.getPrice();
                Long s2LastPrice = getResellBoxRes.getLastPrice();
                Double rateOfChange = (double) (lastPrice - s2LastPrice) / s2LastPrice * 100;
                rateOfChange = Math.round(rateOfChange * 10) / 10.0;
                getResellBoxRes.setRateOfChange(rateOfChange);
            }
        }
        catch(Exception e){
            throw new BaseException(DATALIST_CAL_RATE_ERROR);
        }
    }

    public void sortResellBoxesRes(List<GetResellBoxRes> resellBoxesRes, String sort, String order) throws BaseException {
        if (!order.equals(PARAM_DEFAULT) && !order.equals(DESCENDING_PARAM) && !order.equals(ASCENDING_PARAM)){    // 기준이 없는(잘못입력) 경우
            throw new BaseException(INCORRECT_REQUIRED_ARGUMENT);
        }

        // 2. sort 값 validation & comparator 초기화
        Comparator comparator;
        if(sort.equals(PARAM_DEFAULT)) {
            comparator = new CompareByIdx(order);
        } else if (sort.equals(SORT_FLUCTUATION_PARAM)) {
            comparator = new CompareByRate(order);
        } else {
            throw new BaseException(INCORRECT_REQUIRED_ARGUMENT);
        }

        // 3. 정렬
        try {
            resellBoxesRes.sort(comparator);
        } catch (Exception e) {
            throw new BaseException(DATALIST_SORTING_ERROR);
        }
    }

    public GetResellInfoRes getResellInfo(long resellIdx) throws BaseException {
        GetResellInfoRes getResellInfoRes;
        try {
            getResellInfoRes = resellDao.getResellInfo(resellIdx);
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
        if (getResellInfoRes == null) throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
        calculateResellInfoResPriceAndRateOFChange(getResellInfoRes);

        return getResellInfoRes;
    }

    public GetResellInfoRes calculateResellInfoResPriceAndRateOFChange(GetResellInfoRes getResellInfoRes) throws BaseException {
        try{
            Long lastPrice = getResellInfoRes.getPrice();
            Long s2LastPrice = getResellInfoRes.getLastPrice();
            Double rateOfChange = (double) (lastPrice - s2LastPrice) / s2LastPrice * 100;
            rateOfChange = Math.round(rateOfChange * 10) / 10.0;
            getResellInfoRes.setRateOfChange(rateOfChange);
            return getResellInfoRes;
        }

        catch(Exception e){
            throw new BaseException(DATALIST_CAL_RATE_ERROR);
        }
    }

    public GetResellBoxRes getResellBox(long resellIdx) throws BaseException {
        GetResellBoxRes getResellBoxRes;
        try {
            getResellBoxRes = resellDao.getResellBox(resellIdx);
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
        if (getResellBoxRes == null) throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
        return getResellBoxRes;
    }

    public List<GetResellTransactionRes> getResellTransaction(long resellIdx) throws BaseException {
        List<GetResellTransactionRes> getResellTransactionRes;
        try {
            getResellTransactionRes = resellDao.getResellTransaction(resellIdx);
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
        if (getResellTransactionRes.size() == 0) throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
        return getResellTransactionRes;
    }
}