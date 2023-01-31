package site.lghtsg.api.resells.dataUploader;


import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;
import site.lghtsg.api.config.BaseResponse;
import site.lghtsg.api.resells.dataUploader.model.Resell;
import site.lghtsg.api.resells.dataUploader.model.ResellTransaction;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
            String url = "https://kream.co.kr/brands/jordan";
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
            int resellIdx = 1081;
            while (!urlList.isEmpty()) {
                driver.get(urlList.get(0));
                //상품명, 브랜드, 이미지 url, 모델번호, 발매일자, 발매가, 색상 스크래핑
                WebElement name = driver.findElement(By.className("sub_title"));
                WebElement brand = driver.findElement(By.className("brand"));
                List<WebElement> imageUrlList = driver.findElements(By.className("slide_item"));
                List<WebElement> productInfo = driver.findElements(By.tagName("dd"));
                System.out.println(driver.findElement(By.className("num")).getText());

                if (productInfo.size() == 0) {
                    urlList.add(urlList.get(0));
                } else {
                    resellIdx++;
                    System.out.println(resellIdx + "번째");

                    Resell resell = new Resell();
                    resell.setName(name.getText());
                    resell.setReleasedPrice(productInfo.get(3).getText());
                    resell.setReleasedDate(productInfo.get(1).getText());
                    resell.setColor(productInfo.get(2).getText());
                    resell.setBrand(brand.getText());
                    resell.setProductNum(productInfo.get(0).getText());
                    resell.setImage1(imageUrlList.get(0).findElement(By.tagName("img")).getAttribute("src"));
                    int productCode = Integer.parseInt(driver.getCurrentUrl().replaceAll("[^0-9]", ""));
                    resell.setProductCode(productCode);

                    System.out.println(productCode);

                    if (imageUrlList.size() >= 2) {
                        resell.setImage2(imageUrlList.get(1).findElement(By.tagName("img")).getAttribute("src"));
                    }

                    if (imageUrlList.size() >= 3) {
                        resell.setImage3(imageUrlList.get(2).findElement(By.tagName("img")).getAttribute("src"));
                    }
                    resell.setIconImageIdx(1L);

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
                        //resellUploadDao.uploadResellTransaction(resellTransaction);
                    }
                    System.out.println("————————————");
                    break;
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

    public BaseResponse<String> updateByHour() {
        ChromeOptions options = new ChromeOptions();
        WebDriver driver = new ChromeDriver(options);
        List<Integer[]> resellIdxAndProductCodeList = resellUploadDao.getResellIdxAndProductCode();

        try {
            options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
            options.addArguments("--disable-popup-blocking");       //팝업안띄움
            //options.addArguments("headless");                       //브라우저 안띄움
            options.addArguments("--disable-gpu");            //gpu 비활성화
            options.addArguments("--blink-settings=imagesEnabled=false"); //이미지 다운 안받음
            options.addArguments("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
            //WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            while (!resellIdxAndProductCodeList.isEmpty()) {
                //현재는 모든 데이터의 productCode가 업로드 되어있지 않아 0일 때 예외처리
                if (resellIdxAndProductCodeList.get(0)[1] == 0) {
                    resellIdxAndProductCodeList.remove(0);
                    continue;
                }

                int price;
                int resellIdx = resellIdxAndProductCodeList.get(0)[0];
                driver.get("https://kream.co.kr/products/" + resellIdxAndProductCodeList.get(0)[1]);

                try {
                    price = Integer.parseInt(driver.findElement(By.className("num")).getText().replaceAll("[^0-9]", ""));
                }catch (Exception e){
                    continue;
                }

                LocalDate now = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd");
                String today = now.format(formatter);

                //ResellTodayTrans에 가격 추가
                resellUploadDao.updateResellTodayTransByHour(resellIdx, price, today);

                List<Integer> todayTransactionList = resellUploadDao.getTransactionToday(resellIdxAndProductCodeList.get(0)[0], today);

                int todayTotal = 0;

                for (Integer i : todayTransactionList) {
                    todayTotal += i;
                }

                int updatePrice = todayTotal / todayTransactionList.size();

                resellUploadDao.updateResellTransactionByHour(resellIdx, updatePrice, today);

                resellIdxAndProductCodeList.remove(0);
            }

            return new BaseResponse<>("한 시간 주기 업데이트 성공");
        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse<>("한 시간 주기 업데이트 실패");
        } finally {
            driver.quit();
        }
    }

    public BaseResponse<String> updateByDay() {
        ChromeOptions options = new ChromeOptions();
        WebDriver driver = new ChromeDriver(options);
        List<Integer[]> resellIdxAndProductCodeList = resellUploadDao.getResellIdxAndProductCode();

        try {
            options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
            options.addArguments("--disable-popup-blocking");       //팝업안띄움
            //options.addArguments("headless");                       //브라우저 안띄움
            options.addArguments("--disable-gpu");            //gpu 비활성화
            options.addArguments("--blink-settings=imagesEnabled=false"); //이미지 다운 안받음
            options.addArguments("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
            //WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            while (!resellIdxAndProductCodeList.isEmpty()) {
                //현재는 모든 데이터의 productCode가 업로드 되어있지 않아 0일 때 예외처리
                if (resellIdxAndProductCodeList.get(0)[1] == 0) {
                    resellIdxAndProductCodeList.remove(0);
                    continue;
                }

                int price;
                int resellIdx = resellIdxAndProductCodeList.get(0)[0];
                driver.get("https://kream.co.kr/products/" + resellIdxAndProductCodeList.get(0)[1]);

                try {
                    price = Integer.parseInt(driver.findElement(By.className("num")).getText().replaceAll("[^0-9]", ""));
                }catch (Exception e){
                    continue;
                }

                Date dDate = new Date();
                dDate = new Date(dDate.getTime() + (1000 * 60 * 60 * 24 * -1));
                SimpleDateFormat dSdf = new SimpleDateFormat("yy/MM/dd", Locale.KOREA);
                String yesterday = dSdf.format(dDate);

                LocalDate now = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd");
                String today = now.format(formatter);

                List<Integer> yesterdayTransactionList = resellUploadDao.getTransactionToday(resellIdxAndProductCodeList.get(0)[0], yesterday);

                int yesterdayTotal = 0;

                for (Integer i : yesterdayTransactionList) {
                    yesterdayTotal += i;
                }

                int updatePrice = yesterdayTotal / yesterdayTransactionList.size();

                //전날 거래 마무리
                resellUploadDao.updateResellTransactionByHour(resellIdx, updatePrice, yesterday);
                resellUploadDao.truncateResellTodayTrans(resellIdx, yesterday);


                //오늘 거래 시작
                resellUploadDao.startTodayTransaction(resellIdx, price, today);

                //ResellTodayTrans에 가격 추가
                resellUploadDao.updateResellTodayTransByHour(resellIdx, price, today);

                resellIdxAndProductCodeList.remove(0);
            }

            return new BaseResponse<>("하루 주기 업데이트 성공");
        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse<>("하루 시간 주기 업데이트 실패");
        } finally {
            driver.quit();
        }
    }
}