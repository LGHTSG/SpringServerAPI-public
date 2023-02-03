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
        calculateResellBoxesPriceAndRateOFChange(getResellBoxesRes);
        sortResellBoxesRes(getResellBoxesRes, sort, order);
        if (getResellBoxesRes == null) {
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
        try {
            GetResellInfoRes getResellInfoRes = resellDao.getResellInfo(resellIdx);
            getResellInfoRes = calculateResellInfoResPriceAndRateOFChange(getResellInfoRes);
            if (getResellInfoRes == null) {
                throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
            }
            return getResellInfoRes;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public GetResellInfoRes calculateResellInfoResPriceAndRateOFChange(GetResellInfoRes getResellInfoRes) {

        Long lastPrice = getResellInfoRes.getPrice();
        Long s2LastPrice = getResellInfoRes.getLastPrice();
        Double rateOfChange = (double) (lastPrice - s2LastPrice) / s2LastPrice * 100;
        rateOfChange = Math.round(rateOfChange * 10) / 10.0;
        getResellInfoRes.setRateOfChange(rateOfChange);

        return getResellInfoRes;
    }

    public GetResellBoxRes getResellBox(long resellIdx) throws BaseException {
        try {
            GetResellBoxRes getResellBoxRes = resellDao.getResellBox(resellIdx);
            if (getResellBoxRes == null) {
                throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
            }
            return getResellBoxRes;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public List<GetResellTransactionRes> getResellTransaction(long resellIdx) throws BaseException {
        try {
            List<GetResellTransactionRes> getResellTransactionRes = resellDao.getResellTransaction(resellIdx);
            if (getResellTransactionRes == null) {
                throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
            }
            return getResellTransactionRes;
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
}