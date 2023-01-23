package site.lghtsg.api.resells;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import site.lghtsg.api.common.model.CompareByIdx;
import site.lghtsg.api.common.model.CompareByRate;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.BaseResponseStatus;
import site.lghtsg.api.resells.model.GetResellInfoRes;
import site.lghtsg.api.resells.model.GetResellTransactionRes;
import site.lghtsg.api.resells.model.GetResellBoxRes;

import java.util.ArrayList;
import java.util.Collections;
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
        try {
            List<GetResellBoxRes> getResellBoxesRes = resellDao.getResellBoxes();
            getResellBoxesRes = calculateResellBoxesPriceAndRateOFChange(getResellBoxesRes);
            getResellBoxesRes = sortResellBoxesRes(getResellBoxesRes, sort, order);
            if(getResellBoxesRes == null){
                throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
            }
            return getResellBoxesRes;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public List<GetResellBoxRes> calculateResellBoxesPriceAndRateOFChange(List<GetResellBoxRes> getResellBoxesRes) {
        for (GetResellBoxRes getResellBoxRes : getResellBoxesRes) {
            List<Object> result = calculateChangeOfRate(getResellBoxRes.getIdx());
            Double rateOfChange = Double.parseDouble(String.valueOf(result.get(1)));
            Long price = Long.parseLong(String.valueOf(result.get(0)));
            getResellBoxRes.setPrice(price);
            getResellBoxRes.setRateOfChange(rateOfChange);
        }
        return getResellBoxesRes;
    }

    public List<GetResellBoxRes> sortResellBoxesRes(List<GetResellBoxRes> resellBoxesRes, String sort, String order) throws BaseException {

        try {
            if (sort.equals(PARAM_DEFAULT)) {
                resellBoxesRes.sort(new CompareByIdx());
            } else if (sort.equals(SORT_FLUCTUATION_PARAM)) {
                resellBoxesRes.sort(new CompareByRate());
            } else if (!sort.equals(PARAM_DEFAULT)) {
                throw new BaseException(INCORRECT_REQUIRED_ARGUMENT);
            }

            if (order.equals(ASCENDING_PARAM)) {
                Collections.reverse(resellBoxesRes);
            } else if (!order.equals(PARAM_DEFAULT) && !order.equals(DESCENDING_PARAM)) {
                throw new BaseException(INCORRECT_REQUIRED_ARGUMENT);
            }

            return resellBoxesRes;
        } catch (Exception e) {
            throw new BaseException(DATALIST_SORTING_ERROR);
        }

    }

    public GetResellInfoRes getResellInfo(long resellIdx) throws BaseException {
        try {
            GetResellInfoRes getResellInfoRes = resellDao.getResellInfo(resellIdx);
            if (getResellInfoRes == null) {
                throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
            }
            return getResellInfoRes;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public GetResellBoxRes getResellBox(long resellIdx) throws BaseException {
        try {
            GetResellBoxRes getResellBoxRes = resellDao.getResellBox(resellIdx);
            if(getResellBoxRes == null){
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
            if(getResellTransactionRes == null){
                throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
            }
            return getResellTransactionRes;
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public List<Object> calculateChangeOfRate(long resellIdx) {
        List<Integer> resellTransactionHistory = resellDao.getResellTransactionForPriceAndRateOfChange(resellIdx);
        List<Object> result = new ArrayList<>();
        int currentPrice = resellTransactionHistory.get(0);
        int latestPrice = resellTransactionHistory.get(1);
        double changeOfRate = (double) (currentPrice - latestPrice) / latestPrice * 100;
        changeOfRate = Math.round(changeOfRate);
        result.add(currentPrice);
        result.add(changeOfRate);
        return result;
    }
}