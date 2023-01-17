package site.lghtsg.api.stocks;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.stocks.model.*;

import java.util.List;

import static site.lghtsg.api.config.BaseResponseStatus.DATABASE_ERROR;


@Service
public class StockProvider {

    private final StockDao stockDao;

    @Autowired
    public StockProvider(StockDao stockDao){
        this.stockDao = stockDao;
    }


    public List<GetStockRes> getStocks(String sort, String order) throws BaseException {
        try {
            List<GetStockRes> getStockRes = stockDao.getStocks(sort, order);
            return getStockRes;
        } catch (Exception exception) {
            System.out.println(exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public List<GetStockRes> getStocksByIdx(String order) throws BaseException {
        try {
            List<GetStockRes> getStockRes = stockDao.getStocksByIdx(order);
            return getStockRes;
        } catch (Exception exception) {
            System.out.println(exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public GetStockInfoRes getStockInfo(int stockIdx) throws BaseException {
        try {
            GetStockInfoRes getStockInfoRes = stockDao.getStockInfo(stockIdx);
            return getStockInfoRes;
        } catch (Exception exception) {
            System.out.println(exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public List<GetStockPricesRes> getStockPrices(int stockIdx) throws BaseException {
        try {
            List<GetStockPricesRes> getStockPricesRes = stockDao.getStockPrices(stockIdx);
            return getStockPricesRes;
        } catch (Exception exception) {
            System.out.println(exception);
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
