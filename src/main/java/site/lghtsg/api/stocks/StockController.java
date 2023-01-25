package site.lghtsg.api.stocks;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.BaseResponse;
import site.lghtsg.api.stocks.model.*;

import java.util.List;

import static site.lghtsg.api.config.Constant.*;

@RestController
@RequestMapping("/stocks")
public class StockController {
    @Autowired
    private final StockProvider stockProvider;

    public StockController(StockProvider stockProvider){
        this.stockProvider = stockProvider;
    }



    @ResponseBody
    @GetMapping("") //주식 리스트 조회 /stocks?sort=trading-volume&order=ascending or descending
    public BaseResponse<List<StockBox>> getStockBoxes(@RequestParam(required = false) String sort, @RequestParam(required = false) String order) {
        try {
            if (sort == null) sort = PARAM_DEFAULT;
            if (order == null) sort = PARAM_DEFAULT;
            if (sort != null && order == null) { // order이 null 이면 기본값 descending
                order = DESCENDING_PARAM;
            }
            List<StockBox> stockBox = stockProvider.getStockBoxes(sort, order);
            return new BaseResponse<>(stockBox);
        } catch (
                BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    @ResponseBody
    @GetMapping("/{stockIdx}/info") //특정 주식 정보 조회 /stocks/1/info
    public BaseResponse<StockBox> getStockInfo(@PathVariable ("stockIdx") long stockIdx) {
        try {
            StockBox stockBox = stockProvider.getStockInfo(stockIdx);
            return new BaseResponse<>(stockBox);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    @ResponseBody
    @GetMapping("/{stockIdx}/prices") //특정 주식 누적 가격 조회 /stocks/1/prices
    public BaseResponse<List<StockTransactionData>> getStockPrices(@PathVariable ("stockIdx") long stockIdx) {
        try {
            List<StockTransactionData> stockPricesRes = stockProvider.getStockPrices(stockIdx);
            return new BaseResponse<>(stockPricesRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }
}
