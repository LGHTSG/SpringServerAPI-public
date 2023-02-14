package site.lghtsg.api.stocks.dataUploader;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import site.lghtsg.api.stocks.dataUploader.model.StockInfo;
import site.lghtsg.api.stocks.dataUploader.model.StockTransaction;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openqa.selenium.By.*;

@Service
public class StockScraper {

    private final StockUploadDao stockUploadDao;
    private Map<String, Integer> stockUrlsAndIdxs; // stockIdx 매핑 시 사용

    public StockScraper(StockUploadDao stockUploadDao) {
        this.stockUploadDao = stockUploadDao;
    }

    // 스크래핑 자동 실행

    /**
     * S&P 500 실시간 스크래핑
     */
    @Async
    @Scheduled(cron = "10 0/5,59 14-20 ? * MON-FRI") // UTC 기준
    public void scrapeSNP500() {
        // 실행시간 체크
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();

        if (minute == 59 && hour != 20) return; // 06시 빼고 제외
        else if (hour == 14 && minute < 29) return; // 개장 전 제외

        // 실행
        try {
            scrapeAmericanStock();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("S&P500 스크래핑 실패");
        }

    }

    /**
     * 국내주식 실시간 스크래핑
     */
    @Async
    @Scheduled(cron = "10 0/5,59 0-6 ? * MON-FRI")
    public void scrapeDomesticStocks() {
        // 실행시간 체크
        LocalDateTime now = LocalDateTime.now();

        // 16시 데이터 수집용 (스케줄러 적용시)
        if (now.getMinute() == 59 && now.getHour() != 6) return;

        try {
            scrapeKoreanStock();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("국내주식 스크래핑 실패");
        }
    }

    // TodayTrans 정리 및 lastTrs 업데이트(일단 수동)

//    @Async
//    @Scheduled(cron = "0 30 8 ? * MON-FRI")
    public void updateTrsOfDomestic() {
        updateTrsOfRealTime(true);
    }

//    @Async
//    @Scheduled(cron = "0 0 23 ? * MON-FRI")
    public void updateTrsOfAmerican() {
        updateTrsOfRealTime(false);
    }

    // 세팅 관련 메소드

    /**
     * 실시간 데이터 업로드 시 url-Idx 매핑에 사용될 Map 세팅
     */
    @Async
    @Scheduled(cron = "0 0 23 * * ?") // UTC 기준
    public void setMapper() {
        List<StockInfo> urlsAndIdxs = stockUploadDao.getUrlsAndIdxs();
        this.stockUrlsAndIdxs = new HashMap<>(urlsAndIdxs.size());

        for (StockInfo urlAndIdx : urlsAndIdxs) {
            this.stockUrlsAndIdxs.put(urlAndIdx.getUrl(), urlAndIdx.getStockIdx());
        }
    }

    private WebDriver setDriver() {
        ChromeOptions options = new ChromeOptions();

        options.addArguments("--disable-popup-blocking"); // 팝업 X
        options.addArguments("--disable-gpu"); //gpu 비활성화
        options.addArguments("--blink-settings=imagesEnabled=false"); //이미지 다운 X
        options.addArguments("headless");

        return new ChromeDriver(options);
    }

    // 스크래핑 소스코드

    public void scrapeAmericanStock() {
        // 소요시간 체크
        long sTime = System.currentTimeMillis();

        LocalDateTime now = LocalDateTime.now().plusHours(9); // UTC -> 한국시간

        String transactionTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:00"));

        String url = "https://kr.investing.com/equities/StocksFilter?noconstruct=1&smlID=595&sid=&tabletype=price&index_id=166";
        Connection conn = Jsoup.connect(url);
        try {
            Document document = conn.get();
            Elements elements = document.select("table#cross_rate_markets_stocks_1 tbody tr td");

            Map<Integer, StockTransaction> transactions = convertElement(elements, transactionTime,true);

            stockUploadDao.uploadPrices(transactions);

            long eTime = System.currentTimeMillis();
            System.out.println("스크래핑 완료, 소요 시간 = " + (eTime - sTime) + " ms");
        } catch (IOException e) {
           e.printStackTrace();
        }
    }

    public void scrapeKoreanStock() {
        // 소요시간 체크용
        long sTime = System.currentTimeMillis();

        // 거래시간
        LocalDateTime now = LocalDateTime.now().plusHours(9); // UTC -> 한국시간으로

        String transactionTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:00"));

        String url = "https://kr.investing.com/equities/StocksFilter?noconstruct=1&smlID=694&sid=&tabletype=price&index_id=all";
        Connection conn = Jsoup.connect(url)
                .header("x-requested-with", "XMLHttpRequest");

        try {
            Document doc = conn.get();
            Elements elements = doc.select("table#cross_rate_markets_stocks_1 tbody tr td");

            Map<Integer, StockTransaction> transactions = convertElement(elements, transactionTime, false);

            stockUploadDao.uploadPrices(transactions);

            long eTime = System.currentTimeMillis();
            System.out.println("스크래핑 완료, 소요 시간 = " + (eTime - sTime) + " ms");
        } catch (IOException ie) {
            ie.printStackTrace();
            System.out.println("국내 실시간 스크래핑 실패");
        }

    }

    /**
     * Elements stockAttrs -> Map(stockIdx, transaction) transactions
     * @param elements
     * @param transactionTime
     * @param isDollerData
     * @return
     */
    private Map<Integer, StockTransaction> convertElement(Elements elements, String transactionTime, boolean isDollerData) {
        int exchangeRate = 1230;

        Elements links = elements.select("a");

        Map<Integer, StockTransaction> transactions = new HashMap<>(links.size());
        System.out.println(links.size());

        for (int i = 0; i < links.size(); i++) {

            int priceCnt = 2 + i * 10;
            int tradeVolCnt = 7 + i * 10;

            String http = "https://kr.investing.com";

            String url = http + links.get(i).attr("href");
            Integer idx = stockUrlsAndIdxs.get(url);

            if (idx == null) continue;

            String priceSrc = elements.get(priceCnt).text().replaceAll(",","");
            int price = (isDollerData) ? (int) (Float.parseFloat(priceSrc) * exchangeRate)
                    : Integer.parseInt(priceSrc);

            if (isDollerData) {
                price = Math.round((float) price / 10) * 10; // 환율 계산에서 발생하는 1의 자리 금액 반올림
            }

            // 거래량 : 0, 123, 83.2K, 14.4M 등
            String tradeVol = elements.get(tradeVolCnt).text();

            if (tradeVol.equals("0")) {
                transactions.put(idx, new StockTransaction(idx, transactionTime, 0, price));
                continue;
            }

            int tradingVol = 0;
            int length = tradeVol.length();
            String lastChar = tradeVol.substring(length - 1);

            if (lastChar.equals("M") || lastChar.equals("K")) {
                float num = Float.parseFloat(tradeVol.substring(0, length - 1));
                int unit = (lastChar.equals("M")) ? 1000000 : 1000;

                tradingVol = (int) (num * unit);
            } else {
                tradingVol = Integer.parseInt(tradeVol);
            }
            transactions.put(idx, new StockTransaction(idx, transactionTime, tradingVol, price));
        }
        return transactions;
    }


    /**
     * updateTrs 소스코드
     * @param isDomestic
     */
    private void updateTrsOfRealTime(boolean isDomestic) {
        try {
            List<Integer> stockIdxs = stockUploadDao.getStockIdxs(isDomestic);
            System.out.println(stockIdxs.size() + " " + ((isDomestic)? "korean" : "SNP500"));
            // 압축
            stockUploadDao.clearTodayTrans(stockIdxs);

            // 전일 종가 복사(TodayTrans -> Transaction)
            stockUploadDao.copyOldestTr(stockIdxs, isDomestic);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("StockTodayTrans 정리 실패");
        }
    }


    // Stock 테이블 정보 스크래핑

    /**
     * 실시간 가격 제공하는 국내주식 url 수집
     */
    public void scrapeKoreanStockUrls() {
        WebDriver driver = setDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(8000));

        String url = "https://kr.investing.com/equities/south-korea";

        int cnt = 0;

        try {
            // 주식 목록 찾기
            driver.get(url);

            Select filter = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated
                    (xpath("/html/body/div[5]/section/div[6]/select"))));

            filter.selectByVisibleText("한국의 모든 주식");

            Thread.sleep(4000);

            List<WebElement> stockElements = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    xpath("/html/body/div[5]/section/div[8]/div/table/tbody"))).findElements(tagName("tr"));

            List<String> urls = new ArrayList<>(stockElements.size());

            for (WebElement element : stockElements) {
                urls.add(element.findElement(tagName("a")).getAttribute("href"));
            }

            for (String stockUrl : urls) {
                driver.get(stockUrl);

                WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        className("relative-selector_relative-selector__6cNpW")));
                WebElement nameElement = element.findElement(xpath("..")).findElement(xpath(".."))
                        .findElement(tagName("h1"));

                String[] nameAndCode = nameElement.getText().split(" ");
                String codeSrc = nameAndCode[nameAndCode.length-1];
                String code = codeSrc.substring(1, codeSrc.length()-1);

                // 개별 업로드 / 중간에 자주 끊김
                stockUploadDao.uploadKoreanStockUrl(StockInfo.builder()
                        .stockCode(code)
                        .url(stockUrl)
                        .build());

                cnt++;
            }

        } catch (InterruptedException e) {
            System.out.println("sleep 오류");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(cnt + " 까지 업로드");
            driver.quit();
        }
    }

    /**
     * S&P 500 이름, url 수집
     */
    public void scrapeSNPInfos() {
        WebDriver driver = setDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(10000));

        String url = "https://kr.investing.com/equities/americas";

        try {
            driver.get(url);

            Select filter = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated
                    (xpath("/html/body/div[5]/section/div[6]/select"))));

            filter.selectByVisibleText("S&P 500");

            Thread.sleep(3000);

            List<WebElement> stockElements = driver.findElement(xpath("/html/body/div[5]/section/div[8]/div/table/tbody"))
                    .findElements(tagName("tr"));

            List<StockInfo> stockInfos = new ArrayList<>(stockElements.size());

            for (WebElement stockElement : stockElements) {
                List<WebElement> info = stockElement.findElements(tagName("td"));

                WebElement link = info.get(1).findElement(tagName("a"));
                String name = link.getText();
                String stockUrl = link.getAttribute("href");

                stockInfos.add(StockInfo.builder()
                        .name(name)
                        .url(stockUrl)
                        .build());
            }

            stockUploadDao.uploadSNPInfos(stockInfos);

        } catch (InterruptedException e) {
            System.out.println("sleep 에서 에러");
        } finally {
            driver.quit();
        }
    }

    /**
     * S&P 500 code, issuedShare 수집
     */
    public void scrapeSNP500Details() {
        WebDriver driver = setDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(30000));

        try {
            List<String> stockUrls = stockUploadDao.getAmericanStockUrls(); // 탐색할 종목의 url

            for (String url : stockUrls) {
                driver.get(url);

                Thread.sleep(100);

                WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        className("relative-selector_relative-selector__6cNpW")));
                WebElement nameElement = element.findElement(xpath("..")).findElement(xpath(".."))
                        .findElement(tagName("h1"));

                // issuedShares 스크래핑
                List<WebElement> grid = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                        className("key-info_dd-numeric__5IsvY")));

                WebElement issuedSharesElement = grid.get(grid.size()-1);
                long issuedSharesSrc = Long.parseLong(
                        issuedSharesElement.findElement(tagName("span")).getText().replaceAll(",", "")
                ); // 반올림하기 전 값

                long issuedShares = Math.round(issuedSharesSrc / 1000.0) * 1000;
                System.out.println(issuedShares);

                String[] nameAndCode = nameElement.getText().split(" ");
                String codeSrc = nameAndCode[nameAndCode.length-1];
                String code = codeSrc.substring(1, codeSrc.length()-1);

                stockUploadDao.uploadSNP500Details(StockInfo.builder()
                        .url(url)
                        .stockCode(code)
                        .issuedShares(issuedShares)
                        .build());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("종목코드 얻기 실패");
        } finally {
            driver.quit();
        }
    }

}
