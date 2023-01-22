package site.lghtsg.api.resells;


import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.BaseResponseStatus;
import site.lghtsg.api.resells.model.GetResellInfoRes;
import site.lghtsg.api.resells.model.GetResellTransactionRes;
import site.lghtsg.api.resells.model.GetResellBoxRes;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static site.lghtsg.api.config.Constant.ASCENDING_PARAM;

@Service
public class ResellProvider {
    private final site.lghtsg.api.resells.ResellDao resellDao;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ResellProvider(site.lghtsg.api.resells.ResellDao resellDao) {
        this.resellDao = resellDao;
    }

    public List<GetResellBoxRes> getResellBoxesByIdx(String order) throws BaseException {
        try {
            List<GetResellBoxRes> getResellsByRateRes = resellDao.getResellBoxes();
            if (StringUtils.equals(order, ASCENDING_PARAM)) {
                Collections.sort(getResellsByRateRes, comparatorIdxAsc);
            } else {
                Collections.sort(getResellsByRateRes, comparatorIdxDesc);
            }
            return getResellsByRateRes;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public List<GetResellBoxRes> getResellsByRate(String order) throws BaseException {
        try {
            List<GetResellBoxRes> getResellsByRateRes = resellDao.getResellBoxes();
            if (StringUtils.equals(order, ASCENDING_PARAM)) {
                Collections.sort(getResellsByRateRes, comparatorRateAsc);
            } else {
                Collections.sort(getResellsByRateRes, comparatorRateDesc);
            }
            return getResellsByRateRes;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public GetResellInfoRes getResellInfo(int resellIdx) throws BaseException {
        try {
            GetResellInfoRes getResellInfoRes = resellDao.getResellInfo(resellIdx);
            return getResellInfoRes;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public GetResellBoxRes getResellBox(int resellIdx) throws BaseException {
        try {
            GetResellBoxRes getResellBoxRes = resellDao.getResellBox(resellIdx);
            return getResellBoxRes;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }


    public List<GetResellTransactionRes> getResellTransaction(int resellIdx) throws BaseException {
        try {
            List<GetResellTransactionRes> getResellTransactionRes = resellDao.getResellTransaction(resellIdx);
            return getResellTransactionRes;
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    Comparator<GetResellBoxRes> comparatorIdxDesc = new Comparator<GetResellBoxRes>() {
        @Override
        public int compare(GetResellBoxRes a, GetResellBoxRes b) {
            long a1 = a.getResellIdx();
            long b1 = b.getResellIdx();

            if (a1 < b1) {
                return 1;
            } else if (a1 > b1) {
                return -1;
            } else {
                return 0;
            }
        }
    };

    Comparator<GetResellBoxRes> comparatorIdxAsc = new Comparator<GetResellBoxRes>() {
        @Override
        public int compare(GetResellBoxRes a, GetResellBoxRes b) {
            long a1 = a.getResellIdx();
            long b1 = b.getResellIdx();

            if (a1 > b1) {
                return 1;
            } else if (a1 < b1) {
                return -1;
            } else {
                return 0;
            }
        }
    };

    Comparator<GetResellBoxRes> comparatorRateDesc = new Comparator<GetResellBoxRes>() {
        @Override
        public int compare(GetResellBoxRes a, GetResellBoxRes b) {
            double a1 = Double.parseDouble(a.getRateOfChange());
            double b1 = Double.parseDouble(b.getRateOfChange());

            if (a1 < b1) {
                return 1;
            } else if (a1 > b1) {
                return -1;
            } else {
                return 0;
            }
        }
    };

    Comparator<GetResellBoxRes> comparatorRateAsc = new Comparator<GetResellBoxRes>() {
        @Override
        public int compare(GetResellBoxRes a, GetResellBoxRes b) {
            double a1 = Double.parseDouble(a.getRateOfChange());
            double b1 = Double.parseDouble(b.getRateOfChange());

            if (a1 > b1) {
                return 1;
            } else if (a1 < b1) {
                return -1;
            } else {
                return 0;
            }
        }
    };

}
