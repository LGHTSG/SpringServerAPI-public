package site.lghtsg.api.stocks;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.lghtsg.api.config.BaseException;
import site.lghtsg.api.config.BaseResponse;
import site.lghtsg.api.stocks.model.*;

import java.util.List;

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
            if (sort == null) { // sort가 null 이면 stockIdx 기준
                List<StockBox> stockBox = stockProvider.getStockBoxesByIdx(order);
                return new BaseResponse<>(stockBox);
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
    public BaseResponse<GetStockInfoRes> getStockInfo(@PathVariable ("stockIdx") int stockIdx) {
        try {
            GetStockInfoRes getStockInfoRes = stockProvider.getStockInfo(stockIdx);
            return new BaseResponse<>(getStockInfoRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    @ResponseBody
    @GetMapping("/{stockIdx}/prices") //특정 주식 누적 가격 조회 /stocks/1/prices
    public BaseResponse<List<GetStockPricesRes>> getStockPrices(@PathVariable ("stockIdx") int stockIdx) {
        try {
            List<GetStockPricesRes> getStockPricesRes = stockProvider.getStockPrices(stockIdx);
            return new BaseResponse<>(getStockPricesRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }
}
