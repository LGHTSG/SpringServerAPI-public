package site.lghtsg.api.stocks;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.lghtsg.api.common.model.*;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.stocks.model.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static site.lghtsg.api.config.BaseResponseStatus.*;
import static site.lghtsg.api.config.Constant.*;


@Service
public class StockProvider {

    private final StockDao stockDao;

    @Autowired
    public StockProvider(StockDao stockDao) {
        this.stockDao = stockDao;
    }

    public List<StockBox> getStockBoxes(String sort, String order) throws BaseException {
        List<StockBox> stockBoxes;

        try {
            stockBoxes = stockDao.getAllStockBoxes();
            if(stockBoxes.size() == 0){
                throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
            }
        }
        catch (Exception ignored) {
            throw new BaseException(DATABASE_ERROR);
        }
        stockBoxes = calculateRateOfChange(stockBoxes);

        stockBoxes = sortStockBoxes(stockBoxes, sort, order);

        return stockBoxes;
    }

    static List<StockBox> sortStockBoxes(List<StockBox> stockBoxes, String sort, String order) throws BaseException {
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
        } else if (sort.equals(SORT_MARKET_CAP_PARAM)){ // 시가총액
            comparator = new CompareByMarketCap(order);
        } else if (sort.equals(SORT_TRADING_VOL_PARAM)){ // 거래량
            comparator = new CompareByTradingVolume(order);
        } else {     // 기준이 없는(잘못 입력) 경우
            throw new BaseException(INCORRECT_REQUIRED_ARGUMENT);
        }

        // 3. 정렬
        try {
            stockBoxes.sort(comparator);
        }
        catch(Exception e) {
            throw new BaseException(DATALIST_SORTING_ERROR);
        }
        return stockBoxes;
    }

    //증감율 계산
    static List<StockBox> calculateRateOfChange(List<StockBox> stockBoxes) throws BaseException {
        try {
            for (int i=0, lim=stockBoxes.size(); i<lim; i++) {
                double rateOfChange = (double) (stockBoxes.get(i).getPrice() - stockBoxes.get(i).getClosingPrice()) / stockBoxes.get(i).getClosingPrice() * 100;
                rateOfChange = Math.round(rateOfChange * 10) / 10.0;
                stockBoxes.get(i).setRateOfChange(rateOfChange);
            }
        }
        catch (Exception e){
            throw new BaseException(DATALIST_SORTING_ERROR);
        }
        return stockBoxes;
    }

    public StockBox calculateRateOfChange(StockBox stockBox) throws BaseException {
        try {
                double rateOfChange = (double) (stockBox.getPrice() - stockBox.getClosingPrice()) / stockBox.getClosingPrice() * 100;
                rateOfChange = Math.round(rateOfChange * 10) / 10.0;
                stockBox.setRateOfChange(rateOfChange);
        }
        catch (Exception e){
            throw new BaseException(DATALIST_SORTING_ERROR);
        }
        return stockBox;
    }

    public StockBox getStockInfo(long stockIdx) throws BaseException {
        // 해당 stockIdx가 존재하는지 체크
        StockBox stockBox;
        try {
            stockBox = stockDao.getStockInfo(stockIdx);
            if(stockBox == null) throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
        }
        catch(Exception ignored){
            throw new BaseException(DATABASE_ERROR);
        }
        stockBox = calculateRateOfChange(stockBox);
        return stockBox;
    }

    public List<StockTransactionData> getStockPrices(long stockIdx) throws BaseException {
        List<StockTransactionData> stockTransactionData;
        try {
            stockTransactionData = stockDao.getStockPrices(stockIdx);
            if(stockTransactionData == null) throw new BaseException(REQUESTED_DATA_FAIL_TO_EXIST);
        }
        catch(Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
        return stockTransactionData;
    }

    public StockBox stockBox(int stockIdx) throws BaseException {
        return null;
    }

}
