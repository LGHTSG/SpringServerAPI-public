package site.lghtsg.api.resells;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.BaseResponseStatus;
import site.lghtsg.api.resells.model.GetResellRes;
import site.lghtsg.api.resells.model.GetResellTransactionRes;
import site.lghtsg.api.resells.model.ResellBox;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class ResellProvider {
    private final site.lghtsg.api.resells.ResellDao resellDao;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ResellProvider(site.lghtsg.api.resells.ResellDao resellDao) {
        this.resellDao = resellDao;
    }

    public List<GetResellRes> getResells(String order) throws BaseException {
        try {
            List<GetResellRes> getResellsRes = resellDao.getResells(order);
            return getResellsRes;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public List<GetResellRes> getResellsByRate(String order) throws BaseException {
        try {
            List<GetResellRes> getResellsByRateRes = resellDao.getResellsByRate();
            if (order.equals("ascending")) {
                Collections.sort(getResellsByRateRes, comparatorAsc);
            }
            if (order.equals("descending")) {
                Collections.sort(getResellsByRateRes, comparatorDesc);
            }
            return getResellsByRateRes;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public GetResellRes getResell(int resellIdx) throws BaseException {
        try {
            GetResellRes getResellRes = resellDao.getResell(resellIdx);
            return getResellRes;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public ResellBox getResellBoxes(int resellIdx) throws BaseException {
        try {
            ResellBox resellBox = resellDao.getResellBoxes(resellIdx);
            return resellBox;
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

    Comparator<GetResellRes> comparatorDesc = new Comparator<GetResellRes>() {
        @Override
        public int compare(GetResellRes a, GetResellRes b) {
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

    Comparator<GetResellRes> comparatorAsc = new Comparator<GetResellRes>() {
        @Override
        public int compare(GetResellRes a, GetResellRes b) {
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
