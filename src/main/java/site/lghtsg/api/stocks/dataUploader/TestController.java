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

    // 자동화 및 완료 후, Stock 정보 수집 제외하고 삭제 예정

    // 실시간 데이터 관련

    @GetMapping("/domestic")
    public void getKoreanStocks()  {
        stockScraper.scrapeKoreanStock();
    }

    @GetMapping("/american")
    public void getAmericanStocks()  {
        stockScraper.scrapeAmericanStock();
    }

    @GetMapping("domestic/trs")
    public void updateTrsDomestic() {
        stockScraper.updateTrsOfDomestic();
    }

    @GetMapping("american/trs")
    public void updateTrsAmerican() {
        stockScraper.updateTrsOfAmerican();
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
            e.printStackTrace();
        }
    }

    @GetMapping("/americanprices")
    public void getPricesOfAmerican() {
        try {
            apiConnector.getClosePricesOfAmerican();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("해외종목 업데이트 실패");
        }
    }
}
