package site.lghtsg.api.stocks.dataUploader;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stocks/update")
public class TestController {

    private final StockApiConnector apiConnector;
    private final StockInfoUploader stockInfoUploader;
    private final StockScraper stockScraper;

    public TestController(StockApiConnector apiConnector, StockInfoUploader stockInfoUploader, StockScraper stockScraper) {
        this.apiConnector = apiConnector;
        this.stockInfoUploader = stockInfoUploader;
        this.stockScraper = stockScraper;
    }

    // Stock 테이블 정보수집 관련

    @GetMapping("/domestic_info")
    public void uploadDomesticStockInfo() {
        stockInfoUploader.uploadData();
    }

    @GetMapping("/american_info")
    public void getSNPInfos() {
        stockScraper.scrapeSNPInfos();
    }

    @GetMapping("/american_details")
    public void getSNPDetails() {
        stockScraper.scrapeSNP500Details();
    }

    @GetMapping("/domestic_details")
    public void getDomesticUrls() {
        stockScraper.scrapeKoreanStockUrls();
    }

    // 자동화 및 스크래핑 완료 후, Stock 정보 수집 제외하고 삭제 예정

    // 실시간 데이터 관련

    @GetMapping("/domestic")
    public void getKoreanStocks()  {
        stockScraper.scrapeDomesticStocks();
    }

    @GetMapping("/american")
    public void getAmericanStocks()  {
        stockScraper.scrapeSNP500();
    }

    // 세팅 관련
    @GetMapping("/set_mapper")
    public void setMapper() {
        stockScraper.setMapper();
    }

    // KIS api
    @GetMapping("/domesticprices")
    public void getPricesOfDomestic() {
        try {
            apiConnector.getClosePricesOfDomestic();
        } catch (Exception e) {
            try {
                apiConnector.getClosePricesOfDomestic();
            } catch (Exception e2) { // 여러번 실패하면 (일시적인 게 아니면) 중지
                e.printStackTrace();
            }
        }
    }
//
    // 스크래핑 완성 시 삭제 예정
    @GetMapping("/americanprices")
    public void getPricesOfAmerican() {
        try {
            apiConnector.getClosePricesOfAmerican();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                apiConnector.getClosePricesOfAmerican();
            } catch (Exception e2) { // 여러번 실패하면 중지
                e.printStackTrace();
            }
        }
    }

//    @GetMapping("/set_driver")
//    public void setDriver() {
//        stockScraper.setDriver();
//    }

//
//

//    @GetMapping("/cleardomestictrans")
//    public void clearDomesticTrans() {
//        stockScraper.clearDomesticTrans();
//    }
//
//    @GetMapping("/clearamericantrans")
//    public void clearAmericanTrans() {
//        stockScraper.clearAmericanTrans();
//    }


//    @GetMapping("/lasttrs_new")
//    public String updateLastTrs_NEW() {
//        return apiConnector.updateLastTrs_NEW();
//    }
//
//    @GetMapping("/token")
//    public void updateToken() {
//        try {
//            apiConnector.updateAccessToken();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @GetMapping("/trs_korean")
//    public void updateTrsDomestic() {
//        try {
//            stockScraper.updateTrsOfRealTime(true);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @GetMapping("/trs_american")
//    public void updateTrsAmerican() {
//        try {
//            stockScraper.updateTrsOfRealTime(false);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
