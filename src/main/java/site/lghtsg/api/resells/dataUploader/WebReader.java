package site.lghtsg.api.resells.dataUploader;


import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;
import site.lghtsg.api.config.BaseResponse;
import site.lghtsg.api.resells.dataUploader.model.Resell;
import site.lghtsg.api.resells.dataUploader.model.ResellTransaction;

import java.util.ArrayList;
import java.util.List;

@Service
public class WebReader {

    private final ResellUploadDao resellUploadDao;

    public WebReader(ResellUploadDao resellUploadDao) {
        this.resellUploadDao = resellUploadDao;
    }

    public BaseResponse<String> scraping() {
        ChromeOptions options = new ChromeOptions();
        WebDriver driver = new ChromeDriver(options);
        try {
            options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
            options.addArguments("--disable-popup-blocking");       //팝업안띄움
            //options.addArguments("headless");                       //브라우저 안띄움
            options.addArguments("--disable-gpu");            //gpu 비활성화
            options.addArguments("--blink-settings=imagesEnabled=false"); //이미지 다운 안받음
            options.addArguments("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
            //WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));


            //login 페이지
            driver.get("https://kream.co.kr/login");

            //id, pw 입력
            driver.findElement(By.xpath("//*[@id=\"__layout\"]/div/div[2]/div[1]/div/div[1]/div/input")).sendKeys("wnsdud6969@naver.com");
            driver.findElement(By.xpath("//*[@id=\"__layout\"]/div/div[2]/div[1]/div/div[2]/div/input")).sendKeys("qkrwnsdud123!");

            //버튼 클릭
            driver.findElement(By.xpath("//*[@id=\"__layout\"]/div/div[2]/div[1]/div/div[3]/a")).click();
            System.out.println("로그인 성공 = " + driver.getCurrentUrl());
            Thread.sleep(1000);

            //스크래핑할 브랜드 url로 이동
            String url = "https://kream.co.kr/brands/dyson";
            driver.navigate().to(url);
            Thread.sleep(1000);


            //스크롤 내리기
            /*WebElement element = driver.findElement(By.className("product_card"));
            var stTime = new Date().getTime();
            while (new Date().getTime() < stTime + 50000) {
                Thread.sleep(500);
                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)", element);
            }*/


            List<WebElement> elements = driver.findElements(By.className("product_card"));
            List<String> urlList = new ArrayList<>();

            for (int i = 0; i < elements.size(); i++) {
                String productUrl = elements.get(i).findElement(By.tagName("a")).getAttribute("href");
                urlList.add(productUrl);
            }

            System.out.println("size = " + urlList.size());

            //스크래핑 시작
            int resellIdx = 1039;
            while (!urlList.isEmpty()) {
                driver.get(urlList.get(0));
                //상품명, 브랜드, 이미지 url, 모델번호, 발매일자, 발매가, 색상 스크래핑
                WebElement name = driver.findElement(By.className("sub_title"));
                WebElement brand = driver.findElement(By.className("brand"));
                String imageUrl = driver.findElement(By.tagName("img")).getAttribute("src");
                List<WebElement> productInfo = driver.findElements(By.tagName("dd"));


                if (productInfo.size() == 0) {
                    urlList.add(urlList.get(0));
                } else {
                    resellIdx++;
                    System.out.println(resellIdx + "번째");

                    Resell resell = new Resell(name.getText(), productInfo.get(3).getText(), productInfo.get(1).getText(), productInfo.get(2).getText(), brand.getText(), productInfo.get(0).getText(), imageUrl, 1L);

                    //거래 내역 더보기 클릭
                    try {
                        driver.findElement(By.xpath("//*[@id=\"panel1\"]/a")).click();
                    } catch (Exception e) {
                        resellIdx--;
                        urlList.remove(0);
                        continue;
                    }

                    Thread.sleep(1000);
                    List<WebElement> transaction = driver.findElements(By.className("list_txt"));

                    //거래가격, 거래날짜 저장
                    List<List<String>> priceList = new ArrayList<>();
                    for (int i = transaction.size() - 2; i >= 1; i -= 3) {
                        List<String> temp = new ArrayList<>();
                        temp.add(transaction.get(i).getText());
                        temp.add(transaction.get(i + 1).getText());
                        priceList.add(temp);
                    }

                    if (priceList.size() <= 1) {
                        resellIdx--;
                        urlList.remove(0);
                        continue;
                    }

                    resellUploadDao.uploadResell(resell);


                    for (List<String> list : priceList) {
                        String temp = list.get(0);
                        int price = Integer.parseInt(temp.replaceAll("[^0-9]", ""));
                        ResellTransaction resellTransaction = new ResellTransaction(resellIdx, price, list.get(1));
                        resellUploadDao.uploadResellTransaction(resellTransaction);
                    }
                    System.out.println("————————————");
                }
                urlList.remove(0);
            }
            return new BaseResponse<>(" 리셀 데이터 업로드 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse<>("리셀 데이터 업로드 실패");
        } finally {
            driver.quit();
        }
    }
}

